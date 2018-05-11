/*
 * webtop-mail is a WebTop Service developed by Sonicle S.r.l.
 * Copyright (C) 2014 Sonicle S.r.l.
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
 * You can contact Sonicle S.r.l. at email address sonicle@sonicle.com
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

import com.sonicle.webtop.core.util.ICalendarUtils;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fortuna.ical4j.data.*;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.*;
import net.fortuna.ical4j.model.property.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import sun.awt.windows.ThemeReader;

/**
 *
 * @author gabriele.bulfon
 */
public class ICalendarRequest {
	
	private final String UNSPECIFIED="(unspecified)";
	private final String NOEMAIL="(no email)";
	
	private final Calendar ical;
	
	private final VEvent vevent;
	private final Summary	icalSummary;
	private final Location icalLocation;
	private final DtStart icalDateStart;
	private final DtEnd icalDateEnd;
	private final Duration icalDuration;
	private final Organizer icalOrganizer;
	private final Description icalDescription;
	private final Attendee[] icalAttendees;
	
	private final String summary;
	private final String location;
	private final Date dateStart;
	private final Date dateEnd;
	private final String duration;
	private final String organizer;
	private final String organizerCN;
	private final String organizerEmail;
	private final String description;
	private final String[] attendees;
	private final String[] attendeesCN;
	private final String[] attendeesEmails;
	private final String[] attendeesAnswers;
	
	private final String method;
	private final String uid;
	private final Date lastmodified;
	
	public ICalendarRequest(InputStream istream) throws IOException, ParserException {
		ICalendarUtils.relaxParsingAndCompatibility();
		ical=ICalendarUtils.parse(istream);
		
		Method icalMethod=ical.getMethod();
		if (icalMethod==null) method="REQUEST";
		else method=icalMethod.getValue();
		vevent = (VEvent) ical.getComponent(Component.VEVENT);
		
		uid=vevent.getUid().getValue();
		
		UtcProperty d=vevent.getLastModified();
		if (d==null) d=vevent.getDateStamp();
		lastmodified=d==null?null:d.getDate();
		
		icalSummary=vevent.getSummary();
		icalLocation=vevent.getLocation();
		icalDateStart=vevent.getStartDate();
		icalDateEnd=vevent.getEndDate();
		icalDuration=vevent.getDuration();
		icalOrganizer=vevent.getOrganizer();
		icalDescription=vevent.getDescription();
		
		PropertyList plist=vevent.getProperties(Property.ATTENDEE);
		if (plist!=null) {
			Object oAttendees[]=plist.toArray();
			icalAttendees=new Attendee[oAttendees.length];
			attendees=new String[oAttendees.length];
			attendeesCN=new String[oAttendees.length];
			attendeesEmails=new String[oAttendees.length];
			attendeesAnswers=new String[oAttendees.length];
			for(int i=0;i<oAttendees.length;++i) {
				Attendee a=(Attendee)oAttendees[i];
				icalAttendees[i]=a;
				attendeesAnswers[i]=getParstat(a);
				attendeesEmails[i]=getEmail(a);
				attendeesCN[i]=getValue(a,"CN");
				attendees[i]=attendeesCN[i]+" ("+attendeesEmails[i]+")";
			}
		} else {
			icalAttendees=null;
			attendees=null;
			attendeesCN=null;
			attendeesEmails=null;
			attendeesAnswers=null;
		}
		
		summary=getValue(icalSummary);
		location=getValue(icalLocation);
		dateStart=getDate(icalDateStart);
		dateEnd=getDate(icalDateEnd);
		duration=getValue(icalDuration);
		organizerCN=getValue(icalOrganizer,"CN");
		organizerEmail=getEmail(icalOrganizer);
		organizer=organizerCN+" ("+organizerEmail+")";
		description=getValue(icalDescription);
	}
	
	public Calendar getCalendar() {
		return ical;
	}
	
