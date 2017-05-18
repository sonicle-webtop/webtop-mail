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
package com.sonicle.webtop.mail.bol.js;

import com.sonicle.commons.time.DateTimeUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author gabriele.bulfon
 */
public class JsSmartSearchTotals {

	public Object lock=new Object();
	
	public int viewFromMe = 0;
	public int viewToMe = 0;
	public int viewAttachments = 0;
	public int totalRows = 0;
	public int visibleRows = 0;
	public int year = 0;
	public int month = 0;
	public int day = 0;
	public ArrayList<Person> persons=new ArrayList<>();
	public ArrayList<Folder> folders=new ArrayList<>();
	public ArrayList<Message> messages=new ArrayList<>();
	public ArrayList<Year> years=new ArrayList<>();
	
	private transient HashMap<String,Person> hpersons=new HashMap<>();
	
	public boolean finished=false;
	public int progress=0;
	
	public Person addPerson(String name, String email) {
		Person p=hpersons.get(email);
		if (p==null) {
			synchronized(lock) {
				if (name==null || name.trim().length()==0) name=email;
				p=new Person(name,email);
				persons.add(p);
				hpersons.put(email,p);
			}
		}
		else p.total++;
		return p;
	}
	
	public void includePerson(String email) {
		Person p=hpersons.get(email);
		if (p!=null) p.include();
	}
	
	public void excludePerson(String email) {
		Person p=hpersons.get(email);
		if (p!=null) p.exclude();
	}
	
	public void resetPerson(String email) {
		Person p=hpersons.get(email);
		if (p!=null) p.resetType();
	}
	
	public Folder addFolder(String id, String name, int total) {
		Folder f=new Folder(id,name,total);
		synchronized(lock) {
			folders.add(f);
		}
		return f;
	}
	
	public void addMessage(long uid, String folderid, String subject, String from, String to, Date date, String text) {
		synchronized(lock) {
			messages.add(new Message(uid,folderid,subject,from,to,date,text));
		}
	}
	
	public void addMessage(long uid, String folderid, String subject, String from, String to, String date, String text) {
		synchronized(lock) {
			messages.add(new Message(uid,folderid,subject,from,to,date,text));
		}
	}
	
	public void sortByTotal() {
		synchronized(lock) {
			Collections.sort(persons, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					Person p1=((Person)o1);
					Person p2=((Person)o2);
					if (p1.type==1) {
						if (p2.type!=1) return -1;
					}
					else if (p1.type==-1) {
						if (p2.type!=-1) return -1;
					}
					else if (p1.type==0) {
						if (p2.type==-1) return -1;
						if (p2.type==1) return 1;
					}
					return p2.total-p1.total;
				}
			});
			Collections.sort(folders, new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					Folder f1=((Folder)o1);
					Folder f2=((Folder)o2);
					if (f1.type==1) {
						if (f2.type!=1) return -1;
					}
					else if (f1.type==-1) {
						if (f2.type!=-1) return -1;
					}
					else if (f1.type==0) {
						if (f2.type==-1) return -1;
						if (f2.type==1) return 1;
					}
					return f2.total-f1.total;
				}
			});
		}
	}
	
	public void sortYears() {
		synchronized(lock) {
			Collections.sort(years, new Comparator() {
				@Override
				public int compare(Object y1, Object y2) {
					return ((Year)y1).year-((Year)y2).year;
				}
			});
			for(Year y: years) {
				Collections.sort(y.months, new Comparator() {
					@Override
					public int compare(Object m1, Object m2) {
						return ((Month)m1).month-((Month)m2).month;
					}
				});
				for(Month m: y.months) {
					Collections.sort(m.days, new Comparator() {
						@Override
						public int compare(Object d1, Object d2) {
							return ((Day)d1).day-((Day)d2).day;
						}
					});
				}
			}
		}
	}
	
	public void addDate(Date d) {
		synchronized(lock) {
			DateTime ydt=new DateTime(d);
			Year y=new Year(ydt.getYear());
			int iy=years.indexOf(y);
			if (iy>=0) y=years.get(iy);
			else years.add(y);
			y.total++;
			y.addDate(ydt);
		}
	}
	
	public class Year {
		int year;
		int total=0;
		ArrayList<Month> months=new ArrayList<>();
		
		Year(int year) {
			this.year=year;
		}
		
		public void addDate(DateTime ydt) {
			synchronized(lock) {
				Month m=new Month(ydt.getMonthOfYear());
				int im=months.indexOf(m);
				if (im>=0) m=months.get(im);
				else months.add(m);
				m.total++;
				m.addDate(ydt);
			}
		}

		@Override
		public boolean equals(Object y2) {
			return year==((Year)y2).year;
		}
	
	}
	
	public class Month {
		int month;
		int total=0;
		ArrayList<Day> days=new ArrayList<>();
		
		Month(int month) {
			this.month=month;
		}
		
		@Override
		public boolean equals(Object m2) {
			return month==((Month)m2).month;
		}
	
		public void addDate(DateTime ydt) {
			synchronized(lock) {
				Day d=new Day(ydt.getDayOfMonth());
				int id=days.indexOf(d);
				if (id>=0) d=days.get(id);
				else days.add(d);
				d.total++;
			}
		}

	}
	
	public class Day {
		int day;
		int total=0;
		
		Day(int day) {
			this.day=day;
		}
		
		@Override
		public boolean equals(Object d2) {
			return day==((Day)d2).day;
		}
	
	}
	
	public class Person {
		int type=0; //1=include, -1=exclude
		String name;
		String email;
		int	total=1;
		
		Person(String name, String email) {
			this.name=name;
			this.email=email;
		}
		
		public void include() {
			type=1;
		}
		
		public void exclude() {
			type=-1;
		}
		
		public void resetType() {
			type=0;
		}
		
	}
	
	public class Folder {
		int type=0; //1=include, -1=exclude
		String id;
		String name;
		int	total;
		
		Folder(String id, String name, int total) {
			this.id=id;
			this.name=name;
			this.total=total;
		}
		
		public void include() {
			type=1;
		}
		
		public void exclude() {
			type=-1;
		}
		
		public void resetType() {
			type=0;
		}
		
	}		
	
	class Message {
		long uid;
		String folderid;
		String subject;
		String from;
		String to;
		String date;
		String text;
		
		Message(long uid, String folderid, String subject, String from, String to, Date date, String text) {
			DateTimeFormatter ymdhmsZoneFmt = DateTimeUtils.createYmdHmsFormatter();
			String sdate=ymdhmsZoneFmt.print(new DateTime(date));
			this.uid=uid;
			this.folderid=folderid;
			this.subject=subject;
			this.from=from;
			this.to=to;
			this.date=sdate;
			this.text=text;
		}
		
		Message(long uid, String folderid, String subject, String from, String to, String date, String text) {
			this.uid=uid;
			this.folderid=folderid;
			this.subject=subject;
			this.from=from;
			this.to=to;
			this.date=date;
			this.text=text;
		}
	}
}
