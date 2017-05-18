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

import com.fluffypeople.managesieve.ManageSieveClient;
import com.fluffypeople.managesieve.ManageSieveResponse;
import com.fluffypeople.managesieve.SieveScript;
import com.sonicle.webtop.core.sdk.WTException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author malbinola
 */
public class SieveHelper {
	
	public static ManageSieveClient createSieveClient(String host, int port, String username, String password) throws WTException {
		ManageSieveClient client = null;
		ManageSieveResponse resp = null;
		
		try {
			client = new ManageSieveClient();
			resp = client.connect(host, port);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Can't connect to server. Reason: {0}", resp.getMessage()));
			resp = client.authenticate(username, password);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Could not authenticate. Reason: {0}", resp.getMessage()));
			return client;
			
		} catch(Exception ex) {
			logoutSieveClientQuietly(client);
			throw new WTException(ex, "Error initializing Sieve client [{0}, {1}, {2}]", host, port, username);
		}
	}
	
	public static void logoutSieveClientQuietly(ManageSieveClient client) {
		try { if (client != null) client.logout(); } catch(Exception ex) { /* Do nothing... */}
	}
	
	public static List<SieveScript> listScripts(ManageSieveClient client) throws WTException {
		ManageSieveResponse resp = null;
		
		try {
			List<SieveScript> scripts = new ArrayList<>();
			resp = client.listscripts(scripts);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Could not get list of scripts. Reason: {0}", resp.getMessage()));
			return scripts;
			
		} catch(Exception ex) {
			throw new WTException(ex, "Error listing Sieve scripts");
		}
	}
	
	public static String getActiveScript(ManageSieveClient client) throws WTException {
		List<SieveScript> scripts = listScripts(client);
		for (SieveScript ss : scripts) {
			if (ss.isActive()) return ss.getName();
		}
		return null;
	}
	
	public static SieveScript getScript(ManageSieveClient client, String name) throws WTException {
		ManageSieveResponse resp = null;
		
		try {
			SieveScript ss = new SieveScript();
			ss.setName(name);
			resp = client.getScript(ss);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Could not get script. Reason: {0}", resp.getMessage()));
			return ss;
			
		} catch(Exception ex) {
			throw new WTException(ex, "Error getting Sieve script [{0}]", name);
		}
	}
	
	public static void putScript(ManageSieveClient client, String name, String body) throws WTException {
		ManageSieveResponse resp = null;
		
		try {
			resp = client.putscript(name, body);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Could not put script. Reason: {0}", resp.getMessage()));
			
		} catch(Exception ex) {
			throw new WTException(ex, "Error writing Sieve script [{0}]", name);
		}
	}
	
	public static void renameScript(ManageSieveClient client, String oldName, String newName) throws WTException {
		ManageSieveResponse resp = null;
		
		try {
			resp = client.renamescript(oldName, newName);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Could not rename script. Reason: {0}", resp.getMessage()));
			
		} catch(Exception ex) {
			throw new WTException(ex, "Error renaming Sieve script [{0} -> {1}]", oldName, newName);
		}
	}
	
	public static void activateScript(ManageSieveClient client, String name) throws WTException {
		ManageSieveResponse resp = null;
		
		try {
			resp = client.setactive(name);
			if (!resp.isOk()) throw new IOException(MessageFormat.format("Unable to activate script. Reason: {0}", resp.getMessage()));
			
		} catch(Exception ex) {
			throw new WTException(ex, "Error activating Sieve script [{0}]", name);
		}
	}
}
