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

Ext.define('Sonicle.webtop.mail.SimpleImapTree', {
	extend: 'Ext.tree.Panel',
	requires: [
		'Sonicle.webtop.mail.model.ImapTreeModel'
	],
	
	autoScroll: true,
	
	selModel: {
		ignoreRightMouseSelection: true
	},
		
	useArrows: true,
	rootVisible: false,

	constructor: function(cfg) {
		var me = this;
		
		cfg=cfg||{};

		Ext.apply(cfg,{
			columns: {
				items: [
					{
						xtype: 'treecolumn', //this is so we know which column will show the tree
						text: cfg.mys.res("column-folder"),
						dataIndex: 'folder',
						flex: 3,
						sortable: false,
						renderer: function(v,p,r) {
							if(r.get('isReadOnly')) {
								return "<span style='" + 'color:grey;' + "'>" + v + "</span>";
							}
							var unr=r.get('unread'),
								hunr=r.get('hasUnread'),
								id=r.get('id');
							return (unr!==0||hunr?'<span style="font-weight:bold" data-qtip="'+id+'">'+v+'</span>':v);
						},
						editor: 'textfield'
					},
					{
						header: WTF.headerWithGlyphIcon('far fa-eye'),
						dataIndex: 'unread',
						align: 'right',
						flex: 1,
						sortable: false,
						renderer: function(v,p,r) {
							return (v===0?'':'<span style="font-weight:bold">'+v+'</span>');
						}
					},
					{
						xtype: 'soiconcolumn',
						header: WTF.headerWithGlyphIcon('fas fa-share-alt'),
						dataIndex: 'isSharedToSomeone',
						flex: 1,
						sortable: false,
						hidden: true,
						getIconCls: function(value,rec) {
							return value ? 'wtmail-icon-folderStatus-shared' : '';
						},
						handler: function(grid, rix, cix, e, rec) {
							me.mys.showSharingView(rec);
						},
						scope: me
					}

			  ]
			}
		});
		
		me.callParent([cfg]);
	},
	
	showSharings: function(b) {
		this.getColumns()[2].setHidden(!b);
	},
	
	expandNodePath: function(id,sep,select,node,cb) {
		var me=this;
	
		if (!node) node=me.getRootNode();
		
		Ext.each(node.childNodes,function(cn,cx,an) {
			if (Ext.String.startsWith(id, cn.id+sep)) {
				cn.expand(false,function() {
					me.expandNodePath(id,sep,select,cn,cb);
				});
			}
			else if (id===cn.id) {
				if (select) me.selModel.select(cn);
				if (cb) Ext.callback(cb,me);
			}
		});
	}
	
});

