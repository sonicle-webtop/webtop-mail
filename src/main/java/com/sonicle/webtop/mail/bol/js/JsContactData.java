/* 
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
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2014 Sonicle S.r.l.".
 */
package com.sonicle.webtop.mail.bol.js;

import com.sonicle.commons.EnumUtils;
import com.sonicle.commons.time.DateTimeUtils;
import com.sonicle.webtop.contacts.model.Contact;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author malbinola
 */
public class JsContactData {
	public String title;
	public String firstName;
	public String lastName;
	public String nickname;
	public String gender;
	public String mobile;
	public String pager1;
	public String pager2;
	public String email1;
	public String email2;
	public String email3;
	public String instantMsg1;
	public String instantMsg2;
	public String instantMsg3;
	public String workAddress;
	public String workPostalCode;
	public String workCity;
	public String workState;
	public String workCountry;
	public String workTelephone1;
	public String workTelephone2;
	public String workFax;
	public String homeAddress;
	public String homePostalCode;
	public String homeCity;
	public String homeState;
	public String homeCountry;
	public String homeTelephone1;
	public String homeTelephone2;
	public String homeFax;
	public String otherAddress;
	public String otherPostalCode;
	public String otherCity;
	public String otherState;
	public String otherCountry;
	public String company;
	public String function;
	public String department;
	public String manager;
	public String assistant;
	public String assistantTelephone;
	public String partner;
	public String birthday;
	public String anniversary;
	public String url;
	public String notes;
	public String picture;
	
	public JsContactData() {}
	
	public JsContactData(Contact contact) {
		DateTimeFormatter ymdFmt = DateTimeUtils.createYmdFormatter();
		
		title = contact.getTitle();
		firstName = contact.getFirstName();
		lastName = contact.getLastName();
		nickname = contact.getNickname();
		gender = EnumUtils.toSerializedName(contact.getGender());
		mobile = contact.getMobile();
		pager1 = contact.getPager1();
		pager2 = contact.getPager2();
		email1 = contact.getEmail1();
		email2 = contact.getEmail2();
		email3 = contact.getEmail3();
		instantMsg1 = contact.getInstantMsg1();
		instantMsg2 = contact.getInstantMsg2();
		instantMsg3 = contact.getInstantMsg3();
		workAddress = contact.getWorkAddress();
		workPostalCode = contact.getWorkPostalCode();
		workCity = contact.getWorkCity();
		workState = contact.getWorkState();
		workCountry = contact.getWorkCountry();
		workTelephone1 = contact.getWorkTelephone1();
		workTelephone2 = contact.getWorkTelephone2();
		workFax = contact.getWorkFax();
		homeAddress = contact.getHomeAddress();
		homePostalCode = contact.getHomePostalCode();
		homeCity = contact.getHomeCity();
		homeState = contact.getHomeState();
		homeCountry = contact.getHomeCountry();
		homeTelephone1 = contact.getHomeTelephone1();
		homeTelephone2 = contact.getHomeTelephone2();
		homeFax = contact.getHomeFax();
		otherAddress = contact.getOtherAddress();
		otherPostalCode = contact.getOtherPostalCode();
		otherCity = contact.getOtherCity();
		otherState = contact.getOtherState();
		otherCountry = contact.getOtherCountry();
		company = contact.getCompany();
		function = contact.getFunction();
		department = contact.getDepartment();
		manager = contact.getManager();
		assistant = contact.getAssistant();
		assistantTelephone = contact.getAssistantTelephone();
		partner = contact.getPartner();
		birthday = (contact.getBirthday() != null) ? ymdFmt.print(contact.getBirthday()) : null;
		anniversary = (contact.getAnniversary() != null) ? ymdFmt.print(contact.getAnniversary()) : null;
		url = contact.getUrl();
		notes = contact.getNotes();
	}
}
