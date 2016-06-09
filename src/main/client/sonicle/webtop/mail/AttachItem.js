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

Ext.define('Sonicle.webtop.mail.AttachItem', {
	extend: 'Ext.Component',
	
	fileName: null,
	uploadId: null,
	fileSize: 0,
	msgId:null,

	initComponent : function(){
		var me=this;
		me.callParent(arguments);
	},

	onRender: function(ct, position) {
		var me=this,el=me.el;

		me.callParent(arguments);

		var xcls='x-panel-header';
		if (el) {
			el.addCls(xcls);
		} else {
			el=ct.createChild({
				id: me.id,
				cls: xcls
			},position);
		}
		el.addCls('wtmail-attach-item-normal');
		el.addClsOnOver("wtmail-attach-item-over");
		el.on('click',me.onClick,me);
	},

	afterRender: function() {
		var me=this,
			name=me.fileName,
			href=WTF.processBinUrl(me.mys.ID,"PreviewAttachment",{
				uploadId: me.uploadId
			});

		me.el.update(
				"<table border=0 cellspacing=0 cellpadding=0 style='width: 200px; table-layout: fixed; '><tr>"+
				"<td style='width:16px'><img src='"+WTF.resourceUrl(WT.ID,"/filetypes/"+WT.Util.normalizeFileType(name)+"_16.gif")+"'></td>"+
				"<td style='text-overflow: ellipsis; overflow: hidden; white-space: nowrap; padding-right: 4px;'>"+
				 "<a href='javascript:Ext.emptyFn()' "+
				 " title='"+Ext.htmlEncode(name)+"' "+
				 " onclick='Sonicle.URLMgr.open(\""+href+"\",true,\"location=no,menubar=no,resizable=yes,scrollbars=yes,status=yes,titlebar=yes,toolbar=no,top=10,left=10,width=770,height=480\");' "+
				 " class='wtmail-attach-item'>"+
				 "&nbsp;"+name+"</a>"+
				"</td>"+
				"<td style='width: 20px'><img class='x-tool-img x-tool-close'></td>"+
				"</tr></table>"
		);
		me.callParent(arguments);
	},

	onClick: function(e) {
		var me=this;
		
		if (e.getTarget(null,null,true).hasCls('x-tool-img')) {
			me.ownerCt.remove(this);
			me.fireEvent('remove',me);
		}
	}
	
});