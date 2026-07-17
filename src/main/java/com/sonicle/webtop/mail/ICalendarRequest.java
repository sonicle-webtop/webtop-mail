/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2026 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email internetAddress sonicle@sonicle.com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Sonicle WebTop" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Sonicle WebTop".
 */
package com.sonicle.webtop.mail;

import com.sonicle.commons.Check;
import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.IdentifierUtils;
import com.sonicle.commons.LangUtils;
import com.sonicle.commons.time.DateTimeWindow;
import com.sonicle.commons.time.JodaTimeUtils;
import com.sonicle.commons.web.json.MapItem;
import com.sonicle.commons.web.json.MapItemList;
import com.sonicle.webtop.calendar.CalendarUtils;
import com.sonicle.webtop.core.app.UIBoot;
import com.sonicle.webtop.core.app.WT;
import com.sonicle.webtop.core.app.sdk.WTParseException;
import com.sonicle.webtop.core.sdk.ServiceManifest;
import com.sonicle.webtop.core.util.ICal4jUtils;
import com.sonicle.webtop.core.util.ICalendarUtils;
import com.sonicle.webtop.core.util.RRuleStringify;
import freemarker.template.TemplateException;
import jakarta.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Sequence;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 *
 * @author malbinola
 */
public class ICalendarRequest {
	private final InternetAddress requestFrom;
	private final Calendar iCalendar;
	private final String method;
	private final Action action;
	private final String uid;
	private final DateTime lastModified;
	private final int sequence;
	private final InternetAddress organizer;
	private final When when;
	private final String summary;
	private final String location;
	private final String description;
	private final String comment;
	private final List<AttendeeItem> attendees;
	
	private static final String SERVICE_ID = "com.sonicle.webtop.mail";
	private static final String DAY_SKELETON = "yMMMMEEEEd";
	private static final String TIMED_SKELETON = "yMMMMEEEEdHm";
	
	public ICalendarRequest(final InputStream is) throws WTParseException, IOException {
		this(is, null);
	}
	
	public ICalendarRequest(final InputStream is, final InternetAddress requestFrom) throws WTParseException, IOException {
		try {
			iCalendar = ICalendarUtils.parse(is);
		} catch (ParserException ex) {
			throw new WTParseException(ex);
		}
		this.requestFrom = requestFrom;
		
		method = extractMethod(iCalendar);
		VEvent ve = ICalendarUtils.getVEvent(iCalendar);
		
		uid = extractUid(ve);
		lastModified = extractLastModified(ve);
		sequence = extractSequence(ve);
		organizer = ICalendarUtils.getOrganizerAddress(ve);
		when = extractWhen(ve);
		summary = ICalendarUtils.getSummary(ve);
		location = ICal4jUtils.getPropertyValue(ve.getLocation());
		description = ICal4jUtils.getPropertyValue(ve.getDescription());
		comment = ICal4jUtils.getPropertyValue(ve, Property.COMMENT);
		attendees = extractAttendees(ve);
		action = computeAction();
	}

	public InternetAddress getRequestFrom() {
		return requestFrom;
	}

	public Calendar getiCalendar() {
		return iCalendar;
	}
	
	public String getMethod() {
		return method;
	}

	public Action getAction() {
		return action;
	}

	public String getUid() {
		return uid;
	}
	
	public DateTime getLastModified() {
		return lastModified;
	}

	public int getSequence() {
		return sequence;
	}

	public InternetAddress getOrganizer() {
		return organizer;
	}

	public String getSummary() {
		return summary;
	}

	public String getLocation() {
		return location;
	}

	public When getWhen() {
		return when;
	}

	public String getDescription() {
		return description;
	}
	
	public String getComment() {
		return comment;
	}

	public List<AttendeeItem> getAttendees() {
		return Collections.unmodifiableList(attendees);
	}
	
