Ext.define('Sonicle.overrides.webtop.mail.view.MessageEditor', {
	override: 'Sonicle.webtop.mail.view.MessageEditor',
	
	privates: {
		
		createTopToolbarToggleButtonCfg: function(cfg) {
			return Ext.apply(this.callParent(arguments), {
				hidePressedStyle: true,
				enableToggleIndicator: true
			});
		},
		
		createToButtonCfg: function(cfg) {
			return Ext.apply(this.callParent(arguments), {
				ui: '{secondary}',
				hidePressedStyle: true,
				enableToggleIndicator: true
			});
		}
	}
});