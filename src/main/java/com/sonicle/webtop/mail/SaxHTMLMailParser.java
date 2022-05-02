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
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.*;

public class SaxHTMLMailParser extends DefaultHandler implements LexicalHandler {

  private Body body=new Body();

  private PipedWriter writer=null;
  private PipedReader reader=null;
  private PrintWriter pwriter=null;
  private BufferedReader breader=null;
  private boolean isbody=false;
  private boolean isstyle=false;
  private boolean isscript=false;
  private boolean justBody=false;
  private boolean inhtml=false;
  private boolean donehtml=false;
  private String baseUrl=null;
  private String appUrl="";
  private String securityToken;
  private long msguid=-1;
  private boolean forEdit=false;
  private String provider=null;
  private String providerid=null;

  private static Vector unenclosedTags=new Vector();

  private HTMLMailData mailData=null;
  private String lastComment = null;
  private String currentEntity = null;

  static {
    unenclosedTags.addElement("IMG");
    unenclosedTags.addElement("LINK");
    unenclosedTags.addElement("BR");
    unenclosedTags.addElement("META");
    unenclosedTags.addElement("BASE");
  }
  
  public SaxHTMLMailParser(String securityToken, boolean forEdit, long msguid) {
	  this.securityToken = securityToken;
      this.msguid=msguid;
      this.forEdit=forEdit;
  }

  public SaxHTMLMailParser(String securityToken, String provider, String providerid) {
	  this.securityToken = securityToken;
      this.provider=provider; 
      this.providerid=providerid;
  }
  
  public void initialize(HTMLMailData mailData, boolean justBody) throws SAXException {
    //if (writer!=null || reader!=null) throw new SAXException("SaxHTMLMailParser yet not released!");
    release();

    this.mailData=mailData;

    writer=new PipedWriter();
    try {
      reader=new PipedReader(writer);
    } catch(IOException exc) {
      throw new SAXException(exc);
    }
    pwriter=new PrintWriter(writer);
    breader=new BufferedReader(reader);
    this.justBody=justBody;
  }
  
  public void setApplicationURL(String url) {
      this.appUrl=url;
      //Service.logger.debug("this.appUrl="+this.appUrl);
  }
  
  public BufferedReader getParsedHTML() {
    return breader;
  }

  public void release() {
    try {
      if(writer!=null) {
        writer.close();
      }
      if(reader!=null) {
        reader.close();

      }
    } catch(IOException exc) {
      Service.logger.error("Exception",exc);
    }
    writer=null;
    reader=null;
  }

  public void endOfFile() {
    try {
      writer.flush();
      writer.close();
    } catch(IOException exc) {
      Service.logger.error("Exception",exc);
    }
  }

  public void startDocument() throws SAXException {
    isbody=false;
    isstyle=false;
    isscript=false;
    baseUrl=null;
    inhtml=false;
    if (!donehtml) pwriter.print("<html>");
  }

  public void endDocument() throws SAXException {
    if (!donehtml) {
        pwriter.print("</html>");
        donehtml=true;
    }
    pwriter.flush();
  }

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (lastComment != null) {
			pwriter.print("<!--" + lastComment + "-->");
			lastComment = null;
		}
		
		if (qName.equalsIgnoreCase("html")) {
			inhtml = true;
			return;
		}
		if (!inhtml) {
			return;
		}

		//store body attributes
		if (qName.equalsIgnoreCase("body")) {
			body.setData(attributes);

		}

		//filter out any script element
		if (qName.equalsIgnoreCase("script")) {
			isscript = true;
			return;
		}

		if (!isbody) {
			/*if(!isscript&&qName.equalsIgnoreCase("script")) {
        isscript=true;
      }*/
			if (baseUrl == null && qName.equalsIgnoreCase("base")) {
				baseUrl = attributes.getValue("href");
				if (baseUrl != null) {
					if (baseUrl.toLowerCase().startsWith("file:")) {
						baseUrl = null;
						return;
					} else if (baseUrl.charAt(baseUrl.length() - 1) != '/') {
						int bx = baseUrl.lastIndexOf('/');
						int dx = baseUrl.lastIndexOf('.');
						if (dx > bx) {
							baseUrl = baseUrl.substring(0, bx + 1); //take off any wrong file spec at the end
						} else {
							baseUrl += "/"; //or append a slash
						}
					}
				}
			}
		}

		if (justBody) {
			if (!isbody) {
				if (qName.equalsIgnoreCase("body")) {
					isbody = true;
					return;
				} else if (qName.equalsIgnoreCase("style")) {
					isstyle = true;
				} else {
					return;
				}
			}
		} else if (qName.equalsIgnoreCase("style")) {
			isstyle = true;
		}