	public String generatePreviewBody(final Locale locale, final DateTimeZone timezone) throws IOException, TemplateException {
		MapItem i18n = new MapItem();
		i18n.put("action", actionHeader(locale));
		i18n.put("title", WT.lookupResource(SERVICE_ID, locale, "icalpreview.title"));
		i18n.put("location", WT.lookupResource(SERVICE_ID, locale, "icalpreview.location"));
		i18n.put("when", WT.lookupResource(SERVICE_ID, locale, "icalpreview.when"));
		i18n.put("organizer", WT.lookupResource(SERVICE_ID, locale, "icalpreview.organizer"));
		i18n.put("description", WT.lookupResource(SERVICE_ID, locale, "icalpreview.description"));
		i18n.put("attendees", WT.lookupResource(SERVICE_ID, locale, "icalpreview.attendees"));
		i18n.put("comment", commentHeader(locale));
		
		MapItem base = new MapItem();
		base.put("summary", StringUtils.defaultIfBlank(summary, ""));
		base.put("location", LangUtils.linkifyText(LangUtils.encodeForHTMLContent(StringUtils.defaultIfBlank(location, ""))));
		
		WhenValue whenValue = whenValue(locale, timezone);
		base.put("when", StringUtils.defaultIfBlank(whenValue.value, ""));
		i18n.put("whenFirstInstance", whenValue.isFirstInstance ? WT.lookupResource(SERVICE_ID, locale, "icalpreview.when.firstinstance") : null);
		
		base.put("repeats", repeatsValue(locale, timezone));
		base.put("organizer", organizerValue());
		base.put("description", StringUtils.defaultIfBlank(descriptionValue(), ""));
		base.put("comment", StringUtils.defaultIfBlank(commentValue(), null));
		
		MapItemList atts = new MapItemList();
		for (AttendeeItem entry : attendees) {
			MapItem item = new MapItem();
			item.put("cn", StringUtils.defaultIfBlank(entry.getInternetAddress().getPersonal(), entry.getInternetAddress().getAddress()));
			item.put("address", StringUtils.defaultIfBlank(entry.getInternetAddress().getAddress(), ""));
			item.put("status", StringUtils.lowerCase(entry.getStatus()));
			item.put("statusText", WT.lookupResource(SERVICE_ID, locale, "icalpreview.attendee.status." + StringUtils.lowerCase(entry.getStatus())));
			atts.add(item);
		}
		
		MapItem vars = new MapItem();
		vars.put("i18n", i18n);
		vars.put("base", base);
		vars.put("attendees", atts);
		vars.put("action", StringUtils.lowerCase(EnumUtils.getName(action)));
		
		return WT.buildTemplate(SERVICE_ID, "tpl/icalpreview.html", vars);
	}
	
	public static String htmlWrap(final String body, final String charset, final ServiceManifest manifest, final String theme, final String lookAndFeel) {
		String html = "<html>";
		html += "<head>";
		html += "<meta charset=\"" + LangUtils.encodeForHTMLAttribute(charset) + "\">";
		html += "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">";
		html += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + LangUtils.encodeForHTMLAttribute(charset) + "\">";
		for (String href : UIBoot.getExtJsMinimalStylesheetUrls("classic", theme, false, false)) {
			html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + href + "\" />";
		}
		html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + manifest.getPackageLookAndFeelUrl(lookAndFeel) + "/" + "icalpreview.css" + "\" />";
		html += "</head>";
		html += "<body>" + body + "</body>";
		html += "</html>";
		return html;
	}
	
	private String actionHeader(final Locale locale) {
		String who;
		if (Action.REPLY.equals(action) || Action.PROPOSE.equals(action)) {
			who = "";
			AttendeeItem replyWho = getReplyingAttendee();
			if (replyWho != null) {
				who = StringUtils.defaultIfBlank(replyWho.getInternetAddress().getPersonal(), replyWho.getInternetAddress().getAddress());
			}
		} else {
			who = organizer.getPersonal();
		}
		return WT.lookupFormattedResource(SERVICE_ID, locale, "icalpreview.action." + StringUtils.lowerCase(EnumUtils.getName(action)), who);
	}
	
