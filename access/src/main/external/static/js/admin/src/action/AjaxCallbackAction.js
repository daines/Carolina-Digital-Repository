define('AjaxCallbackAction', [ 'jquery', 'jquery-ui', 'RemoteStateChangeMonitor', 'ConfirmationDialog'], function(
		$, ui, RemoteStateChangeMonitor, ConfirmationDialog) {
			
	function AjaxCallbackAction(options) {
		this._create(options);
	};
	
	AjaxCallbackAction.prototype.defaultOptions = {
			workMethod : $.get,
			workLabel : undefined,
			workPath : "",
			workDone : undefined,
			workDoneTarget : undefined,
			followup : undefined,
			followupTarget : undefined,
			followupPath : "",
			followupLabel : undefined,
			followupFrequency : 1000,
			completeTarget : undefined,
			parentElement : undefined,
			animateSpeed : 80,
			confirm : false,
			confirmMessage : "Are you sure?",
			confirmAnchor : undefined,
			alertHandler : "#alertHandler"
		};

	AjaxCallbackAction.prototype._create = function(options) {
		this.options = $.extend({}, this.defaultOptions, options);
		
		if (this.options.workDoneTarget == undefined)
			this.options.workDoneTarget = this;
		if (this.options.completeTarget == undefined)
			this.options.completeTarget = this;
		if (this.options.followupTarget == undefined)
			this.options.followupTarget = this;
		if (this.options.setText == undefined)
			this.options.setText = this.setText;
		if (this.options.followupError == undefined)
			this.options.followupError = this.followupError;
		if (this.options.followupErrorTarget == undefined)
			this.options.followupErrorTarget = this;
		this.alertHandler = $(this.options.alertHandler);
		
		this.setWorkURL(this.options.workPath);
		
		this.followupId = null;
		this._init();
	};
	
	AjaxCallbackAction.prototype._init = function() {
		var op = this;
		
		if (this.options.followup) {
			this.setFollowupURL(this.options.followupPath);

			this.followupMonitor = new RemoteStateChangeMonitor({
				'checkStatus' : this.options.followup,
				'checkStatusTarget' : this.options.followupTarget,
				'checkError' : this.options.followupError,
				'checkErrorTarget' : this.options.followupErrorTarget,
				'statusChanged' : this.completeState,
				'statusChangedTarget' : this.options.completeTarget, 
				'checkStatusAjax' : {
					url : this.followupURL,
					dataType : 'json'
				}
			});
		}
		
		if (this.options.disabled){
			this.disable();
		} else {
			this.enable();
		}
	};
	
	AjaxCallbackAction.prototype.execute = function() {
		if (this.options.disabled)
			return;
		if (this.options.confirm) {
			var op = this;
			var dialogOptions = {
					width : 'auto',
					modal : true
				};
			if (this.options.parentObject) {
				this.options.parentObject.highlight();
				dialogOptions['close'] = function() {
					op.options.parentObject.unhighlight();
				};
			}
				
			if (this.options.confirmAnchor) {
				dialogOptions['position'] = {};
				dialogOptions['position']['of'] = this.options.confirmAnchor; 
			}
		
			var confirmationDialog = new ConfirmationDialog({
				'promptText' : this.options.confirmMessage,
				'confirmFunction' : this.doWork,
				'confirmTarget' : this,
				'dialogOptions' : dialogOptions,
				autoOpen : true
			});
		} else {
			this.doWork();
		}
	};

	AjaxCallbackAction.prototype.doWork = function(workMethod, workData) {
		if (this.options.disabled)
			return;
		this.performWork(this.options.workMethod, null);
	};

	AjaxCallbackAction.prototype.workState = function() {
		this.disable();
		if (this.options.parentObject) {
			this.options.parentObject.setState("working");
			this.options.parentObject.setStatusText(this.options.workLabel);
		}
	};

	AjaxCallbackAction.prototype.performWork = function(workMethod, workData) {
		this.workState();
		var op = this;
		workMethod(this.workURL, workData, function(data, textStatus, jqXHR) {
			if (op.options.followup) {
				if (op.options.workDone) {
					try {
						var workSuccessful = op.options.workDone.call(op.options.workDoneTarget, data);
						if (!workSuccessful)
							throw "Operation was unsuccessful";
					} catch (e) {
						op.alertHandler.alertHandler('error', e);
						if (op.options.parentObject)
							op.options.parentObject.setState("failed");
						return;
					}
				}
				if (op.options.parentObject)
					op.options.parentObject.setState("followup");
				op.followupMonitor.performPing();
			} else {
				if (op.options.parentObject)
					op.options.parentObject.setState("idle");
				if (op.options.complete)
					op.options.complete.call(op.options.completeTarget, data);
				op.enable();
			}
		}).fail(function(jqxhr, textStatus, error) {
			op.alertHandler.alertHandler('error', textStatus + ", " + error);
		});
	};
	
	AjaxCallbackAction.prototype.followupError = function(obj, errorText, error) {
		this.alertHandler.alertHandler('error', "An error occurred while checking the status of " + (this.options.metadata? this.options.metadata.title : "an object"));
		if (console && console.log)
			console.log((this.options.metadata? "Error while checking " + this.options.metadata.id + ": " : "") +errorText, error);
		if (this.options.parentObject)
			this.options.parentObject.setState("failed");
	};

	AjaxCallbackAction.prototype.disable = function() {
		this.options.disabled = true;
	};

	AjaxCallbackAction.prototype.enable = function() {
		this.options.disabled = false;
	};

	AjaxCallbackAction.prototype.setWorkURL = function(url) {
		this.workURL = url;
		this.workURL = this.resolveParameters(this.workURL);
	};

	AjaxCallbackAction.prototype.setFollowupURL = function(url) {
		this.followupURL = url;
		this.followupURL = this.resolveParameters(this.followupURL);
	};

	AjaxCallbackAction.prototype.resolveParameters = function(url) {
		if (!url || !this.pid)
			return url;
		return url.replace("{idPath}", this.pid);
	};

	AjaxCallbackAction.prototype.followupState = function() {
		if (this.options.followupLabel != null) {
			if (this.options.parentObject)
				this.options.parentObject.setStatusText(this.options.followupLabel);
		}
	};

	AjaxCallbackAction.prototype.completeState = function(data) {
		if (this.options.parentObject) {
			this.options.parentObject.setState("idle");
		}
		this.enable();
	};
	
	return AjaxCallbackAction;
});