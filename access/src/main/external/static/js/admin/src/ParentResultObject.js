/*

    Copyright 2008 The University of North Carolina at Chapel Hill

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
define('ParentResultObject', [ 'jquery', 'ResultObject'], 
		function($, ResultObject) {
	
	function ParentResultObject(options) {
		ResultObject.call(this, options);
	};
	
	ParentResultObject.prototype.constructor = ParentResultObject;
	ParentResultObject.prototype = Object.create( ResultObject.prototype );
	
	ParentResultObject.prototype.init = function(metadata) {
		this.metadata = metadata;
		this.pid = metadata.id;
		this.actionMenuInitialized = false;
		this.element = this.options.element;
		this.element.data('resultObject', this);
		this.links = [];
	};
	
	return ParentResultObject;
});