	private String commentHeader(final Locale locale) {
		String who;
		if (Action.REPLY.equals(action) || Action.PROPOSE.equals(action)) {
			who = "";
			AttendeeItem replyWho = getReplyingAttendee();
			if (replyWho != null) {
				who = StringUtils.defaultIfBlank(replyWho.getInternetAddress().getPersonal(), replyWho.getInternetAddress().getAddress());
			}
		} else {
			who = organizer.getPersonal();
		}
		return WT.lookupFormattedResource(SERVICE_ID, locale, "icalpreview.comment", who);
	}
	
	private MapItem organizerValue() {
		MapItem item = new MapItem();
		item.put("cn", organizer.getPersonal());
		item.put("address", StringUtils.defaultIfBlank(organizer.getAddress(), ""));
		return item;
	}
	
	private String commentValue() {
		AttendeeItem replyWho = null;
		if (Action.REPLY.equals(action) || Action.PROPOSE.equals(action)) {
			replyWho = getReplyingAttendee();
		}
		if (replyWho != null) {
			if (!StringUtils.isBlank(replyWho.getComment())) return replyWho.getComment();
		}
		return comment;
	}
	
	private String descriptionValue() {
		return LangUtils.linkifyText(LangUtils.encodeLineBreaksForHTML(LangUtils.sanitizeHtml(description)));
	}
	
	private static class WhenValue {
		public final String value;
		public final boolean isFirstInstance;
		
		public WhenValue(String value, boolean isFirstInstance) {
			this.value = value;
			this.isFirstInstance = isFirstInstance;
		}
	}
	
	private WhenValue whenValue(final Locale locale, final DateTimeZone timezone) {
		String timezoneSuffix = " (" + timezone.getID() + ")";
		String formatSkeleton;
		DateTime start, end;
		if (when.isAllDay()) {
			formatSkeleton = DAY_SKELETON;
			final int diffDays = JodaTimeUtils.calendarDaysBetween(when.getStart(), when.getEnd(), true, true);
			if (diffDays == 0) {
				start = end = when.getStart();
			} else {
				start = when.getStart();
				end = when.getEnd();
			}
			
		} else {
			formatSkeleton = TIMED_SKELETON;
			start = when.getStart().withZone(timezone);
			end = when.getEnd().withZone(timezone);
		}
		
		boolean isFirstInstance = false;
		ICalendarUtils.RRInfo rrInfo = when.getRRInfo();
		if (rrInfo != null) {
			DateTimeWindow dtw = findFirstRRInstanceFrom(rrInfo, start, end, when.getTimezone(), JodaTimeUtils.now().withTimeAtStartOfDay());
			if (dtw != null) {
				isFirstInstance = true;
				start = dtw.getStart();
				end = dtw.getEnd();
			}
		}
		
		return new WhenValue(JodaTimeUtils.formatDateTimeInterval(formatSkeleton, locale, start, end) + timezoneSuffix, isFirstInstance);
	}
	
	private String repeatsValue(final Locale locale, final DateTimeZone timezone) {
		if (when.getRRInfo() == null) {
			return null;
			
		} else {
			RRuleStringify.Strings strings = WT.getRRuleStringifyStrings(locale);
			RRuleStringify rrs = new RRuleStringify(locale, strings);
			rrs.setPrefixText(WT.lookupFormattedResource(SERVICE_ID, locale, "icalpreview.repeats", rrs.formatDate(when.getStart(), timezone)));
			return rrs.toHumanReadableTextQuietly(when.getRRInfo().getRecur(), timezone);
		}
	}
	
	private AttendeeItem getReplyingAttendee() {
		if (requestFrom != null) {
			for (AttendeeItem attendee : attendees) {
				if (StringUtils.equalsIgnoreCase(attendee.getInternetAddress().getAddress(), requestFrom.getAddress())) {
					return attendee;
				}
			}
		} else if (!attendees.isEmpty()) {
			return attendees.get(0);
		}
		return null;
	}
	
