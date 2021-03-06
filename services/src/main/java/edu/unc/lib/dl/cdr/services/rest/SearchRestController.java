package edu.unc.lib.dl.cdr.services.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.unc.lib.dl.acl.util.GroupsThreadStore;
import edu.unc.lib.dl.search.solr.model.BriefObjectMetadata;
import edu.unc.lib.dl.search.solr.model.BriefObjectMetadataBean;
import edu.unc.lib.dl.search.solr.model.SearchRequest;
import edu.unc.lib.dl.search.solr.model.SearchResultResponse;
import edu.unc.lib.dl.search.solr.model.SearchState;
import edu.unc.lib.dl.search.solr.model.SimpleIdRequest;
import edu.unc.lib.dl.ui.controller.AbstractSolrSearchController;
import edu.unc.lib.dl.ui.util.SerializationUtil;

@Controller
public class SearchRestController extends AbstractSolrSearchController {
	
	private static Pattern jsonpCleanupPattern = Pattern.compile("[^a-zA-Z0-9_$]+");

	@RequestMapping(value = "/search")
	public @ResponseBody String search(HttpServletRequest request, HttpServletResponse response) {
		return doSearch(null, request, false);
	}
	
	@RequestMapping(value = "/search/{id}")
	public @ResponseBody String search(@PathVariable("id") String id, HttpServletRequest request) {
		return doSearch(id, request, false);
	}
	
	@RequestMapping(value = "/list")
	public @ResponseBody String list(HttpServletRequest request, HttpServletResponse response) {
		return doSearch(null, request, true);
	}
	
	@RequestMapping(value = "/list/{id}")
	public @ResponseBody String list(@PathVariable("id") String id, HttpServletRequest request) {
		return doSearch(id, request, true);
	}
	
	private List<String> getResultFields(HttpServletRequest request) {
		String fields = request.getParameter("fields");
		// Allow for retrieving of specific fields
		if (fields != null) {
			String[] fieldNames = fields.split(",");
			List<String> resultFields = new ArrayList<String>();
			for (String fieldName: fieldNames) {
				String fieldKey = searchSettings.searchFieldKey(fieldName);
				if (fieldKey != null)
					resultFields.add(fieldKey);
			}
			return resultFields;
		} else {
			// Retrieve a predefined set of fields
			String fieldSet = request.getParameter("fieldSet");
			List<String> resultFields = searchSettings.resultFields.get(fieldSet);
			if (resultFields == null)
				resultFields = new ArrayList<String>(searchSettings.resultFields.get("brief"));
			return resultFields;
		}
	}
	
	private String doSearch(String pid, HttpServletRequest request, boolean applyCutoffs) {
		SearchRequest searchRequest = generateSearchRequest(request);
		searchRequest.setApplyCutoffs(applyCutoffs);
		searchRequest.setRootPid(pid);
		
		SearchState searchState = searchRequest.getSearchState();
		
		List<String> resultFields = this.getResultFields(request);
		searchState.setResultFields(resultFields);
		
		// Rollup
		String rollup = request.getParameter("rollup");
		searchState.setRollup(rollup != null && !"false".equalsIgnoreCase(rollup));
		if (searchState.getRollup() && !"true".equalsIgnoreCase(rollup)) {
			String fieldKey = searchSettings.searchFieldKey(rollup);
			if (fieldKey != null)
				searchState.setRollupField(fieldKey);
			else
				searchState.setRollup(false);
		}
		
		SearchResultResponse resultResponse = queryLayer.performSearch(searchRequest);
		if (resultResponse == null) 
			return null;
		
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("numFound", resultResponse.getResultCount());
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>(resultResponse.getResultList().size());
		for (BriefObjectMetadata metadata: resultResponse.getResultList()) {
			results.add(SerializationUtil.metadataToMap(metadata, GroupsThreadStore.getGroups()));
		}
		response.put("results", results);
		
		String callback = request.getParameter("callback");
		// If there's a jsonp callback, do some overzealous cleanup to remove any bad stuff
		if (callback != null) {
			callback = jsonpCleanupPattern.matcher(callback).replaceAll("");
			return callback + "(" + SerializationUtil.objectToJSON(response) + ")";
		}
		
		return SerializationUtil.objectToJSON(response);
	}
	
	@RequestMapping(value = "/record/{id}")
	public @ResponseBody String getSingleItem(@PathVariable("id") String id, HttpServletRequest request, HttpServletResponse response) {
		List<String> resultFields = this.getResultFields(request);
		
		SimpleIdRequest idRequest = new SimpleIdRequest(id, resultFields);
		BriefObjectMetadataBean briefObject = queryLayer.getObjectById(idRequest);
		if (briefObject == null) {
			response.setStatus(404);
			return null;
		}
		
		String callback = request.getParameter("callback");
		// If there's a jsonp callback, do some overzealous cleanup to remove any bad stuff
		if (callback != null) {
			callback = jsonpCleanupPattern.matcher(callback).replaceAll("");
			return callback + "(" + SerializationUtil.metadataToJSON(briefObject, GroupsThreadStore.getGroups()) + ")";
		}
		
		return SerializationUtil.metadataToJSON(briefObject, GroupsThreadStore.getGroups());
	}
}
