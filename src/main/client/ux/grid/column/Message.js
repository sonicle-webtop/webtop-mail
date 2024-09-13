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
	uses: [
		'Sonicle.String',
		'Sonicle.Bytes'
	],
	
	config: {
		threaded: false,
		sentMode: false
	},
	
	/**
	 * @cfg {left|right} [collapseToolPosition=left]
	 * Position of the expand/collapse tool for dealing with thread grouping.
	 */
	collapseToolPosition: 'left',
	
	/**
	 * @cfg {body|head} [flagIconsPosition=body]
	 * Position of the flag/tag/note icons.
	 */
	flagIconsPosition: 'body',
	
	/**
	 * @cfg {Boolean} [showSize=false]
	 * Controls whether to show or hide size data next to body icons.
	 */
	showSize: false,
	
	/**
	 * @cfg {Boolean} [stopSelection=false]
	 * Prevent grid selection upon click.
	 * Beware that if you allow for the selection to happen then the selection model will steal focus from
	 * any possible floating window (like a message box) raised in the handler. This will prevent closing the
	 * window when pressing the Escape button since it will no longer contain a focused component.
	 */
	stopSelection: true,
	
	/**
	 * @cfg {Number} [maxTags=3]
	 * The maximum number of visible tags.
	 */
	maxTags: 100,
	
	/**
	 * @cfg {Ext.data.Store} tagsStore
	 */
	tagsStore: null,
	
	/**
	 * @cfg {Function/String} collapseHandler
	 * A function called when the thread collapse/expand icon is clicked.
	 * @cfg {Ext.view.Table} handler.view The owning TableView.
	 * @cfg {Number} handler.rowIndex The row index clicked on.
	 * @cfg {Number} handler.colIndex The column index clicked on.
	 * @cfg {Event} handler.e The click event.
	 * @cfg {Ext.data.Model} handler.record The Record underlying the clicked row.
	 * @cfg {HTMLElement} handler.row The table row clicked upon.
	 */
	
	/**
	 * @cfg {Function/String} noteHandler
	 * A function called when the notes icon is clicked.
	 * @cfg {Ext.view.Table} handler.view The owning TableView.
	 * @cfg {Number} handler.rowIndex The row index clicked on.
	 * @cfg {Number} handler.colIndex The column index clicked on.
	 * @cfg {Event} handler.e The click event.
	 * @cfg {Ext.data.Model} handler.record The Record underlying the clicked row.
	 * @cfg {HTMLElement} handler.row The table row clicked upon.
	 */
	
	/**
	 * @cfg {Object} scope
	 * The scope (`this` reference) in which the `{@link #collapseHandler}`
	 * functions are executed.
	 * Defaults to this Column.
	 */
	
	alwaysShowTime: false,
	dateShortFormat: 'm/d/Y',
	dateLongFormat: 'M d Y',
	timeShortFormat: 'g:i A',
	timeLongFormat: 'H:i:s',
	
	attachmentIconCls: 'fas fa-paperclip',
	priorityIconCls: 'fas fa-exclamation',
	tagIconCls: 'fas fa-tag',
	noteIconCls: 'fas fa-sticky-note',
	collapseToolOpenIconCls: 'far fa-plus-square',
	collapseToolCloseIconCls: 'far fa-minus-square',
	collapseTooltip: 'Click to expand/collapse discussion threads',
	noteTooltip: 'Click to view annotation',
	noSubjectText: '[no subject]',
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
		complete: 'Completed',
		special: 'Special'
	},
	
	//headFloatIconSelector: '.wtmail-messagecolumn-head-float-icon',
	threadCollapseSelector: '.wtmail-messagecolumn-collapsetool',
	noteIconSelector: '.wtmail-messagecolumn-icon-note',
	
	tpl: [
		/*TODO: deprecate wtmail-grid-cell-messagecolumn*/
		'<div class="wtmail-grid-cell-messagecolumn wtmail-messagecolumn-grid-cell <tpl if="unread">wtmail-messagecolumn-grid-cell-unread</tpl>">',
			'<div class="wtmail-messagecolumn-head-float">', // head(float) -->
				
				
				'<tpl if="headFloatIcon">',
					'<div class="wtmail-messagecolumn-icon wtmail-messagecolumn-head-float-icon {headFloatIcon.iconCls}" style="height:16px;" data-qtip="{headFloatIcon.tooltip}"></div>',
				'</tpl>',
				
				
				'<tpl if="highPriority">',
					'<div class="wtmail-messagecolumn-icon">',
						'<i class="wtmail-messagecolumn-icon-priority {priorityIconCls}"></i>',
					'</div>',
				'</tpl>',
				'<tpl if="hasAttachment">',
					'<div class="wtmail-messagecolumn-icon">',
						'<i class="wtmail-messagecolumn-icon-attachment {attachmentIconCls}"></i>',
					'</div>',
				'</tpl>',
				'<div class="<tpl if="unread"> wtmail-grid-cell-messagecolumn-unread</tpl>">',
					'<span data-qtip="{date.tooltip}">{date.text}</span>',
				'</div>',
			'</div>', // <-- head(float)
			'<div class="wtmail-messagecolumn-head">', // head -->
			'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}">',
			'<tpl if="statusIconCls">',
				'<div class="wtmail-messagecolumn-icon {statusIconCls}"></div>',
			'</tpl>',
			'</div>',
			
			/*
			'<tpl if="threaded">',
				'<tpl if="collapseToolPos == \'left\'">', // collapseTool(left) -->
					'<tpl if="threadIndent == 0 && threadHasChildren">',
						'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}">',
							'<div class="wtmail-messagecolumn-collapsetool wtmail-messagecolumn-collapsetool-left" data-qtip="{collapseTooltip}">',
								'<i class="wtmail-messagecolumn-collapsetool-icon <tpl if="!threadOpen">{collapseToolOpenIconCls}<tpl else>{collapseToolCloseIconCls}</tpl>"></i>',
							'</div>',
						'</div>',
					'<tpl else>',
						'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}"></div>',
					'</tpl>',
					'<tpl if="!threadOpen && threadUnseenChildren &gt; 0">',
						'<b class="wtmail-messagecolumn-collapsetool">+{threadUnseenChildren}&nbsp;</b>',
					'</tpl>',
				'<tpl else>', // <-- collapseTool(left):else
					'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}width:0px"></div>',
				'</tpl>', // <-- collapseTool(left)
			'<tpl else>',
				'<div class="wtmail-messagecolumn-thread" style="width:0px"></div>',
			'</tpl>',
			*/
			
			/*
			'<tpl if="headIconCls">',
				'<div class="wtmail-messagecolumn-icon {headIconCls}" style="margin-right:4px;height:16px;"></div>',
			'</tpl>',
			*/
				'<span <tpl if="unread">class="wtmail-grid-cell-messagecolumn-unread"</tpl> data-qtip="{address.tooltip}">{address.text}</span>&nbsp;',
			'<tpl if="flagIconsPos == \'head\'">', // flagicons(head) -->
			'<tpl for="tags">',
				'<span class="wtmail-messagecolumn-icon" style="color:{color};" data-qtip="{tooltip}">',
					'<i class="wtmail-messagecolumn-icon-tag {parent.tagIconCls}"></i>',
				'</span>',
			'</tpl>',
			'<tpl if="flag">',
				'<div class="wtmail-messagecolumn-icon {flag.colorCls}" data-qtip="{flag.tooltip}">',
					'<i class="wtmail-messagecolumn-icon-flag {flag.glyphCls}"></i>',
				'</div>',
			'</tpl>',
			'<tpl if="star">',
				'<div class="wtmail-messagecolumn-icon {star.colorCls}" data-qtip="{star.tooltip}">',
					'<i class="wtmail-messagecolumn-icon-star {star.glyphCls}"></i>',
				'</div>',
			'</tpl>',
			'<tpl if="note">',
				'<div class="wtmail-messagecolumn-icon" data-qtip="{note.tooltip}">',
					'<i class="wtmail-messagecolumn-icon-note {noteIconCls}"></i>',
				'</div>',
			'</tpl>',
			'</tpl>', // <-- flagicons(head)
			'</div>', // <-- head
			'<div class="wtmail-messagecolumn-body-float">', // body(float) -->
			'<tpl if="flagIconsPos == \'body\'">', // flagicons(body) -->
			'<tpl for="tags">',
				'<div class="wtmail-messagecolumn-icon" style="color:{color};" data-qtip="{tooltip}">',
					'<i class="wtmail-messagecolumn-icon-tag {parent.tagIconCls}"></i>',
				'</div>',
			'</tpl>',
			'<tpl if="flag">',
				'<div class="wtmail-messagecolumn-icon {flag.colorCls}" data-qtip="{flag.tooltip}">',
					'<i class="wtmail-messagecolumn-icon-flag {flag.glyphCls}"></i>',
				'</div>',
			'</tpl>',
			'<tpl if="star">',
				'<div class="wtmail-messagecolumn-icon {star.colorCls}" data-qtip="{star.tooltip}">',
					'<i class="wtmail-messagecolumn-icon-star {star.glyphCls}"></i>',
				'</div>',
			'</tpl>',
			'<tpl if="note">',
				'<div class="wtmail-messagecolumn-icon" data-qtip="{note.tooltip}">',
					'<i class="wtmail-messagecolumn-icon-note {noteIconCls}"></i>',
				'</div>',
			'</tpl>',
			'</tpl>', // <-- flagicons(body)
			'<tpl if="showSize">',
				'<span data-qtip="{size.tooltip}">{size.text}</span>',
			'</tpl>',
			'<tpl if="threaded && collapseToolPos == \'right\'">', // collapseTool(right) -->
				'<tpl if="threadIndent == 0 && threadHasChildren">',
					'<div class="wtmail-messagecolumn-collapsetool wtmail-messagecolumn-collapsetool-right" data-qtip="{collapseTooltip}">',
					'<tpl if="!threadOpen && threadUnseenChildren &gt; 0">',
						'<span>{threadUnseenChildren}</span>',
					'</tpl>',
						'<i class="wtmail-messagecolumn-collapsetool-icon <tpl if="!threadOpen">{collapseToolOpenIconCls}<tpl else>{collapseToolCloseIconCls}</tpl>"></i>',
					'</div>',
				'</tpl>',
			'</tpl>', // <-- collapseTool(right)
			'</div>', // <-- body(float)
			'<div class="wtmail-messagecolumn-body">', // body -->
			'<tpl if="threaded">',
				'<tpl if="collapseToolPos == \'left\'">', // collapseTool(left) -->
					'<tpl if="threadIndent == 0 && threadHasChildren">',
						'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}">',
							'<div class="wtmail-messagecolumn-collapsetool wtmail-messagecolumn-collapsetool-left" data-qtip="{collapseTooltip}">',
								'<i class="wtmail-messagecolumn-collapsetool-icon <tpl if="!threadOpen">{collapseToolOpenIconCls}<tpl else>{collapseToolCloseIconCls}</tpl>"></i>',
							'</div>',
						'</div>',
					'<tpl else>',
						'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}"></div>',
					'</tpl>',
					'<tpl if="!threadOpen && threadUnseenChildren &gt; 0">',
						'<b class="wtmail-messagecolumn-collapsetool">+{threadUnseenChildren}&nbsp;</b>',
					'</tpl>',
				'<tpl else>', // <-- collapseTool(left):else
					'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}"></div>',
				'</tpl>', // <-- collapseTool(left)
			'<tpl else>',
				'<div class="wtmail-messagecolumn-thread"></div>',
			'</tpl>',
			
			/*
			'<tpl if="threaded">',
				'<div class="wtmail-messagecolumn-thread" style="{threadIndentStyle}<tpl if="collapseToolPos == \'right\'">width:0px</tpl>">',
			'<tpl else>',
				'<div class="wtmail-messagecolumn-thread" style="width:0px">',
			'</tpl>',
				'</div>',
			*/
				'<span <tpl if="unread">class="wtmail-grid-cell-messagecolumn-unread"</tpl>data-qtip="{subject.tooltip}">',
					'<tpl if="threaded && !threadOpen && threadUnseenChildren &gt; 0">',
						'<u>{subject.text}</u>',
					'<tpl else>',
						'{subject.text}',
					'</tpl>',
				'</span>',
			'</div>' // <-- body
	],
	
	constructor: function(cfg) {
		var me = this;
		me.origScope = cfg.scope || me.scope;
		me.scope = cfg.scope = null;
		//cfg.collapseToolPosition = 'right';
		me.callParent([cfg]);
	},
	
	processEvent: function(type, view, cell, recordIndex, cellIndex, e, record, row) {
		var me = this,
			isClick = type === 'click',
			disabled = me.disabled,
			ret;
		
		if (!disabled && isClick) {
			if (e.getTarget(me.threadCollapseSelector)) {
				// Flag event to tell SelectionModel not to process it.
				e.stopSelection = me.stopSelection;
				// Do not allow focus to follow from this mousedown unless the grid is already in actionable mode 
				if (isClick && !view.actionableMode) {
					e.preventDefault();
				}
				Ext.callback(me.collapseHandler, me.origScope, [view, recordIndex, cellIndex, e, record, row], undefined, me);
			
			}/* else if (e.getTarget(me.headFloatIconSelector)) {
				// Flag event to tell SelectionModel not to process it.
				e.stopSelection = me.stopSelection;
				// Do not allow focus to follow from this mousedown unless the grid is already in actionable mode 
				if (isClick && !view.actionableMode) {
					e.preventDefault();
				}
				Ext.callback(me.typeHandler, me.origScope, [view, recordIndex, cellIndex, e, record, row], undefined, me);
				
			}*/ else if (e.getTarget(me.noteIconSelector)) {
				// Flag event to tell SelectionModel not to process it.
				e.stopSelection = me.stopSelection;
				// Do not allow focus to follow from this mousedown unless the grid is already in actionable mode 
				if (isClick && !view.actionableMode) {
					e.preventDefault();
				}
				Ext.callback(me.noteHandler, me.origScope, [view, recordIndex, cellIndex, e, record, row], undefined, me);
			}
			
		} else {
			ret = me.callParent(arguments);
		}
		return ret;
	},
	
	defaultRenderer: function(val, meta, rec, ridx, cidx, sto) {
		var me = this,
			threaded = me.getThreaded(),
			addr = me.getSentMode() ? rec.get('to') : rec.get('from'),
			addremail = me.getSentMode() ? rec.get('toemail') : rec.get('fromemail'),
			data = {};
		
		Ext.apply(data, {
			collapseToolPos: me.collapseToolPosition,
			collapseToolOpenIconCls: me.collapseToolOpenIconCls,
			collapseToolCloseIconCls: me.collapseToolCloseIconCls,
			flagIconsPos: me.flagIconsPosition,
			attachmentIconCls: me.attachmentIconCls,
			priorityIconCls: me.priorityIconCls,
			tagIconCls: me.tagIconCls,
			noteIconCls: me.noteIconCls,
			threaded: threaded,
			threadOpen: threaded ? rec.get('threadOpen') : false,
			threadHasChildren: threaded ? rec.get('threadHasChildren') : false,
			threadUnseenChildren: threaded ? rec.get('threadUnseenChildren') : -1,
			threadIndent: threaded ? rec.get('threadIndent') : -1,
			threadIndentStyle: threaded ? me.buildThreadIndentStyle(rec.get('threadIndent')) : '',
			statusIconCls: me.self.buildStatusIcon(rec.get('status')),
			date: me.buildDate(sto, rec.get('istoday'), rec.get('fmtd'), rec.get('date')),
			size: me.buildSize(rec.get('size')),
			address: {text: Ext.String.htmlEncode(addr), tooltip: addremail},
			subject: me.buildSubject(rec.get('subject')),
			highPriority: rec.get('priority') < 3 ? true : false,
			hasAttachment: rec.get('atts'),
			note: rec.get('note') === true ? {tooltip: me.noteTooltip} : null,
			flag: me.self.buildFlag(rec.get('flag'), {flagsTexts: me.flagsTexts}),
			star: rec.get('starred') ? me.self.buildFlag('special', {flagsTexts: me.flagsTexts}) : null,
			tags: me.buildTags(rec.get('tags')),
			unread: rec.get('unread') === true,
			
			
			headIconCls: me.self.buildStatusIcon(rec.get('status')),
			headFloatIcon: me.self.buildTypeIcon(rec.get('pecstatus')),
			//headIconCls: me.buildStatusIcon(rec),
			//headFloatIcon: me.buildTypeIcon(rec),
			collapseTooltip: me.collapseTooltip
		});
		return this.tpl.apply(data);
	},
	
	buildThreadIndentStyle: function(indent) {
		var mright = 0;
		if (Ext.isNumber(indent)) {
			mright = indent * 15;
		}
		return 'margin-right:' + mright + 'px;';
	},
	
	buildDate: function(store, today, fmtd, date) {
		var me = this,
			textFmt, tipFmt;
		if (!fmtd && (today || (store.getGroupField && store.getGroupField() === 'gdate'))) {
			textFmt = me.timeShortFormat;
			tipFmt = me.dateShortFormat + ' ' + me.timeLongFormat;
		} else {
			textFmt = me.dateLongFormat + (me.alwaysShowTime ? ' ' + me.timeShortFormat : '');
			tipFmt = me.dateShortFormat + ' ' + me.timeLongFormat;
		}
		return {
			text: Ext.Date.format(date, textFmt),
			tooltip: Ext.Date.format(date, tipFmt)
		};
	},
	
	buildSize: function(size) {
		return {
			text: Sonicle.Bytes.format(size, {decimals: 0}),
			tooltip: Sonicle.Bytes.format(size)
		};
	},
	
	buildSubject: function(subject) {
		var sub = Ext.isEmpty(subject) ? this.noSubjectText : subject;
		//sub is already html encoded, but looks like tooltip
		//needs to be encoded twice or any html will be interpreted
		//[ see https://forum.sencha.com/forum/showthread.php?333529 ]
		return {
			text: sub,
			tooltip: Sonicle.String.htmlAttributeEncode(sub)
		};
	},
	
	buildTags: function(tags) {
		var sto = this.tagsStore,
			limit = this.maxTags,
			arr = [];
		if (sto) {
			Ext.iterate(tags, function(itm) {
				if (arr.length >= limit) return false;
				var rec = sto.findRecord('id', itm);
				if (rec) arr.push({color: rec.get('color'), tooltip: rec.get('name')});
			});
		}
		return arr;
	},
	
	statics: {
		/**
		 * Map of flag CSS classes
		 */
		flagsCls: {
			red: 'wtmail-flag-red',
			orange: 'wtmail-flag-orange',
			green: 'wtmail-flag-green',
			blue: 'wtmail-flag-blue',
			purple: 'wtmail-flag-purple',
			yellow: 'wtmail-flag-yellow',
			black: 'wtmail-flag-black',
			gray: 'wtmail-flag-gray',
			white: 'wtmail-flag-white',
			brown: 'wtmail-flag-brown',
			azure: 'wtmail-flag-azure',
			pink: 'wtmail-flag-pink',
			special: 'wtmail-flag-special',
			complete: 'wtmail-flag-complete'
		},

		/**
		 * Map of flag glyph CSS classes
		 */
		flagsGlyphs: {
			flag: 'fas fa-bookmark',
			special: 'fas fa-star',
			complete: 'fas fa-check'
		},
		
		buildFlag: function(flag, opts) {
			opts = opts || {};
			if (!Ext.isObject(opts.flagsTexts)) Ext.raise('flagsTexts property is missing');
			if (!Ext.isObject(opts.flagsCls)) opts.flagsCls = this.flagsCls;
			if (!Ext.isObject(opts.flagsGlyphs)) opts.flagsGlyphs = this.flagsGlyphs;
			if (Ext.isEmpty(flag)) return null;
			var textMap = opts.flagsTexts,
				colorClsMap = opts.flagsCls,
				glyphClsMap = opts.flagsGlyphs,
				key = Sonicle.String.removeEnd(flag, '-complete'),
				completed = Ext.String.endsWith(flag, '-complete'),
				colorCls, glyphCls, tip;

			if (completed) {
				glyphCls = glyphClsMap['complete'];
				colorCls = colorClsMap[key];
				tip = Ext.String.format('{0} ({1})', textMap[key], Sonicle.String.lower(textMap['complete']));
			} else {
				glyphCls = (['special', 'complete'].indexOf(key) !== -1) ? glyphClsMap[key] : glyphClsMap['flag'];
				colorCls = colorClsMap[key];
				tip = textMap[key];
			}
			return {glyphCls: glyphCls, colorCls: colorCls, tooltip: tip};
		},
		
		buildStatusIcon: function(status) {
			switch(status) {
				case 'new':
					//return 'fa-envelope';
					return 'wtmail-icon-status-new';
				case 'replied':
					//return 'fa-reply';
					return 'wtmail-icon-status-replied';
				case 'forwarded':
					//return 'fa-share';
					return 'wtmail-icon-status-forwarded';
				case 'repfwd':
					//return 'fa-retweet';
					return 'wtmail-icon-status-replied-forwarded';
				case 'invitation':
					return 'wtmail-icon-status-invitation';
			}
			return null;
		},

		buildTypeIcon: function(pstatus) {
			//TODO: add support other message types (eg. appointment invitation)
			switch(pstatus) {
				case 'posta-certificata':
					return {iconCls: 'wtmail-pec', tooltip: 'Messaggio PEC'};
				case 'accettazione':
					return {iconCls: 'wtmail-pec-accepted', tooltip: 'PEC (ricevuta di accettazione)'};
				case 'non-accettazione':
					return {iconCls: 'wtmail-pec-not-accepted', tooltip: 'PEC (ricevuta di NON accettazione)'};
				case 'avvenuta-consegna':
					return {iconCls: 'wtmail-pec-delivered', tooltip: 'PEC (ricevuta di consegna)'};
				case 'errore':
					return {iconCls: 'wtmail-pec-error', tooltip: 'PEC (errore)'};
			}
			return null;
		}
	}
});