	private When extractWhen(final VEvent ve) throws WTParseException {
		DateTime start, end;
		DateTimeZone timezone;
		
		final boolean allDay = ICal4jUtils.isAllDay(ve);
		if (allDay) {
			LocalDate localStart = ICal4jUtils.toJodaLocalDate(ICal4jUtils.getDatePropertyValue(ve.getStartDate()), DateTimeZone.UTC);
			if (localStart == null) throw new WTParseException("Invalid DTSTART [{}]", ve.getStartDate().toString());
			LocalDate localEnd = ICal4jUtils.toJodaLocalDate(ICal4jUtils.getDatePropertyValue(ve.getEndDate()), DateTimeZone.UTC);
			if (localEnd == null) throw new WTParseException("Invalid DTEND [{}]", ve.getEndDate().toString());
			
			start = localStart.toDateTimeAtStartOfDay();
			end = localEnd.toDateTimeAtStartOfDay();
			timezone = start.getZone();
			
		} else {
			start = ICal4jUtils.toJodaDateTime((net.fortuna.ical4j.model.DateTime)ICal4jUtils.getDatePropertyValue(ve.getStartDate()), DateTimeZone.UTC);
			if (start == null) throw new WTParseException("Invalid DTSTART [{}]", ve.getStartDate().toString());
			end = ICal4jUtils.toJodaDateTime((net.fortuna.ical4j.model.DateTime)ICal4jUtils.getDatePropertyValue(ve.getEndDate()), DateTimeZone.UTC);
			if (end == null) throw new WTParseException("Invalid DTEND [{}]", ve.getEndDate().toString());
			timezone = start.getZone();
		}
		
		ICalendarUtils.RRInfo recurInfo = ICalendarUtils.extractRRInfo(ve);
		return new When(allDay, start, end, timezone, recurInfo);
	}
	
	private ArrayList<AttendeeItem> extractAttendees(final VEvent ve) throws WTParseException {
		ArrayList<AttendeeItem> items = new ArrayList<>();
		PropertyList atts = ve.getProperties(Property.ATTENDEE);
		if (!atts.isEmpty()) {
			for (Object o: atts) {
				items.add(extractAttendee(ICalendarUtils.toAttendeeItem((Attendee)o)));
				//items.add(extractAttendee((Attendee)o));
			}
		}
		return items;
	}
	
	private AttendeeItem extractAttendee(final ICalendarUtils.AttendeeItem attendee) {
		return new AttendeeItem(attendee.getRecipient(), attendee.getPartStat().getValue(), attendee.getResponseComment());
	}
	
	/*
	private AttendeeItem extractAttendee(final Attendee attendee) throws WTParseException {
		// Evaluates attendee details
		// Extract email and common name (CN)
		// Eg: CN=Henry Cabot:MAILTO:hcabot@host2.com -> drop ":MAILTO:"
		URI uri = attendee.getCalAddress();
		Cn cn = (Cn)attendee.getParameter(Parameter.CN);
		if (uri != null) {
			String address = uri.getSchemeSpecificPart();
			InternetAddress ia = InternetAddressUtils.toInternetAddress(address, (cn == null) ? address : cn.getValue());
			
			// Evaluates attendee response status
			PartStat partstat = (PartStat)attendee.getParameter(Parameter.PARTSTAT);
			if (partstat == null) partstat = PartStat.NEEDS_ACTION;

			return new AttendeeItem(ia, partstat.getValue());
			
		} else {
			throw new WTParseException("Invalid ATTENDEE [{}]", attendee.toString());
		}
	}
	*/
	
	private String extractMethod(final Calendar ical) {
		Method icMethod = ical.getMethod();
		return (icMethod == null) ? Method.REQUEST.getValue() : icMethod.getValue();
	}
	
