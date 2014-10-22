/*
* WebTop Groupware is a bundle of WebTop Services developed by Sonicle S.r.l.
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

/**
 *
 * @author gbulfon
 */
public class AdvancedSearchEntry {

    public static final int METHOD_CONTAINS=1;
    public static final int METHOD_STARTSWITH=2;
    public static final int METHOD_ENDSWITH=3;
    public static final int METHOD_IS=4;
    public static final int METHOD_ISNOT=5;
    public static final int METHOD_DOESNOTCONTAIN=6;
    public static final int METHOD_SINCE=7;
    public static final int METHOD_UPTO=8;

    String field;
    int method;
    String value;

    public AdvancedSearchEntry(String jsEntry) {
        int ix1=jsEntry.indexOf("|");
        int ix2=jsEntry.indexOf("|",ix1+1);
        String s1=jsEntry.substring(0,ix1);
        String s2=jsEntry.substring(ix1+1,ix2);
        String s3=jsEntry.substring(ix2+1);

        field=s1;
        method=s2.equals("startswith")?METHOD_STARTSWITH:
            s2.equals("endswith")?METHOD_ENDSWITH:
            s2.equals("is")?METHOD_IS:
            s2.equals("upto")?METHOD_UPTO:
            s2.equals("since")?METHOD_SINCE:
            s2.equals("isnot")?METHOD_ISNOT:
            s2.equals("!contains")?METHOD_DOESNOTCONTAIN:
            METHOD_CONTAINS;
        value=s3;
    }

    public String getField() {
        return field;
    }

    public int getMethod() {
        return method;
    }

    public String getValue() {
        return value;
    }

}
