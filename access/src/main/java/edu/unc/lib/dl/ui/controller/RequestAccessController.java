package edu.unc.lib.dl.ui.controller;

import java.util.HashMap;
import java.util.Map;

import net.tanesha.recaptcha.ReCaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import edu.unc.lib.dl.acl.util.GroupsThreadStore;
import edu.unc.lib.dl.search.solr.model.BriefObjectMetadataBean;
import edu.unc.lib.dl.search.solr.model.SimpleIdRequest;
import edu.unc.lib.dl.ui.model.RequestAccessForm;
import edu.unc.lib.dl.ui.service.ContactEmailService;
import edu.unc.lib.dl.ui.validator.RequestAccessFormValidator;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/requestAccess")
public class RequestAccessController extends AbstractSolrSearchController {
	private final Logger LOG = LoggerFactory.getLogger(RequestAccessController.class);

	@Autowired
	private RequestAccessFormValidator validator;
	@Autowired
	private ReCaptcha reCaptcha;
	@Autowired
	@Qualifier("requestAccessEmailService")
	private ContactEmailService emailService;

	private void setFormAttributes(String id, Model model) {
		SimpleIdRequest idRequest = new SimpleIdRequest(id, GroupsThreadStore.getGroups());
		BriefObjectMetadataBean metadata = this.queryLayer.getObjectById(idRequest);

		model.addAttribute("metadata", metadata);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String initalizeForm(Model model) {
		return this.initalizeForm(null, null, model);
	}

	@RequestMapping(value = "/{idPrefix}/{id}", method = RequestMethod.GET)
	public String initalizeForm(@PathVariable("idPrefix") String idPrefix, @PathVariable("id") String idSuffix,
			Model model) {
		String id = null;
		if (idSuffix != null)
			id = idPrefix + ":" + idSuffix;
		
		RequestAccessForm requestAccessForm = new RequestAccessForm();
		model.addAttribute("requestAccessForm", requestAccessForm);

		LOG.debug("Initializing request access form, retrieving metadata for " + id);
		if (id != null)
			this.setFormAttributes(id, model);
		model.addAttribute("reCaptcha", this.reCaptcha.createRecaptchaHtml("", "clean", null));

		return "requestAccessForm";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String submitForm(@ModelAttribute("requestAccessForm") RequestAccessForm requestAccessForm,
			BindingResult results, Model model, SessionStatus status, HttpServletRequest request) {
		return this.submitForm(requestAccessForm, results, null, null, model, status, request);
	}

	@RequestMapping(value = "/{idPrefix}/{id}", method = RequestMethod.POST)
	public String submitForm(@ModelAttribute("requestAccessForm") RequestAccessForm requestAccessForm,
			BindingResult results, @PathVariable("idPrefix") String idPrefix, @PathVariable("id") String idSuffix,
			Model model, SessionStatus status, HttpServletRequest request) {
		String id = null;
		if (idSuffix != null)
			id = idPrefix + ":" + idSuffix;

		requestAccessForm.setRemoteAddr(request.getServerName());
		model.addAttribute("requestAccessForm", requestAccessForm);

		// Validate form
		validator.validate(requestAccessForm, results);

		if (results.hasErrors()) {
			model.addAttribute("reCaptcha", this.reCaptcha.createRecaptchaHtml("", "clean", null));
			if (id != null)
				this.setFormAttributes(id, model);
			return "requestAccessForm";
		}

		// Send email
		Map<String, Object> emailProperties = new HashMap<String, Object>();
		emailProperties.put("form", requestAccessForm);
		emailProperties.put("serverName", request.getServerName());
		this.emailService.sendContactEmail(emailProperties);

		return "requestAccessForm";
	}

	public void setValidator(RequestAccessFormValidator validator) {
		this.validator = validator;
	}

	public void setEmailService(ContactEmailService emailService) {
		this.emailService = emailService;
	}
}
