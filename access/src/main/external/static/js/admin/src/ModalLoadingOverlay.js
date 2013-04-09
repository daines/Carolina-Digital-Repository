define([ 'jquery', 'jquery-ui', 'editable', 'moment', 'qtip'], function($) {
	$.widget("cdr.modalLoadingOverlay", {
		_create : function() {
			this.overlay = $('<div class="load_modal"></div>');
			this.overlay.insertAfter(this.element);
			if (this.element != $(document)) {
				this.overlay.css({'width' : this.element.innerWidth(), 'height' : this.element.innerHeight(),
					'top' : this.element.position().top, 'left' : this.element.position().left});
			}
		},
		
		close : function() {
			this.overlay.remove();
		}
	});
});