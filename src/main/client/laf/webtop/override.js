Ext.define('Sonicle.overrides.webtop.mail.MessagesPanel', {
	override: 'Sonicle.webtop.mail.MessagesPanel',
	
	privates: {
		
		createKeepFilterBtnCfg: function(cfg) {
			return Ext.apply(this.callParent(arguments), {
				enableToggleIndicator: true
			});
		}
	}
});
Ext.define('Sonicle.overrides.webtop.mail.view.MessageEditor', {
	override: 'Sonicle.webtop.mail.view.MessageEditor',
	
	privates: {
		
		createTopToolbarToggleButtonCfg: function(cfg) {
			return Ext.apply(this.callParent(arguments), {
				enableToggleIndicator: true
			});
		},
		
		createToButtonCfg: function(cfg) {
			return Ext.apply(this.callParent(arguments), {
				ui: '{secondary}',
				enableToggleIndicator: true
			});
		}
	}
});

Ext.define('Sonicle.overrides.webtop.mail.Service', {
	override: 'Sonicle.webtop.mail.Service',
	
	toolItemHeight: 161
});
