define([ 'jquery', 'jquery-ui', 'editable', 'moment', 'qtip'], function($) {
	$.widget("cdr.editAccessControlForm", {
		
		_create : function() {
			var self = this;
			
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
			
			$(".boolean_toggle", this.element).click(function(){
				if ($(this).html() == "Yes") {
					$(this).html("No");
				} else { 
					$(this).html("Yes");
				}
				/*var value = $(this).data('value');
				if (value) {
					$(this).data('value', false);
					$(this).html("No");
				} else { 
					$(this).data('value', true);
					$(this).html("Yes");
				}*/
				
				return false;
			});
			
			$(".edit_role_granted a", this.element).click(function(){
				$(".roles_granted a", self.element).show();
				$(".edit_role_granted", self.element).hide();
				$(".add_role_granted", self.element).show();
				return false;
			});
			
			$(".add_group_name, .add_role_name", this.element).keypress(function(e){
				var code = (e.keyCode ? e.keyCode : e.which);
				if (code == 13) {
					$(".add_role_button", self.element).click();
					e.preventDefault();
				}
			});
			
			$(".add_role_button", this.element).click(function(){
				var roleValue = $(".add_role_name", self.element).val();
				var groupName = $.trim($(".add_group_name", self.element).val());
				if (roleValue == "" || groupName == "")
					return false;
				
				var roleRow = $("tr.role_" + roleValue, self.element);
				if (roleRow.length == 0) {
					var roleName = $(".add_role_name :selected", self.element).text();
					roleRow = $("<tr class='role_" + roleValue + "'><td class='role'>" + 
							roleName + "</td><td class='groups'></td></tr>");
					$(".edit_role_granted", self.element).before(roleRow);
				}
				
				$(".groups", roleRow).append("<span>" + groupName + "</span><a class='remove_group'>x</a><br/>");
			});
			
			$(this.element).on("click", ".roles_granted .remove_group", function(){
				$(this).prev("span").remove();
				$(this).next("br").remove();
				var parentTd = $(this).parent();
				if (parentTd.children("span").length == 0){
					parentTd.parent().remove();
				}
				$(this).remove();
			});
		}
	});
});