		boolean islink = false;
		boolean ismailto = false;
		boolean changedTarget = false;
		String mailtoParams = null;
		if (qName.equalsIgnoreCase("a")||qName.equalsIgnoreCase("area")) {
			islink = true;

		}
		pwriter.print("<" + qName);
		int len = attributes.getLength();
		for (int i = 0; i < len; ++i) {
			String aqname = attributes.getQName(i);
			String avalue = attributes.getValue(i);
			String laqname = aqname.toLowerCase();
			boolean isdataimg
					= laqname.equals("src")
					&& (avalue.startsWith("data:")
					|| avalue.startsWith("DATA:"));
			if (!isdataimg) {
				String lavalue = avalue.toLowerCase();
				if (laqname.endsWith("src") || laqname.equals("background")) {
					//clear any source calling /
					if (avalue.startsWith("#")) {
						avalue = "";
					} else if (avalue.toLowerCase().startsWith("cid:")) {
						avalue = evaluateCid(avalue);
					} else {
						avalue = evaluateUrl(avalue);
					}
				} else if (laqname.equals("target")) {
					avalue = "_blank";
					changedTarget = true;
				} else if (laqname.equals("href") && lavalue.startsWith("mailto:")) {
					mailtoParams = lavalue.substring(7);
					avalue = "#";
					ismailto = true;
				}
			}
			// Skip contenteditable attribute in order to avoid live editing
			if (!laqname.equals("contenteditable")) {
				pwriter.print(" " + aqname + "=\"" + StringUtils.replace(avalue, "\"", "&quot;") + "\"");
			}
		}
		if (ismailto) {
			String email=mailtoParams;
			String sparams="\""+email+"\"";
			int ix=mailtoParams.indexOf("?");
			if (ix>0) {
				email=mailtoParams.substring(0,ix);
				mailtoParams=mailtoParams.substring(ix+1);
				Map<String,String> params=getQueryMap(mailtoParams);
				String subject=params.get("subject");
				String body=params.get("body");
				if (subject==null) subject="";
				if (body==null) body="";
				sparams="\""+email+"\",\""+subject+"\",\""+body+"\"";
			}
			pwriter.print(" onclick='parent.WT.handleMailAddress(" + sparams + "); return false;'");
		} else if (islink && !changedTarget) {
			pwriter.print(" target=_blank");
		}
		pwriter.print(">");
		pwriter.flush();
	}
	
	

  public void characters(char[] chars, int start, int len) throws org.xml.sax.SAXException {
      if (!inhtml) return;

    if(justBody) {
      if(isbody) {
        //pwriter.print(MailUtils.htmlescape(chars, start, len));
		pwriter.print(MailUtils.htmlescape(new String(chars, start, len)));
        return;
      }
    }

    if(isstyle) {
      String str="";
      String pre=new String(chars, start, len);
      do {
        String xpre=pre.toLowerCase();
        int sx=xpre.indexOf("url(");
        if(sx>=0) {
          int ex=xpre.indexOf(')', sx+4);
          if (ex>sx) {
              String oldurl=pre.substring(sx+4, ex);
              String newurl=evaluateUrl(oldurl);
    //					Service.logger.debug("STYLE: substituting "+oldurl+" with "+newurl);
              str+=pre.substring(0, sx+4)+newurl+")";
              pre=pre.substring(ex+1);
          } else {
              str+=pre.substring(0,sx+4);
              pre=pre.substring(sx+4);
          }
        } else {
          str+=pre;
          break;
        }
      } while(true);
      pwriter.print(str);

    } else if(isscript) {
	  //skip any scripting
      //pwriter.write(chars, start, len);
    } else {
	  //leave html entities as they are
	  if (currentEntity==null)
		pwriter.print(MailUtils.htmlescape(new String(chars, start, len)));
	  else
		pwriter.print("&"+currentEntity+";");
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
      if (!inhtml) return;
      if (qName.equalsIgnoreCase("html")) {
          inhtml=false;
          return;
      }
    if(isscript&&qName.equalsIgnoreCase("script")) {
      isscript=false;

    }
    if (qName.equalsIgnoreCase("base") && baseUrl==null) return;

    if(justBody) {
      if(isbody) {
        if(qName.equalsIgnoreCase("body")) {
          isbody=false;
          pwriter.flush();
          try {
            writer.flush();
          } catch(IOException exc) {
          }
          return;
        }
      }

      if(!isstyle) {
        return;
      }
    }

    if(isstyle) {
      if(qName.equalsIgnoreCase("style")) {
        isstyle=false;
      }
    }

    if(isEnclosedTag(qName)) {
      pwriter.print("</"+qName+">");
    }
  }

  public void error(SAXParseException e) throws SAXException {
    Service.logger.error("Exception",e);
  }

  public void fatalError(SAXParseException e) throws SAXException {
    Service.logger.error("Exception",e);
  }

  public void warning(SAXParseException e) throws SAXException {
    Service.logger.error("Exception",e);
  }

  private String evaluateCid(String avalue) {
    String name=avalue.substring(4);
    mailData.removeUnknownPart(mailData.getCidPart(name));
    String cidUrl="";
    if (!forEdit) {
        if (appUrl!=null) cidUrl+=appUrl;
        cidUrl+="service-request?csrf="+securityToken+"&service=com.sonicle.webtop.mail&action=GetAttachment&nowriter=true";
        if (provider==null) {
            cidUrl+="&folder="+XURLEncoder.encode(mailData.getFolderName())+"&idmessage="+msguid;
                
        } else {
            cidUrl+="&provider="+provider+"&providerid="+XURLEncoder.encode(providerid);
        }
        cidUrl+="&cid="+name;
    } else {
        /*cidUrl+="service-request?csrf="+securityToken+"&service=com.sonicle.webtop.mail&action=PreviewAttachment&nowriter=true"+
            "&newmsgid="+msguid+
            "&cid="+XURLEncoder.encode(name);*/
        cidUrl=avalue;
    }
	mailData.addReferencedCid(name);
    return cidUrl;
  }

	private Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<>();
		for (String param : params) {
			String[] keys = param.split("=");
			if(keys.length >= 2){
				String name = keys[0];
				String value = keys[1];
				map.put(name, value);
			}
		}
		return map;
	}  

  private String evaluateUrl(String avalue) {
//		Service.logger.debug("evaluating url "+avalue);
    boolean islocal=mailData.conatinsUrlPart(avalue);
    if(!islocal) { //must use remote url: compose if necessary
      if(baseUrl==null) {
        return avalue;
      }
      String str=null;
      int len=avalue.length();
      if (len>3) str=avalue.substring(0, 4).toLowerCase();
      if(str!=null && (str.startsWith("http")||str.startsWith("ftp:"))) {
        return avalue;
      }
      if(len>0 && avalue.charAt(0)=='/') {
        avalue=avalue.substring(1);
      }
      return baseUrl+avalue;
    }
    mailData.removeUnknownPart(mailData.getUrlPart(avalue));
//		Service.logger.debug("Using url copy of "+avalue);
    String cidUrl="";
    if (!forEdit) {
        if (appUrl!=null) cidUrl+=appUrl;
        cidUrl+="service-request?csrf="+securityToken+"&service=com.sonicle.webtop.mail&action=GetAttachment&nowriter=true";
        if (provider==null) {
            cidUrl+="&folder="+XURLEncoder.encode(mailData.getFolderName())+"&idmessage="+msguid;
        } else {
            cidUrl+="&provider="+provider+"&providerid="+XURLEncoder.encode(providerid);
        }
        cidUrl+="&url="+XURLEncoder.encode(avalue);
    } else {
        /*cidUrl+="service-request?csrf="+securityToken+"&service=com.sonicle.webtop.mail&action=PreviewAttachment&nowriter=true"+
            "&newmsgid="+msguid+
            "&url="+XURLEncoder.encode(avalue);*/
        cidUrl=avalue;
    }
    return cidUrl;
  }

  private boolean isEnclosedTag(String name) {
    return!unenclosedTags.contains(name);
  }

  public String getBodyBGCOLOR() {
    return body.bgcolor;
  }

  public String getBodyLINK() {
    return body.link;
  }

  public String getBodyVLINK() {
    return body.vlink;
  }

  public String getBodyTEXT() {
    return body.text;
  }

	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {}

	@Override
	public void endDTD() throws SAXException {}

	@Override
	public void startEntity(String name) throws SAXException {
		currentEntity=name;
	}

	@Override
	public void endEntity(String name) throws SAXException {
		currentEntity=null;
	}

	@Override
	public void startCDATA() throws SAXException {}

	@Override
	public void endCDATA() throws SAXException {}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		final String s = new String(ch, start, length).trim();
		lastComment = StringUtils.isBlank(s) ? null : s;
	}

  class Body {
    String bgcolor;
    String link;
    String vlink;
    String text;

    void setData(Attributes attributes) {
      for(int i=0; i<attributes.getLength(); ++i) {
        String aqname=attributes.getQName(i);
        String avalue=attributes.getValue(i);
        if(aqname.equalsIgnoreCase("bgcolor")) {
          bgcolor=avalue;
        } else if(aqname.equalsIgnoreCase("link")) {
          link=avalue;
        } else if(aqname.equalsIgnoreCase("vlink")) {
          vlink=avalue;
        } else if(aqname.equalsIgnoreCase("text")) {
          text=avalue;
        }
      }
    }
  }
  
/*  class MyPrintWriter extends PrintWriter {

      MyPrintWriter(Writer w) {
          super(w);
      }

      public void print(String s) {
          System.out.print(s);
          super.print(s);
      }
      
      public void write(char[] buf, int off, int len) {
          for(int i=0;i<len;++i)
            System.out.print(buf[off+i]);
          super.write(buf,off,len);
      }
  }*/
}
