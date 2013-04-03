package edu.unc.lib.dl.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.unc.lib.dl.ui.controller.AbstractSolrSearchController;

@Controller
public class AccessControlController extends AbstractSolrSearchController {
	private static final Logger log = LoggerFactory.getLogger(AccessControlController.class);
	
	@RequestMapping(value = "acl/{prefix}/{id}", method = RequestMethod.GET)
	public String getAccessControl(@PathVariable("prefix") String idPrefix, @PathVariable("id") String id, Model model) {
		
		// Get access control object from fedora, transformed into XML tag.  Should sword just have this as a response type for pid/acl?
		
		// Get roleGroups from solr so that we can figure out which groups are inherited and which are direct
		
		// Could get the parent objects ACL's to use as the "inherited" info OR ask the acl fedora endpoint for the computed perms
		// RELS-EXT to provide the directly assigned roles
		
		// Inherited perms will have to be merged in the display
		// But inherited perms will have to NOT be included in the accessControl upload
		
		return "";
	}

}
