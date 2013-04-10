define([ 'jquery', 'jquery-ui', 'editable', 'moment', 'qtip'], function($) {
	$.widget("cdr.modalLoadingOverlay", {
		options : {
			'text' : null,
			'iconSize' : 'large',
			'iconPath' : '/static/images/admin/loading-small.gif',
			'autoOpen' : true
		},
		
		_create : function() {
			if (this.options.text == null)
				this.overlay = $('<div class="load_modal icon_' + (this.options.iconSize) + '"></div>');
			else {
				this.overlay = $('<div class="load_modal"></div>');
				var textSpan = $('<span>' + this.options.text + '</span>');
				this.overlay.append(textSpan);
				if (this.options.iconPath)
					this.overlay.append('<img src="' + this.options.iconPath + '" />');
			}
			if (this.options.autoOpen)
				this.show();
			else this.hide();
			
			this.overlay.appendTo(document.body);
		},
		
		close : function() {
			this.overlay.remove();
		},
		
		show : function() {
			if (this.element != $(document)) {
				this.overlay.css({'width' : this.element.innerWidth(), 'height' : this.element.innerHeight(),
					'top' : this.element.position().top, 'left' : this.element.position().left});
			}
			this.overlay.show();
		},
		
		hide : function() {
			this.overlay.hide();
		},
		
		setText : function(text) {
			this.overlay.children('span').html(text);
		}
	});
});