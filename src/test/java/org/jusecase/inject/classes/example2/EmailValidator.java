package org.jusecase.inject.classes.example2;

public class EmailValidator {

    public void validate(String email) {
        if (email == null || email.length() == 0) {
            throw new BadRequest("Please enter an email address");
        }

        if (!email.contains("@")) {
            throw new BadRequest(email + " is not a valid email address");
        }
    }
}
