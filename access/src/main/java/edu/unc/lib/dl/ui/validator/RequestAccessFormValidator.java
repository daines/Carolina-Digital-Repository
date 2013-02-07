package edu.unc.lib.dl.ui.validator;

import java.util.regex.Pattern;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.ValidationUtils;

import edu.unc.lib.dl.ui.model.RequestAccessForm;

public class RequestAccessFormValidator implements Validator {
	private final Logger LOG = LoggerFactory.getLogger(RequestAccessFormValidator.class);
	
	private static Pattern emailRegex = Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}\\b");
	@Autowired
	private ReCaptcha reCaptcha;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return RequestAccessForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "personalName", "required.personalName");
		RequestAccessForm form = (RequestAccessForm)target;
		if (!emailRegex.matcher(form.getEmailAddress()).matches()) {
			errors.rejectValue("emailAddress", "invalid.emailAddress");
		}
		ReCaptchaResponse response = reCaptcha.checkAnswer(form.getRemoteAddr(), form.getRecaptcha_challenge_field(), form.getRecaptcha_response_field());
		
		if (!response.isValid()) {
			LOG.debug("Recaptcha validation failed because: " + response.getErrorMessage());
			errors.rejectValue("recaptcha_challenge_field", "incorrect.recaptcha");
		}
	}
}
