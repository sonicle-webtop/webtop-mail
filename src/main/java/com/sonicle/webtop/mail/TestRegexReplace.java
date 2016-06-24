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

import com.sonicle.commons.RegexUtils;
import java.io.File;
import java.util.regex.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;



public class TestRegexReplace {


//	static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
	static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("([^a-zA-z0-9])");

	public static void main(String args[]) throws Exception {
        String content=FileUtils.readFileToString(new File("/export/home/gbulfon/content2.txt"), "UTF-8");
		String regex1=RegexUtils.escapeRegexSpecialChars("service-request?service=com.sonicle.webtop.mail&amp;csrf=iakyng66lcbs277m&amp;action=PreviewAttachment&amp;nowriter=true&amp;uploadId=");
		String regex2=RegexUtils.escapeRegexSpecialChars("&amp;cid=");
		String replaced=StringUtils.replacePattern(content, regex1+".{36}"+regex2, "cid:");
		regex1=RegexUtils.escapeRegexSpecialChars("service-request?service=com.sonicle.webtop.mail&csrf=iakyng66lcbs277m&action=PreviewAttachment&nowriter=true&uploadId=");
		regex2=RegexUtils.escapeRegexSpecialChars("&cid=");
		replaced=StringUtils.replacePattern(content, regex1+".{36}"+regex2, "cid:");
	}

	public static String escapeRE(String str) {
		return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");	
	}
/*	public static String escapeRE(String str) {
		//Pattern escaper = Pattern.compile("([^a-zA-z0-9])");
		//return escaper.matcher(str).replaceAll("\\\\$1");
		return str.replaceAll("([^a-zA-z0-9])", "\\\\$1");
	}*/
}
