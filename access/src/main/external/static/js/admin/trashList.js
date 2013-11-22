require.config({
	urlArgs: "v=3.4-SNAPSHOT",
	baseUrl: '/static/js/',
	paths: {
		'jquery' : 'cdr-admin',
		'jquery-ui' : 'cdr-admin',
		'text' : 'lib/text',
		'underscore' : 'lib/underscore',
		'tpl' : 'lib/tpl',
		'qtip' : 'lib/jquery.qtip.min',
		'PID' : 'cdr-admin',
		'MetadataObject' : 'cdr-admin',
		'AjaxCallbackButton' : 'cdr-admin',
		'DeleteObjectButton' : 'cdr-admin',
		'ResultObject' : 'cdr-admin',
		'ResultObjectList' : 'cdr-admin',
		'BatchCallbackButton' : 'cdr-admin',
		'DeleteBatchButton' : 'cdr-admin',
		'MoveObjectToTrashButton' : 'cdr-admin',
		'ModalLoadingOverlay' : 'cdr-admin',
		'RemoteStateChangeMonitor' : 'cdr-admin',
		'ConfirmationDialog' : 'cdr-admin',
		'AlertHandler' : 'cdr-admin',
		'SearchMenu' : 'cdr-admin',
		'ResultTableActionMenu' : 'cdr-admin',
		'ResultTableView' : 'cdr-admin',
		'MoveDropLocation' : 'cdr-admin',
		'CreateContainerForm' : 'cdr-admin',
		'AbstractFileUploadForm' : 'cdr-admin',
		'IngestPackageForm' : 'cdr-admin',
		'CreateSimpleObjectForm' : 'cdr-admin',
		'ResultObjectActionMenu' : 'cdr-admin',
		'AddMenu' : 'cdr-admin',
		'contextMenu' : 'admin/lib/jquery.contextMenu',
		'detachplus' : 'cdr-admin',
		
		'StructureEntry' : 'cdr-admin',
		'StructureView' : 'cdr-admin',
		'URLUtilities' : 'cdr-admin',
		
		'editable' : 'admin/lib/jqueryui-editable.min',
		'moment' : 'cdr-admin'
	},
	shim: {
		'jquery-ui' : ['jquery'],
		'qtip' : ['jquery'],
		'contextMenu' : ['jquery', 'jquery-ui'],
		'underscore': {
			exports: '_'
		}
	}
});

define('trashList', ['module', 'jquery', 'AlertHandler', 'SearchMenu', 'ResultTableView'], function(module, $) {
	//console.profile();
	var alertHandler = $("<div id='alertHandler'></div>");
	alertHandler.alertHandler().appendTo(document.body).hide();
	
	
	//var startTime = new Date();
	
	//console.log("Result table start: " + (new Date()).getTime());
	
	// Keep the result area locked to the size of the view port
	var $resultPage = $('.result_page');
	var $resultView = $('#result_view');
	var $columnHeaders = $('.column_headers');
	var $resultHeader = $('.result_header');
	var $resultTable = $('.result_table');
	var $window = $(window);
	var menuOffset = 360;
	var resizeResults = function () {
		var wHeight = $window.height(), wWidth = $window.width();
		$resultPage.height(wHeight - 105);
		$resultView.height(wHeight - 105);
		$resultHeader.width(wWidth - menuOffset);
		$resultTable.width(wWidth - menuOffset);
		$columnHeaders.width(wWidth - menuOffset);
	};
	resizeResults.call();
	$window.resize(resizeResults);
	
	// Keep result area the right size when the menu is resized
	var searchMenu = $("#search_menu").searchMenu({
		filterParams : module.config().filterParams,
		resultTableView : $("#result_view"),
		selectedId : module.config().container && /\w+\/uuid:[0-9a-f\-]+($|\?)/.test(document.URL)? module.config().container.id : false,
	}).on("resize", function(){
		menuOffset = searchMenu.position().left + searchMenu.innerWidth() + 40;
		resizeResults.call();
	});
	
	$(".result_area > div").resultTableView({
		metadataObjects : module.config().metadataObjects,
		container : module.config().container,
		alertHandler : alertHandler,
		resultEntryTemplate : 'tpl!../templates/admin/trashResultEntry',
		resultFields : {
			"select" : {name : "", colClass : "narrow", dataType : "index", sortField : "collection"},
			"resourceType" : {name : "", colClass : "narrow", sortField : "resourceType"},
			"title" : {name : "Title", colClass : "itemdetails", dataType : "title", sortField : "title"},
			"ancestorNames" : {name : "Original Path", colClass : "original_path", sortField : "ancestorNames"},
			"dateModified" : {name : "Modified", colClass : "date_added", sortField : "dateUpdated"}
		},
		pageNavigation : {
			resultUrl : module.config().resultUrl,
			filterParams : module.config().filterParams,
			pagingActive : module.config().pagingActive,
			pageStart : module.config().pageStart,
			pageRows : module.config().pageRows,
			resultCount : module.config().resultCount
		}
	});
	
	//console.profileEnd();
	//console.log("Result table finish " + (new Date() - startTime));
});
