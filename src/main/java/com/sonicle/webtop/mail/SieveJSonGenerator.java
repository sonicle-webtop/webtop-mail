/*
* WebTop Groupware is a bundle of WebTop Services developed by Sonicle S.r.l.
* Copyright (C) 2011 Sonicle S.r.l.
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

import com.sonicle.commons.MailUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

import com.sonicle.mail.sieve.*;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author gbulfon
 */
public class SieveJSonGenerator implements MailFiltersParserListener {

        String context;
        StringBuffer json=new StringBuffer();
        Service ms;
        Locale locale;
        String prefix;

        public SieveJSonGenerator(String context, Service ms, Locale locale, String folderPrefix) {
            this.context=context;
            this.ms=ms;
            this.locale=locale;
            this.prefix=folderPrefix;
        }

        public StringBuffer generate(MailFilters filters) throws SQLException {
            json.append("{\n");
            MailFiltersParser.parse(filters,this);
            return json;
        }

        public void filtersStart() {
            json.append("  filters: [\n");
        }

        public void filter(int row, MailFilterConditions mfcs) {
            if (row>0) json.append(",\n");
            
			int idfilter=mfcs.getIDFilter();
			boolean enabled=mfcs.isEnabled();
			String operator=mfcs.getOperator();
			String action=mfcs.getAction();
			String actionvalue=mfcs.getActionValue();
			
            int mfcssize=mfcs.size();
            String condition=ms.lookupResource(locale,MailLocaleKey.FILTERS_IF)+" ";
            for(int i=0;i<mfcssize;++i) {
                if (i>0) condition+=" "+ms.lookupResource(locale,MailLocaleKey.FILTERS_OPERATOR(operator))+" ";
                MailFilterCondition mfc=mfcs.get(i);
                condition+=ms.lookupResource(locale,MailLocaleKey.FILTERS_FIELD(mfc.getField()))+" ";
                condition+=ms.lookupResource(locale,MailLocaleKey.FILTERS_COMPARISON(mfc.getStringComparison()))+" ";
                ArrayList<String> values=mfc.getValues();
                int vsize=values.size();
                if (vsize>1) condition+="(";
                for(int v=0;v<vsize;++v) {
                    if (v>0) condition+=" "+ms.lookupResource(locale,MailLocaleKey.FILTERS_OR)+" ";
                    String value=values.get(v);
                    condition+="\""+value+"\"";
                }
                if (vsize>1) condition+=")";
            }
            condition=StringEscapeUtils.escapeEcmaScript(condition);

            String saction=StringEscapeUtils.escapeEcmaScript(ms.lookupResource(locale, MailLocaleKey.FILTERS_ACTION(action.toLowerCase())));
            String sactionvalue=" ";
            if (actionvalue!=null && actionvalue.trim().length()>0) {
                if (action.equals("FILE")) {
                    if (actionvalue.startsWith(prefix)) sactionvalue=actionvalue.substring(prefix.length());
                } else if (action.equals("REJECT")) {
                    sactionvalue=actionvalue;
                }
            }
            sactionvalue=StringEscapeUtils.escapeEcmaScript(sactionvalue);

            json.append("    { idfilter: "+idfilter+", row: "+(row+1)+", active: "+enabled+", description: '"+condition+"', action: '"+saction+"', value: '"+sactionvalue+"'}");
        }

        public void filtersEnd(int rows) {
            json.append("  ],\n");
            json.append("  totalCount: "+rows+"\n");
            json.append("}\n");
        }
        
        public void vacation(boolean active, String message, String addresses) {
/*            if (message!=null) {
                json.append("  vactive: "+active+",\n");
                json.append("  vmessage: '"+Utils.jsEscape(message)+"',\n");
                json.append("  vaddresses: '"+Utils.jsEscape(addresses)+"'\n");
            } else {
                json.append("  vactive: false,\n");
                json.append("  vmessage: '',\n");
                json.append("  vaddresses: ''\n");
            }
            json.append("}\n");*/
        }
		
}