	public VEvent getVEvent() {
		return vevent;
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getUID() {
		return uid;
	}
	
	public Date getLastModified() {
		return lastmodified;
	}
	
	public int getAttendees() {
		return attendees!=null?attendees.length:0;
	}
	
	public String getAttendeeCN(int i) {
		return attendeesCN!=null?attendeesCN[i]:null;
	}
	
	public String getAttendeeEmail(int i) {
		return attendeesEmails!=null?attendeesEmails[i]:null;
	}
	
	public String getAttendeeAnswer(int i) {
		return attendeesAnswers!=null?attendeesAnswers[i]:null;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getLocation() {
		return location;
	}
	
	public Date getStartDate() {
		return dateStart;
	}
	
	public Date getEndDate() {
		return dateEnd;
	}
	
	public String getOrganizer() {
		return organizer;
	}
	
	public String getOrganizerCN() {
		return organizerCN;
	}
	
	public String getOrganizerEmail() {
		return organizerEmail;
	}
	
	public String getOrganizerAddress() {
		String CN=organizerCN!=null?organizerCN:organizerEmail;
		return CN+" <"+organizerEmail+">";
	}
	
	private String getValue(Property p) {
		return (p==null?UNSPECIFIED:p.getValue());
	}
	
	private Date getDate(DateProperty dp) {
		return (dp==null?null:dp.getDate());
	}
	
	private String getValue(Property prop, String paramname) {
		if (prop==null) return UNSPECIFIED;
		Parameter param=prop.getParameter(paramname);
		return (param==null?UNSPECIFIED:param.getValue());
	}
	
	private String getEmail(Property p) {
		if (p==null) return NOEMAIL;
		String value=p.getValue();
		if (value==null) return NOEMAIL;
		if (value.startsWith("mailto:")) return value.substring(7);
		return NOEMAIL;
	}
	
	private String getParstatClassName(Property p) {
		if (p==null) return "";
		Parameter param=p.getParameter("PARTSTAT");
		if (param==null) return "wtmail-ical-tentative-xs";
		String v=param.getValue();
		if (v.equals("ACCEPTED")) return "wtmail-ical-accepted-xs"; 
		else if (v.equals("DECLINED")) return "wtmail-ical-declined-xs"; 
		else if (v.equals("NEEDS-ACTION")) return "wtmail-ical-needaction-xs"; 
		return "wtmail-ical-tentative-xs"; 
	}

	private String getParstat(Property p) {
		if (p==null) return "";
		Parameter param=p.getParameter("PARTSTAT");
		if (param==null) return "TENTATIVE";
		String v=param.getValue();
		if (v.equals("ACCEPTED")) return "ACCEPTED"; 
		else if (v.equals("DECLINED")) return "DECLINED"; 
		else if (v.equals("NEEDS-ACTION")) return "NEEDS-ACTION"; 
		return "TENTATIVE"; 
	}
	
	public String getHtmlView(Locale locale, String serviceVersion, String laf, ResourceBundle bundle) {
		String htmlmsg = "";
		if (method.equals("REQUEST")) {
			htmlmsg = MessageFormat.format(bundle.getString("tpl.ical.msg.invited"), StringEscapeUtils.escapeHtml4(organizerCN));
		} else if (method.equals("REPLY")) {
			Parameter param=icalAttendees[0].getParameter("PARTSTAT");
			String v=param.getValue();
			if (v.equals("ACCEPTED"))
				htmlmsg = MessageFormat.format(bundle.getString("tpl.ical.msg.accepted"), StringEscapeUtils.escapeHtml4(attendeesCN[0]));
			else if (v.equals("DECLINED"))
				htmlmsg = MessageFormat.format(bundle.getString("tpl.ical.msg.declined"), StringEscapeUtils.escapeHtml4(attendeesCN[0]));
			else
				htmlmsg = MessageFormat.format(bundle.getString("tpl.ical.msg.answered"), StringEscapeUtils.escapeHtml4(attendeesCN[0]));
			
		} else if (method.equals("CANCEL")) {
			htmlmsg = MessageFormat.format(bundle.getString("tpl.ical.msg.canceled"), StringEscapeUtils.escapeHtml4(organizerCN));
		}
		
		String htmlorganizer="<span class='"+getParstatClassName(icalOrganizer)+"'></span>&nbsp;"+StringEscapeUtils.escapeHtml4(organizer);
		StringBuilder htmlattendees=new StringBuilder();
		if (attendees!=null) {
			for(int i=0;i<attendees.length;++i) {
				String s=attendees[i];
				Attendee a=icalAttendees[i];
				htmlattendees.append("<span class='"+getParstatClassName(a)+"'></span>&nbsp;");
				htmlattendees.append(s); 
				htmlattendees.append("<br>"); 
			}
		}
		return String.format(
			locale,
			"<html><head><meta content='text/html; charset=utf-8' http-equiv='Content-Type'>"+
					"<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/com.sonicle.webtop.mail/"+serviceVersion+"/laf/"+laf+"/service.css\" />"+
			"</head><body>"+
					
			"<table border=0 cellpadding=4 class=wtmail-ical-tabletitle>"+
			"<tr><td class=wtmail-ical-title>%s</td></tr>"+
			"</table>"+

			"<table border=0 cellpadding=4 class=wtmail-ical-tabledata>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+
			//"<tr><td class=wtmail-ical-label>Quando:</td><td>"+sDtStart+"</td></tr>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+
//			"<tr><td class=wtmail-ical-label>Durata:</td><td class=wtmail-ical-data>%s</td></tr>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+
			"<tr><td class=wtmail-ical-label>%s</td><td class=wtmail-ical-data>%s</td></tr>"+

			"</table>"+
			"</body></html>",
				htmlmsg,
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.summary")),
				StringEscapeUtils.escapeHtml4(summary),
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.location")),
				StringEscapeUtils.escapeHtml4(location),
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.start")),
				StringEscapeUtils.escapeHtml4(String.format(locale,"%tc",dateStart)),
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.end")),
				StringEscapeUtils.escapeHtml4(String.format(locale,"%tc",dateEnd)),
//				htmlduration,
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.organizer")),
				htmlorganizer,
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.description")),
				hyperlinkText(StringEscapeUtils.escapeHtml4(description),"_new"),
				StringEscapeUtils.escapeHtml4(bundle.getString("tpl.ical.event.attendees")),
				htmlattendees
		);
	}
	
/*	public static void main(String args[]) throws Exception {
		ICalendarRequest ir=new ICalendarRequest(
				new FileInputStream(
						"E:\\gabriele.bulfon\\Downloads\\ibuildings.ics"
				)
		);
		Service.logger.debug("METHOD = "+ir.getMethod());
		Service.logger.debug("UID    = "+ir.getUID());
		String html=ir.getHtmlView(Locale.FRENCH,"win");
		FileOutputStream fout=new FileOutputStream("E:\\gabriele.bulfon\\Downloads\\ibuildings.html");
		IOUtils.copy(new ByteArrayInputStream(html.getBytes("UTF8")), fout);
		fout.close();
	}*/
	
	// NOTES:   1) \w includes 0-9, a-z, A-Z, _
	//          2) The leading '-' is the '-' character. It must go first in character class expression
	private static final String VALID_CHARS = "-\\w+&@#/%=~()|";
	private static final String VALID_NON_TERMINAL = "?!:,.;";

	// Notes on the expression:
	//  1) Any number of leading '(' (left parenthesis) accepted.  Will be dealt with.  
	//  2) s? ==> the s is optional so either [http, https] accepted as scheme
	//  3) All valid chars accepted and then one or more
	//  4) Case insensitive so that the scheme can be hTtPs (for example) if desired
	private static final Pattern URI_FINDER_PATTERN = Pattern.compile("\\(*https?://["+ VALID_CHARS + VALID_NON_TERMINAL + "]*[" +VALID_CHARS + "]", Pattern.CASE_INSENSITIVE );
		
	/**
	 * <p>
	 * Finds all "URL"s in the given _rawText, wraps them in 
	 * HTML link tags and returns the result (with the rest of the text
	 * html encoded).
	 * </p>
	 * <p>
	 * We employ the procedure described at:
	 * http://www.codinghorror.com/blog/2008/10/the-problem-with-urls.html
	 * which is a <b>must-read</b>.
	 * </p>
	 * Basically, we allow any number of left parenthesis (which will get stripped away)
	 * followed by http:// or https://.  Then any number of permitted URL characters
	 * (based on http://www.ietf.org/rfc/rfc1738.txt) followed by a single character
	 * of that set (basically, those minus typical punctuation).  We remove all sets of 
	 * matching left & right parentheses which surround the URL.
	 *</p>
	 * <p>
	 * This method *must* be called from a tag/component which will NOT
	 * end up escaping the output.  For example:
	 * <PRE>
	 * <h:outputText ... escape="false" value="#{core:hyperlinkText(textThatMayHaveURLs, '_blank')}"/>
	 * </pre>
	 * </p>
	 * <p>
	 * Reason: we are adding <code>&lt;a href="..."&gt;</code> tags to the output *and*
	 * encoding the rest of the string.  So, encoding the outupt will result in
	 * double-encoding data which was already encoded - and encoding the <code>a href</code>
	 * (which will render it useless).
	 * </p>
	 * <p>
	 * 
	 * @param   _rawText  - if <code>null</code>, returns <code>""</code> (empty string).
	 * @param   _target   - if not <code>null</code> or <code>""</code>, adds a target attributed to the generated link, using _target as the attribute value.
	 */
	public static final String hyperlinkText( final String _rawText, final String _target ) {

		String returnValue = null;

		if ( !StringUtils.isBlank( _rawText ) ) {

			final Matcher matcher = URI_FINDER_PATTERN.matcher( _rawText );

			if ( matcher.find() ) {

				final int originalLength    =   _rawText.length();

				final String targetText = ( StringUtils.isBlank( _target ) ) ? "" :  " target=\"" + _target.trim() + "\"";
				final int targetLength      =   targetText.length();

				// Counted 15 characters aside from the target + 2 of the URL (max if the whole string is URL)
				// Rough guess, but should keep us from expanding the Builder too many times.
				final StringBuilder returnBuffer = new StringBuilder( originalLength * 2 + targetLength + 15 );

				int currentStart;
				int currentEnd;
				int lastEnd     = 0;

				String currentURL;

				do {
					currentStart = matcher.start();
					currentEnd = matcher.end();
					currentURL = matcher.group();

					// Adjust for URLs wrapped in ()'s ... move start/end markers
					//      and substring the _rawText for new URL value.
					while ( currentURL.startsWith( "(" ) && currentURL.endsWith( ")" ) ) {
						currentStart = currentStart + 1;
						currentEnd = currentEnd - 1;

						currentURL = _rawText.substring( currentStart, currentEnd );
					}

					while ( currentURL.startsWith( "(" ) ) {
						currentStart = currentStart + 1;

						currentURL = _rawText.substring( currentStart, currentEnd );
					}

					// Text since last match
					returnBuffer.append( StringEscapeUtils.escapeHtml4(_rawText.substring( lastEnd, currentStart ) ) );

					// Wrap matched URL
					returnBuffer.append( "<a href=\"" + currentURL + "\"" + targetText + ">" + currentURL + "</a>" );

					lastEnd = currentEnd;

				} while ( matcher.find() );

				if ( lastEnd < originalLength ) {
					returnBuffer.append( StringEscapeUtils.escapeHtml4( _rawText.substring( lastEnd ) ) );
				}

				returnValue = returnBuffer.toString();
			}
		} 

		if ( returnValue == null ) {
			returnValue = StringEscapeUtils.escapeHtml4( _rawText );
		}

		return returnValue;

	}	
	
}