	private Action computeAction() {
		Check.notNull(method);
		switch (method) {
			case "REQUEST":
				return sequence == 0 ? Action.INVITE : Action.MODIFY;
			case "REPLY":
				return Action.REPLY;
			case "CANCEL":
				return Action.CANCEL;
			case "COUNTER":
				return Action.PROPOSE;
			case "DECLINECOUNTER":
				return Action.PROPOSAL_DECLINE;
		}
		return null;
	}
	
	private String extractUid(final VEvent ve) {
		String veUid = ICalendarUtils.getUidValue(ve);
		return (!StringUtils.isBlank(veUid)) ? veUid : ICalendarUtils.buildUid(DigestUtils.md5Hex(IdentifierUtils.getUUIDTimeBased(true)), "nodomain.tld");
	}
	
	private DateTime extractLastModified(final VEvent ve) {
		DateTime dt = ICalendarUtils.getPropertyValueAsDateTime(ve.getLastModified(), org.joda.time.DateTimeZone.UTC);
		if (dt == null) dt = ICalendarUtils.getPropertyValueAsDateTime(ve.getDateStamp(), org.joda.time.DateTimeZone.UTC);
		return dt;
	}
	
	private int extractSequence(final VEvent ve) {
		Sequence veSequence = ve.getSequence();
		return (veSequence != null) ? veSequence.getSequenceNo() : 0;
	}
	
	private static DateTimeWindow findFirstRRInstanceFrom(final ICalendarUtils.RRInfo rrInfo, final DateTime start, final DateTime end, final DateTimeZone timezone, final DateTime from) {
		List<LocalDate> dates = ICal4jUtils.calculateRecurrenceSet(rrInfo.getRecur(), start, true, rrInfo.getExDates(), start, end, timezone, from, null, 1);
		return (!dates.isEmpty()) ? CalendarUtils.computeStartEndForEventInstance(dates.get(0), start.toLocalDateTime(), end.toLocalDateTime(), timezone) : null;
	}
	
	public static enum Action {
		INVITE, // Invite attendees to an event
		MODIFY, // Modify a previous invitation
		CANCEL, // Cancel an event or remove attendees
		REPLY, // Respond to a REQUEST with attendance status
		PROPOSE, // Propose changes to an event (counter-offer)
		PROPOSAL_DECLINE // Reject a counter-proposal
		;
	}
	
	public static class When {
		private final boolean allDay;
		private final DateTime start;
		private final DateTime end;
		private final DateTimeZone timezone;
		private final ICalendarUtils.RRInfo rrInfo;
		
		public When(boolean allDay, DateTime start, DateTime end) {
			this(allDay, start, end, null, null);
		}
		
		public When(boolean allDay, DateTime start, DateTime end, DateTimeZone timezone, ICalendarUtils.RRInfo rrInfo) {
			this.allDay = allDay;
			this.start = Check.notNull(start, "start");
			this.end = Check.notNull(end, "end");
			this.timezone = (timezone == null) ? start.getZone() : timezone;
			this.rrInfo = rrInfo;
		}

		public boolean isAllDay() {
			return allDay;
		}

		public DateTime getStart() {
			return start;
		}

		public DateTime getEnd() {
			return end;
		}

		public DateTimeZone getTimezone() {
			return timezone;
		}
		
		public ICalendarUtils.RRInfo getRRInfo() {
			return rrInfo;
		}
	}
	
	public static class AttendeeItem {
		private final InternetAddress internetAddress;
		private final String status;
		private final String comment;
		
		public AttendeeItem(InternetAddress internetAddress, String status, String comment) {
			this.internetAddress = Check.notNull(internetAddress, "internetAddress");
			this.status = Check.notEmpty(status, "status");
			this.comment = comment;
		}

		public InternetAddress getInternetAddress() {
			return internetAddress;
		}

		public String getStatus() {
			return status;
		}

		public String getComment() {
			return comment;
		}
	}
}
