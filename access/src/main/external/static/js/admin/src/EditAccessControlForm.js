define([ 'jquery', 'jquery-ui', 'editable', 'moment', 'qtip'], function($) {
	$.widget("cdr.editAccessControlForm", {
		
		_create : function() {
			$.fn.editable.defaults.mode = 'inline';
			this.addEmbargo = $(".add_embargo", this.element).editable({
				emptytext: 'Add embargo',
				format: 'MM/DD/YYY',
				viewformat: 'MM.DD.YYYY',
				template: 'MM/DD/YYYY',
				combodate: {
					minYear: 2012,
					maxYear: 2030,
					minuteStep: 1
				}
			});
			
			$(".roles_granted .remove_group", this.element).hide();
		}
	});
});