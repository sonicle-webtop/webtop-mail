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
Ext.define('Sonicle.webtop.mail.ux.grid.column.Message', {
	extend: 'Ext.grid.column.Template',
	alias: 'widget.wtmail-mailmessagecolumn',
	
	config: {
		threaded: false
	},
	
	/**
	 * @cfg {Number} [maxTags=3]
	 * The maximum number of visible tags.
	 */
	maxTags: 3,
	
	/**
	 * @cfg {Ext.data.Store} tagsStore
	 */
	tagsStore: null,
	
	/**
	 * Array of flag colors
	 */
	flagColors: {
		red: '#f44336',
		orange: '#ff9800',
		green: '#4caf50',
		blue: '#3f51b5',
		purple: '#9c27b0',
		yellow: '#ffc107',
		black: '#000000',
		gray: '#9e9e9e',
		white: '#cccccc',
		brown: '#795548',
		azure: '#03a9f4',
		pink: '#e91e63'
	},
	
	noRcptText: '[No recipient]',
	noSubjectText: '[No subject]',
	flagsTexts: {
		red: 'Red marker',
		orange: 'Orange marker',
		green: 'Green marker',
		blue: 'Blue marker',
		purple: 'Purple marker',
		yellow: 'Yellow marker',
		black: 'Black marker',
		gray: 'Gray marker',
		white: 'White marker',
		brown: 'Brown marker',
		azure: 'Azure marker',
		pink: 'Pink marker',
		completed: 'Completed',
		special: 'Special'
	},
	
	tpl: [
		'<div class="wtmail-grid-cell-messagecolumn">',
			'<div class="wtmail-messagecolumn-head-float">',
				'<span <tpl if="unread">class="wtmail-grid-cell-messagecolumn-unread"</tpl> data-qtip="{date.tooltip}">{date.text}</span>',
				'<div class="wtmail-messagecolumn-glyph" style="width:16px;text-align:center;">',
					'<tpl if="atts">',
						'<i class="fa fa-paperclip"></i>',
					'</tpl>',
				'</div>',
			'</div>',
			'<div class="wtmail-messagecolumn-head">',
				'<tpl if="threaded">',
					'<tpl if="threadIndent == 0 && threadHasChildren">',
						'<div class="wtmail-messagecolumn-thread x-grid-group-hd-collapsible <tpl if="!threadOpen">x-grid-group-hd-collapsed</tpl>" style="{threadIndentStyle}">',
							'<span class="x-grid-group-title wtmail-element-toclick" onclick="{threadOnclick}">&nbsp;</span>',
						'</div>',	
					'<tpl else>',
						'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}"></div>',
					'</tpl>',					
				'</tpl>',
				'<tpl if="headIconCls">',
					'<div class="wtmail-messagecolumn-icon {headIconCls}" style="margin-right:2px;height:16px;"></div>',
				'</tpl>',
				'<span <tpl if="unread">class="wtmail-grid-cell-messagecolumn-unread"</tpl> data-qtip="{rcpt.tooltip}">{rcpt.text}</span>',
			'</div>',
			'<div class="wtmail-messagecolumn-body-float">',
				'<tpl for="tags">',
					'<div class="wtmail-messagecolumn-glyph" style="color:{color};" data-qtip="{tooltip}">',
						'<i class="fa fa-tag"></i>',
					'</div>',
				'</tpl>',
				'<tpl if="flag">',
					'<div class="wtmail-messagecolumn-glyph" style="color:{flag.color};" data-qtip="{flag.tooltip}">',
						'<i class="fa fa-{flag.glyph}"></i>',
					'</div>',
				'</tpl>',
			'</div>',
			'<div class="wtmail-messagecolumn-body">',
				'<tpl if="threaded">',
					'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}"></div>',
				'</tpl>',
				'<tpl if="bodyIconCls">',
					'<div class="wtmail-messagecolumn-icon {bodyIconCls}" style="margin-right:2px;height:16px;"></div>',
				'</tpl>',
				'<span data-qtip="{subject}">{subject}</span>',
			'</div>',
				'<tpl if="preview">',
					'<hr>',
					'<div class="wtmail-messagecolumn-preview">{preview}</div>',
				'</tpl>',
		'</div>'
	],
	
	defaultRenderer: function(val, meta, rec, ridx, cidx, sto) {
		//TODO: display the right recipient (from or to) according to folder type
		var me = this,
				threaded = me.getThreaded(),
				data = {};
		
		Ext.apply(data, {
			threaded: threaded,
			threadOpen: threaded ? rec.get('threadOpen') : false,
			threadHasChildren: threaded ? rec.get('threadHasChildren') : false,
			threadIndent: threaded ? rec.get('threadIndent') : -1,
			threadIndentStyle: threaded ? me.buildThreadIndentStyle(rec.get('threadIndent')) : '',
			threadOnclick: threaded ? "WT.getApp().getService('com.sonicle.webtop.mail').messagesPanel.folderList.collapseClicked("+ridx+");" : '', //TODO: handle click directly!
			date: me.buildDate(sto, rec.get('istoday'), rec.get('fmtd'), rec.get('date')),
			rcpt: {text: rec.get('from'), tooltip: rec.get('fromemail')},
			subject: me.buildSubject(rec.get('subject')),
			//preview: rec.get('msgtext')
			atts: rec.get('atts'),
			flag: me.buildFlag(rec.get('flag')),
			tags: me.buildTags(rec.get('tags')),
			unread: rec.get('unread') === true,
			headIconCls: me.buildStatusIcon(rec.get('status'))
		});
		return this.tpl.apply(data);
	},
	
	buildThreadIndentStyle: function(indent) {
		var mright = 0;
		if (Ext.isNumber(indent)) {
			mright = indent * 5;
		}
		return 'margin-right:' + mright + 'px;';
	},
	
	buildSubject: function(subject) {
		return Ext.isEmpty(subject) ? this.noSubjectText : subject;
	},
	
	buildDate: function(store, today, fmtd, date) {
		var textFmt, tipFmt;
		if (!fmtd && (today || (store.getGroupField && store.getGroupField() === 'gdate'))) {
			textFmt = WT.getShortTimeFmt();
			tipFmt = WT.getShortDateFmt() + ' ' + WT.getLongTimeFmt();
		} else {
			textFmt = WT.getLongDateFmt();
			tipFmt = WT.getShortDateFmt() + ' ' + WT.getLongTimeFmt();
		}
		return {
			text: Ext.Date.format(date, textFmt),
			tooltip: Ext.Date.format(date, tipFmt)
		};
	},
	
	buildTags: function(tags) {
		var sto = this.tagsStore,
				limit = this.maxTags,
				arr = [];
		if (sto) {
			Ext.iterate(tags, function(itm) {
				if (arr.length >= limit) return false;
				var rec = sto.findRecord('tagId', itm);
				if (rec) arr.push({color: rec.get('color'), tooltip: rec.get('description')});
			});
		}
		return arr;
	},
	
	buildFlag: function(flag) {
		var txts = this.flagsTexts;
		if (Ext.isEmpty(flag)) return null;
		if (flag === 'special') {
			return {glyph: 'star', color: '#ffc107', tooltip: txts['special']};
		} else if (flag === 'complete') {
			return {glyph: 'check', color: 'initial', tooltip: txts['completed']};
		} else {
			var k = Sonicle.String.removeEnd(flag, '-complete'),
					co = this.flagColors[k];
			if (Ext.String.endsWith(flag, '-complete')) {
				return {glyph: 'check-square', color: co, tooltip: Ext.String.format('{0} ({1})', txts[k], txts['completed'].toLowerCase())};
			} else {
				return {glyph: 'bookmark', color: co, tooltip: txts[k]};
			}
		}
	},
	
	buildStatusIcon: function(status) {
		switch(status) {
			case 'new':
				return 'wtmail-icon-status-new';
			case 'replied':
				return 'wtmail-icon-status-replied';
			case 'forwarded':
				return 'wtmail-icon-status-forwarded';
			case 'repfwd':
				return 'wtmail-icon-status-replied-forwarded';
			default:
				return null;
		}
	}
});
