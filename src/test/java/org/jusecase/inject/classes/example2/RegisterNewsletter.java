package org.jusecase.inject.classes.example2;

import org.jusecase.inject.Component;

import javax.inject.Inject;

@Component
public class RegisterNewsletter {
    @Inject
    private NewsletterGateway newsletterGateway;
    @Inject
    private EmailValidator emailValidator;

    public void register(String email) {
        emailValidator.validate(email);
        try {
            newsletterGateway.addRecipient(email);
        } catch (DuplicateKeyException e) {
            throw new BadRequest("This email address is already registered.");
        }
    }
}
