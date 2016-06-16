/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sonicle.webtop.mail;

import com.sonicle.commons.web.json.JsonResult;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author matteo
 */
public class ColumnVisibilitySetting extends HashMap<String, Boolean> {
	
	public ColumnVisibilitySetting() {
		super();
	}
	
	public static boolean isDefault(boolean isSent, String column, boolean value) {
		if(column.equals("priority") && value) return true;
		if(column.equals("status") && value) return true;
		if(isSent) {
			if(column.equals("from") && !value) return true;
			if(column.equals("to") && value) return true;
		} else {
			if(column.equals("from") && value) return true;
			if(column.equals("to") && !value) return true;
		}
		if(column.equals("subject") && value) return true;
		if(column.equals("date") && value) return true;
		if(column.equals("gdate") && !value) return true;
		if(column.equals("size") && value) return true;
		if(column.equals("atts") && value) return true;
		if(column.equals("flag") && value) return true;
		if(column.equals("note") && value) return true;
		return false;
	}
	
	public static void applyDefaults(boolean isSent, ColumnVisibilitySetting cvs) {
		if(!cvs.containsKey("priority")) cvs.put("priority", true);
		if(!cvs.containsKey("status")) cvs.put("status", true);
		if(isSent) {
			if(!cvs.containsKey("from")) cvs.put("from", false);
			if(!cvs.containsKey("to")) cvs.put("to", true);
		} else {
			if(!cvs.containsKey("from")) cvs.put("from", true);
			if(!cvs.containsKey("to")) cvs.put("to", false);
		}
		if(!cvs.containsKey("subject")) cvs.put("subject", true);
		if(!cvs.containsKey("date")) cvs.put("date", true);
		if(!cvs.containsKey("gdate")) cvs.put("gdate", false);
		if(!cvs.containsKey("size")) cvs.put("size", true);
		if(!cvs.containsKey("atts")) cvs.put("atts", true);
		if(!cvs.containsKey("flag")) cvs.put("flag", true);
		if(!cvs.containsKey("note")) cvs.put("note", true);
	}
	
	public static ColumnVisibilitySetting fromJson(String value) {
		try {
			if(StringUtils.isEmpty(value)) return new ColumnVisibilitySetting();
			return JsonResult.GSON_PLAIN_WONULLS.fromJson(value, ColumnVisibilitySetting.class);
		} catch(Exception ex) {
			Service.logger.error("Exception",ex);
			return new ColumnVisibilitySetting();
		}
	}
	
	public static String toJson(ColumnVisibilitySetting cvs) {
		return JsonResult.GSON_PLAIN_WONULLS.toJson(cvs);
	}
}