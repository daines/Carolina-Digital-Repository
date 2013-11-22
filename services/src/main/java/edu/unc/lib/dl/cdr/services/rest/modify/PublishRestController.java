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
package edu.unc.lib.dl.cdr.services.rest.modify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.unc.lib.dl.fedora.AuthorizationException;
import edu.unc.lib.dl.fedora.FedoraException;
import edu.unc.lib.dl.fedora.ManagementClient;
import edu.unc.lib.dl.fedora.PID;
import edu.unc.lib.dl.services.OperationsMessageSender;
import edu.unc.lib.dl.util.ContentModelHelper;

@Controller
public class PublishRestController {
	private static final Logger log = LoggerFactory.getLogger(PublishRestController.class);

	@Autowired(required = true)
	@Qualifier("forwardedManagementClient")
	private ManagementClient managementClient;
	@Autowired(required = true)
	private OperationsMessageSender messageSender;

	@RequestMapping(value = "edit/publish/{id}", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> publishObject(@PathVariable("id") String id, HttpServletRequest request) {
		PID pid = new PID(id);
		return this.publishObject(pid, true, request.getRemoteUser());
	}

	@RequestMapping(value = "edit/unpublish/{id}", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, ? extends Object> unpublishObject(@PathVariable("id") String id, HttpServletRequest request) {
		PID pid = new PID(id);
		return this.publishObject(pid, false, request.getRemoteUser());
	}

	private Map<String, ? extends Object> publishObject(PID pid, boolean publish, String username) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("pid", pid.getPid());
		result.put("action", (publish) ? "publish" : "unpublish");
		log.debug("Publishing object " + pid);

		try {
			// Update relation
			managementClient.setExclusiveLiteral(pid, ContentModelHelper.CDRProperty.isPublished.toString(),
					(publish) ? "yes" : "no", null);
			result.put("timestamp", System.currentTimeMillis());
			// Send message to trigger solr update
			String messageId = messageSender.sendPublishOperation(username, Arrays.asList(pid), true);
			result.put("messageId", messageId);
		} catch (AuthorizationException e) {
			result.put("error", "Insufficient privileges to publish object " + pid.getPid());
		} catch (FedoraException e) {
			log.error("Failed to update relation on " + pid, e);
			result.put("error", e.toString());
		}

		return result;
	}

	@RequestMapping(value = "edit/publish", method = RequestMethod.POST)
	public @ResponseBody
	List<? extends Object> publishObjects(@RequestParam("ids") String ids, HttpServletRequest request) {
		return publishObjects(ids, true, request.getRemoteUser());
	}

	@RequestMapping(value = "edit/unpublish", method = RequestMethod.POST)
	public @ResponseBody
	List<? extends Object> unpublishObjects(@RequestParam("ids") String ids, HttpServletRequest request) {
		return publishObjects(ids, false, request.getRemoteUser());
	}

	public List<? extends Object> publishObjects(@RequestParam("ids") String ids, boolean publish, String username) {
		if (ids == null)
			return null;
		List<Object> results = new ArrayList<Object>();
		for (String id : ids.split("\n")) {
			results.add(this.publishObject(new PID(id), publish, username));
		}
		return results;
	}
}
