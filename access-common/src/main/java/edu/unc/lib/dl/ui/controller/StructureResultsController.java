/**
 * Copyright 2008 The University of North Carolina at Chapel Hill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.unc.lib.dl.ui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.unc.lib.dl.acl.util.GroupsThreadStore;
import edu.unc.lib.dl.search.solr.model.BriefObjectMetadataBean;
import edu.unc.lib.dl.search.solr.model.SimpleIdRequest;
import edu.unc.lib.dl.search.solr.model.HierarchicalBrowseResultResponse;
import edu.unc.lib.dl.ui.exception.ResourceNotFoundException;
import edu.unc.lib.dl.ui.util.SerializationUtil;

/**
 * Handles requests for JSON representations of structural results.
 * 
 * @author bbpennel
 */
@Controller
public class StructureResultsController extends AbstractStructureResultsController {
	private static final Logger LOG = LoggerFactory.getLogger(StructureResultsController.class);
	
	@RequestMapping("/structure/json")
	public @ResponseBody
	String getStructureJSON(@RequestParam(value = "files", required = false) String includeFiles, Model model,
			HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");
		HierarchicalBrowseResultResponse result = getStructureResult(null, "true".equals(includeFiles), false, false,
				request);
		return SerializationUtil.structureToJSON(result, GroupsThreadStore.getGroups());
	}

	@RequestMapping("/structure/{pid}/json")
	public @ResponseBody
	String getStructureJSON(@PathVariable("pid") String pid,
			@RequestParam(value = "files", required = false) String includeFiles, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("application/json");
		HierarchicalBrowseResultResponse result = getStructureResult(pid, "true".equals(includeFiles), false, false,
				request);
		return SerializationUtil.structureToJSON(result, GroupsThreadStore.getGroups());
	}

	/**
	 * Retrieves a composite strucuture, containing the normal structure results starting at the specified pid, merged
	 * into a list of all resources in the first tier under the parent collection, linked by any intermediary folders.
	 */
	@RequestMapping("/structure/collection")
	public @ResponseBody String getStructureFromParentCollectionJSON(
			@RequestParam(value = "files", required = false) String includeFiles, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("application/json");
		HierarchicalBrowseResultResponse result = getStructureResult(null, "true".equals(includeFiles), true, false,
				request);
		return SerializationUtil.structureToJSON(result, GroupsThreadStore.getGroups());
	}

	@RequestMapping("/structure/{pid}/collection")
	public @ResponseBody String getStructureFromParentCollectionJSON(@PathVariable("pid") String pid,
			@RequestParam(value = "files", required = false) String includeFiles, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("application/json");
		HierarchicalBrowseResultResponse result = getStructureResult(pid, "true".equals(includeFiles), true, false,
				request);
		return SerializationUtil.structureToJSON(result, GroupsThreadStore.getGroups());
	}

	/**
	 * Retrieves the structure of the contents of the parent of the specified pid.
	 */
	@RequestMapping("/structure/{pid}/parent")
	public @ResponseBody String getParentChildren(@PathVariable("pid") String pid,
			@RequestParam(value = "files", required = false) String includeFiles, Model model, HttpServletRequest request, HttpServletResponse response) {
		// Get the parent pid for the selected object and get its structure view
		BriefObjectMetadataBean selectedContainer = queryLayer.getObjectById(new SimpleIdRequest(pid,
				tierResultFieldsList));
		if (selectedContainer == null)
			throw new ResourceNotFoundException("Object " + pid + " was not found.");
		
		
		response.setContentType("application/json");
		HierarchicalBrowseResultResponse result = getStructureResult(selectedContainer.getAncestorPathFacet().getSearchKey(), "true".equals(includeFiles), false, false,
				request);
		return SerializationUtil.structureToJSON(result, GroupsThreadStore.getGroups());
	}
}