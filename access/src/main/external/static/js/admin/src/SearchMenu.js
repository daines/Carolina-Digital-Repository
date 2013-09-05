define('SearchMenu', [ 'jquery', 'jquery-ui', 'URLUtilities', 'StructureView'], function(
		$, ui, URLUtilities) {
	$.widget("cdr.searchMenu", {
		options : {
			filterParams : '',
			selectedId : false
		},
		
		_create : function() {
			var self = this;
			this.element.children('.query_menu').accordion({
				header: "> div > h3",
				heightStyle: "content",
				collapsible: true
			});
			this.element.children('.filter_menu').accordion({
				header: "> div > h3",
				heightStyle: "content",
				collapsible: true,
				active: false,
				beforeActivate: function(event, ui) {
					if (ui.newPanel.attr('data-href') != null && !ui.newPanel.data('contentLoaded')) {
						var isStructureBrowse = (ui.newPanel.attr('id') == "structure_facet");
						$.ajax({
							url : URLUtilities.uriEncodeParameters(ui.newPanel.attr('data-href')),
							dataType : isStructureBrowse? 'json' : null,
							success : function(data) {
								if (isStructureBrowse) {
									var $structureView = $('<div/>').html(data);
									$structureView.structureView({
										rootNode : data.root,
										showResourceIcons : true,
										showParentLink : true,
										queryPath : 'list',
										filterParams : self.options.filterParams,
										selectedId : self.options.selectedId
									});
									$structureView.addClass('inset facet');
									// Inform the result view that the structure browse is ready for move purposes
									if (self.options.resultTableView)
										self.options.resultTableView.resultTableView('addMoveDropLocation', 
											$structureView.find(".structure_content"),
											'.entry > .primary_action', 
											function($dropTarget){
												var dropObject = $dropTarget.closest(".entry_wrap").data("structureEntry");
												// Needs to be a valid container with sufficient perms
												if (!dropObject || dropObject.options.isSelected || $.inArray("addRemoveContents", dropObject.metadata.permissions) == -1)
													return false;
												return dropObject.metadata;
											});
									data = $structureView;
								}
								ui.newPanel.html(data);
								ui.newPanel.data('contentLoaded', true);
							}
						});
					}
				}
			}).accordion('option', 'active', 0);
			
			this.element.resizable({
				handles: 'e',
				alsoResize : ".structure.inset.facet",
				minWidth: 300,
				maxWidth: 600
			}).css('visibility', 'visible');
		}
	});
});