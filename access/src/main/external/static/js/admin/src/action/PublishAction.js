define('PublishAction', ['jquery', 'AjaxCallbackAction'], function($, AjaxCallbackAction) {
		
	function PublishAction(context) {
		this.context = context;
	
		var options = {
			workDone : this.publishWorkDone,
			followup : this.publishFollowup,
			followupPath : "/services/api/status/item/{idPath}/solrRecord/version",
			workMethod : $.post
		};
		
		AjaxCallbackAction.prototype._create.call(this, options);
		
		this.performPublish = this.context.action == 'Publish';
		if (this.performPublish) {
			this.publishState();
		} else {
			this.unpublishState();
		}
	};

	PublishAction.prototype.constructor = PublishAction;
	PublishAction.prototype = Object.create(AjaxCallbackAction.prototype);

	PublishAction.prototype.publishFollowup = function(data) {
		if (data) {
			return this.options.parentObject.updateVersion(data);
		}
		return false;
	};
	
	PublishAction.prototype.completeState = function() {
		this.context.actionHandler.addEvent({
			action : 'RefreshResult',
			target : this.context.target
		});
		
		this.context.target.enable();
	};
	PublishAction.prototype.unpublishState = function() {
		this.setWorkURL("/services/api/edit/unpublish/{idPath}");
		this.options.workLabel = "Unpublishing...";
		this.options.followupLabel = "Unpublishing....";
	};

	PublishAction.prototype.publishState = function() {
		this.setWorkURL("/services/api/edit/publish/{idPath}");
		this.options.workLabel = "Publishing...";
		this.options.followupLabel = "Publishing....";
	};

	PublishAction.prototype.publishWorkDone = function(data) {
		var jsonData;
		if ($.type(data) === "string") {
			try {
				jsonData = $.parseJSON(data);
			} catch (e) {
				throw "Failed to change publication status for " + (this.options.metadata? this.options.metadata.title : this.pid);
			}
		} else {
			jsonData = data;
		}
		
		this.completeTimestamp = jsonData.timestamp;
		return true;
	};
	
	return PublishAction;
});