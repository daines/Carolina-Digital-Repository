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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.search.solr.model.SearchRequest;
import edu.unc.lib.dl.search.solr.model.SearchState;
import edu.unc.lib.dl.ui.model.RecordNavigationState;
import edu.unc.lib.dl.search.solr.model.SearchResultResponse;
import edu.unc.lib.dl.search.solr.util.SearchFieldKeys;
import edu.unc.lib.dl.search.solr.util.SearchStateUtil;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;

/**
 * Controller which interprets the provided search state, from either the last search state in the session or from GET
 * parameters, as well as actions performed on the state, and retrieves search results using it.
 * 
 * @author bbpennel
 */
@Controller
public class SearchActionController extends AbstractSolrSearchController {
	@Autowired(required=true)
	protected PID collectionsPid;
	private static final Logger LOG = LoggerFactory.getLogger(SearchActionController.class);

	@RequestMapping("/search/{pid}")
	public String search(@PathVariable("pid") String pid, Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);
		searchRequest.setRootPid(pid);
		searchRequest.setApplyCutoffs(false);
		model.addAttribute("queryMethod", "search");
		return search(searchRequest, model, request);
	}
	
	@RequestMapping("/search")
	public String search(Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);
		// Backwards compability with the previous search url
		if (!extractOldPathSyntax(request, searchRequest)) {
			searchRequest.setApplyCutoffs(false);
		}
		model.addAttribute("queryMethod", "search");
		return search(searchRequest, model, request);
	}
	
	private String search(SearchRequest searchRequest, Model model, HttpServletRequest request) {
		SearchResultResponse resultResponse = doSearch(searchRequest, model, request);
		// Setup parameters for full record navigation
		RecordNavigationState recordNavigationState = new RecordNavigationState();
		recordNavigationState.setSearchState(resultResponse.getSearchState());
		recordNavigationState.setSearchStateUrl((String) request.getAttribute("searchStateUrl"));
		recordNavigationState.setRecordIdList(resultResponse.getIdList());
		recordNavigationState.setTotalResults(resultResponse.getResultCount());
		request.getSession().setAttribute("recordNavigationState", recordNavigationState);
		
		model.addAttribute("resultType", "searchResults");
		model.addAttribute("pageSubtitle", "Search Results");
		
		return "searchResults";
	}
	
	@RequestMapping("/list/{pid}")
	public String list(@PathVariable("pid") String pid, Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);
		searchRequest.setRootPid(pid);
		searchRequest.setApplyCutoffs(true);
		model.addAttribute("queryMethod", "list");
		return search(searchRequest, model, request);
	}
	
	@RequestMapping("/list")
	public String list(Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);
		searchRequest.setApplyCutoffs(true);
		model.addAttribute("queryMethod", "list");
		return search(searchRequest, model, request);
	}
	
	@RequestMapping("/collections")
	public String browseCollections(Model model, HttpServletRequest request) {
		SearchRequest searchRequest = generateSearchRequest(request);
		searchRequest.setRootPid(this.collectionsPid.getPid());
		searchRequest.setApplyCutoffs(true);
		SearchState searchState = searchRequest.getSearchState();
		searchState.setResourceTypes(Arrays.asList(searchSettings.resourceTypeCollection));
		searchState.setRowsPerPage(searchSettings.defaultCollectionsPerPage);
		
		SearchResultResponse result = doSearch(searchRequest, model, request);
		result.setSelectedContainer(null);
		
		model.addAttribute("queryMethod", "collections");
		model.addAttribute("menuId", "browse");
		model.addAttribute("resultType", "collectionBrowse");
		model.addAttribute("pageSubtitle", "Browse Collections");
		return "searchResults";
	}
	
	protected SearchResultResponse doSearch(SearchRequest searchRequest, Model model, HttpServletRequest request) {
		LOG.debug("In handle search actions");
		searchRequest.setRetrieveFacets(true);

		// Request object for the search
		SearchState searchState = searchRequest.getSearchState();
		
		SearchResultResponse resultResponse = queryLayer.performSearch(searchRequest);
		
		if (resultResponse != null) {
			if (searchRequest.isRetrieveFacets()) {
				SearchRequest facetRequest = new SearchRequest(searchState, true);
				facetRequest.setApplyCutoffs(false);
				if (resultResponse.getSelectedContainer() != null) {
					SearchState facetState = (SearchState) searchState.clone();
					facetState.getFacets().put(SearchFieldKeys.ANCESTOR_PATH.name(), resultResponse.getSelectedContainer().getPath());
					facetRequest.setSearchState(facetState);
				}
				
				// Retrieve the facet result set
				SearchResultResponse resultResponseFacets = queryLayer.getFacetList(facetRequest);
				resultResponse.setFacetFields(resultResponseFacets.getFacetFields());
			}
			
			queryLayer.populateBreadcrumbs(searchRequest, resultResponse);
		}
		
		model.addAttribute("searchStateUrl", SearchStateUtil.generateStateParameterString(searchState));
		model.addAttribute("searchQueryUrl", SearchStateUtil.generateSearchParameterString(searchState));
		model.addAttribute("userAccessGroups", searchRequest.getAccessGroups());
		model.addAttribute("resultResponse", resultResponse);
		
		return resultResponse;
	}

	public void setCollectionsPid(PID collectionsPid) {
		this.collectionsPid = collectionsPid;
	}
}
