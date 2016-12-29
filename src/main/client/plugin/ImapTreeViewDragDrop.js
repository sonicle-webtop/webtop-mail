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

Ext.define('Sonicle.webtop.mail.plugin.ImapTreeViewDragDrop', {
    extend: 'Ext.tree.plugin.TreeViewDragDrop',
	alias: 'plugin.imaptreeviewdragdrop',
	
	ddGroup: 'mail',
	containerScroll: true,
	allowContainerDrops: false,
	
    /**
     * Override to implement moveFolder
	 *  
     * @param {String} src The source folder.
     * @param {String} dst The destination folder.
     */
	moveFolder: Ext.emptyFn,
	
    /**
     * Override to implement moveMessages
	 *  
     * @param {Object} data The drag data from MessageGrid
     * @param {String} dst The destination folder id.
     */
	moveMessages: Ext.emptyFn,
	
    /**
     * Override to implement copyMessages
	 *  
     * @param {Object} data The drag data from MessageGrid
     * @param {String} dst The destination folder id.
     */
	copyMessages: Ext.emptyFn,
	
    /**
     * Override to implement copyAttachment
	 *  
     * @param {Object} data The drag data from MessageView
     * @param {String} dst The destination folder id.
     */
	copyAttachment: Ext.emptyFn,
	
	dropZone: {
		onNodeDrop : function(targetNode, dragZone, e, data) {
			var me=this;
			
			targetNode=me.view.getRecord(targetNode); //from el node to record node
			
			switch(dragZone.ddGroup) {
				case "mail":
					if (data.records[0].isNode) {
						//data.event.dropStatus=true;
						me.ownerPlugin.moveFolder(data.records[0].id,targetNode.id);
					} else {
						if (!data.copy) {
							me.ownerPlugin.moveMessages(data, targetNode.id);
						} else {
							me.ownerPlugin.copyMessages(data, targetNode.id);
						}
						//data.event.cancel=false;
					}
					break;
					
				case "attachment":
					me.ownerPlugin.copyAttachment(data, targetNode.id);
					break;
			}
			return true;
		},
		
		onNodeOver : function(node, dragZone, e, data) {
			var ctrlKey=dragZone.ddGroup!=="attachment"?e.ctrlKey:false;
			
			Ext.tree.ViewDropZone.prototype.onNodeOver.call(this,node,dragZone,e,data);
			data.copy=ctrlKey;
			var returnCls=ctrlKey? Ext.baseCSSPrefix + 'tree-drop-ok-append': Ext.baseCSSPrefix + 'dd-drop-ok';
			this.currentCls=returnCls;
			return returnCls;
		}
		
		
	},
	
	onViewRender : function(view) {
        var me = this;
		me.callParent(arguments);
		if (me.enableDrop) {
			me.dropZone.ownerPlugin=me;
			me.dropZone.addToGroup("attachment");
		}
    }	
	
});