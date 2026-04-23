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

import jakarta.mail.*;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeUtility;
import com.sun.mail.imap.IMAPMessage;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;

public class MailTextExtractor {

    public static String buildReadable(IMAPMessage m) throws Exception {
        StringBuffer sb = new StringBuffer();

        // 1) Header del messaggio (tutti)
        appendAllHeaders(m, sb);

        // 2) Parti del messaggio
        processPart(m, sb, false);

        return sb.toString();
    }

    // === helpers ===

    private static void appendAllHeaders(Message msg, StringBuffer sb) throws MessagingException {
        sb.append("=== MESSAGE HEADERS ===\n");
        //@SuppressWarnings("unchecked")
        Enumeration<Header> e = msg.getAllHeaders();
        while (e.hasMoreElements()) {
            Header h = e.nextElement();
            sb.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        }
        sb.append("\n");
    }

    private static void processPart(Part part, StringBuffer sb, boolean insideAlternative) throws Exception {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            if (part.isMimeType("multipart/alternative")) {
                // scegliamo UNA sola parte: preferenza html > plain > qualsiasi text/*
                BodyPart bestHtml = null, bestPlain = null, bestText = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/html")) bestHtml = bp;
                    else if (bp.isMimeType("text/plain")) bestPlain = bp;
                    else if (bp.isMimeType("text/*") && bestText == null) bestText = bp;
                }
                BodyPart chosen = (bestHtml != null) ? bestHtml : (bestPlain != null ? bestPlain : bestText);
                if (chosen != null) {
                    processPart(chosen, sb, true); // dentro alternative
                }
                // tutto il resto nell'alternative viene ignorato
            } else {
                // normale multipart: cicla le sottoparti
                for (int i = 0; i < mp.getCount(); i++) {
                    processPart(mp.getBodyPart(i), sb, false);
                }
            }
            return;
        }

        // non-multipart
        if (part.isMimeType("text/*")) {
            // appendi solo il testo
            String text = readTextPart(part);
            if (text != null && !text.isEmpty()) {
                String ct = safe(part.getContentType());
                sb.append("=== TEXT PART (").append(ct).append(") ===\n");
                sb.append(text).append("\n\n");
            }
        } else if (part.isMimeType("message/rfc822")) {
            // messaggio annidato: non appendiamo il contenuto, solo le info della parte + header del messaggio interno
            appendNonTextPartHeaders(part, sb);
            Object content = part.getContent();
            if (content instanceof Message) {
                sb.append("=== ENCLOSED MESSAGE HEADERS ===\n");
                @SuppressWarnings("unchecked")
                Enumeration<Header> e = ((Message) content).getAllHeaders();
                while (e.hasMoreElements()) {
                    Header h = e.nextElement();
                    sb.append(h.getName()).append(": ").append(h.getValue()).append("\n");
                }
                sb.append("\n");
            }
        } else {
            // allegati o altre parti non testo: solo header/metadata
            appendNonTextPartHeaders(part, sb);
        }
    }

    private static void appendNonTextPartHeaders(Part part, StringBuffer sb) throws MessagingException {
        String ct = safe(part.getContentType());
        String disp = safe(part.getDisposition());
        String name = safe(part.getFileName());
        int size = part.getSize(); // puÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂ² essere -1 se sconosciuto

        sb.append("=== NON-TEXT PART ===\n");
        sb.append("Content-Type: ").append(ct).append("\n");
        if (!disp.isEmpty()) sb.append("Content-Disposition: ").append(disp).append("\n");
        if (!name.isEmpty()) sb.append("Filename: ").append(decodeMime(name)).append("\n");
        if (size >= 0) sb.append("Size: ").append(size).append(" bytes\n");
        // puoi loggare altri header specifici se vuoi:
        // es. Content-ID, Content-Description, ecc.
        sb.append("\n");
    }

    private static String readTextPart(Part part) throws Exception {
        // prova a ricavare la charset dal content-type
        String charset = null;
        try {
            ContentType ct = new ContentType(part.getContentType());
            charset = ct.getParameter("charset");
        } catch (Exception ignore) {}

        Object content = part.getContent();
        if (content instanceof String) {
            return (String) content; // JavaMail spesso lo fornisce giÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂÃÂ  decodificato
        }

        try (InputStream is = part.getInputStream()) {
            Reader r;
            if (charset != null && Charset.isSupported(charset)) {
                r = new InputStreamReader(is, charset);
            } else {
                r = new InputStreamReader(is); // fallback platform default
            }
            StringWriter w = new StringWriter();
            char[] buf = new char[8192];
            int n;
            while ((n = r.read(buf)) != -1) w.write(buf, 0, n);
            return w.toString();
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String decodeMime(String s) {
        try {
            return MimeUtility.decodeText(s);
        } catch (Exception e) {
            return s;
        }
    }
}
