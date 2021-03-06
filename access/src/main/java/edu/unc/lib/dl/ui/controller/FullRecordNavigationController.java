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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.unc.lib.dl.ui.model.RecordNavigationState;
import edu.unc.lib.dl.search.solr.model.SearchState;
import edu.unc.lib.dl.search.solr.model.SearchRequest;
import edu.unc.lib.dl.search.solr.model.SearchResultResponse;
import edu.unc.lib.dl.search.solr.service.SearchStateFactory;
import edu.unc.lib.dl.search.solr.util.SearchFieldKeys;
import edu.unc.lib.dl.search.solr.util.SearchSettings;
import edu.unc.lib.dl.search.solr.util.SearchStateUtil;

/**
 * Handles navigation between full record pages, specifically the next and previous page functions.
 * Also handles the retrieve of the next page of search results if the user pages to a record outside 
 * of their last search result.
 * @author bbpennel
 */
@Controller
@RequestMapping("/recordNavigation")
public class FullRecordNavigationController extends AbstractSolrSearchController {
	private static final Logger LOG = LoggerFactory.getLogger(FullRecordNavigationController.class);
	@Autowired
	private SearchSettings searchSettings;
	
	@RequestMapping(method = RequestMethod.GET)
	public String handleRequest(Model model, HttpServletRequest request){
		RecordNavigationState recordNavigationState = (RecordNavigationState)request.getSession().getAttribute("recordNavigationState");
		
		SearchState searchState = null;
		if (recordNavigationState != null)
			searchState = recordNavigationState.getSearchState();
		
		if (searchState == null){
			String id = request.getParameter(searchSettings.searchStateParam(SearchFieldKeys.ID.name()));
			if (id == null){
				//Don't have anywhere to go, so go home
				return "redirect:/";
			} else {
				//Forward back to self since users session timed out
				model.addAttribute(searchSettings.searchStateParam(SearchFieldKeys.ID.name()), id);
				return "redirect:/record";
			}
		}
		
		List<String> resultFields = new ArrayList<String>();
		resultFields.add(SearchFieldKeys.ID.name());
		searchState.setResultFields(resultFields);
		
		SearchRequest searchRequest = generateSearchRequest(request, searchState);
		
		//Retrieve search results
		SearchResultResponse resultResponse = queryLayer.getSearchResults(searchRequest);
		recordNavigationState.setRecordIdList(resultResponse.getIdList());
		recordNavigationState.setTotalResults(resultResponse.getResultCount());
		
		//Change the currently active full record to match the next item that should be selected
		String actionsParam = request.getParameter(searchSettings.searchStateParam("ACTIONS"));
		if (actionsParam != null){
			try {
				if (actionsParam.contains(searchSettings.actionName("PREVIOUS_PAGE"))){
					recordNavigationState.setCurrentRecord(recordNavigationState.getRecordIdList().size() - 1);
				} else if (actionsParam.contains(searchSettings.actionName("NEXT_PAGE"))){
					recordNavigationState.setCurrentRecord(0);
				}
			} catch (IndexOutOfBoundsException e){
				//Tried to go to a page that doesn't exist, error page time
				return "";
			}
		}
		request.getSession().setAttribute("recordNavigationState", recordNavigationState);
		model.addAttribute(searchSettings.searchStateParam(SearchFieldKeys.ID.name()), recordNavigationState.getCurrentRecordId());

		return "redirect:/record";
	}
}
