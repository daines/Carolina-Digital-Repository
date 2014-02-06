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
package edu.unc.lib.dl.data.ingest.solr.action;

import edu.unc.lib.dl.data.ingest.solr.SolrUpdateRequest;
import edu.unc.lib.dl.data.ingest.solr.exception.IndexingException;
import edu.unc.lib.dl.util.IndexingActionType;

public class CleanReindexAction extends AbstractIndexingAction {

	@Override
	public void performAction(SolrUpdateRequest updateRequest) throws IndexingException {
		SolrUpdateRequest deleteTreeRequest = new SolrUpdateRequest(updateRequest.getTargetID(),
				IndexingActionType.DELETE_SOLR_TREE, solrUpdateService.nextMessageID(), updateRequest);
		SolrUpdateRequest commitRequest = new SolrUpdateRequest(updateRequest.getTargetID(), IndexingActionType.COMMIT,
				solrUpdateService.nextMessageID(), updateRequest);
		SolrUpdateRequest recursiveAddRequest = new SolrUpdateRequest(updateRequest.getTargetID(),
				IndexingActionType.RECURSIVE_ADD, solrUpdateService.nextMessageID(), updateRequest);
		solrUpdateService.offer(deleteTreeRequest);
		solrUpdateService.offer(commitRequest);
		solrUpdateService.offer(recursiveAddRequest);
	}

}