package org.jusecase.inject.classes.example2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.inject.ComponentTest;
import org.jusecase.inject.Trainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RegisterNewsletterTest implements ComponentTest {

    @Trainer
    NewsletterGatewayTrainer newsletterGatewayTrainer;

    RegisterNewsletter registerNewsletter;

    @BeforeEach
    void setUp() {
        registerNewsletter = new RegisterNewsletter();
    }

    @Test
    void success() {
        whenEmailIsRegistered("test@example.com");
        assertThat(newsletterGatewayTrainer.isRecipient("test@example.com")).isTrue();
    }

    @Test
    void alreadyRegistered() {
        newsletterGatewayTrainer.addRecipient("test@example.com");
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered("test@example.com"));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("This email address is already registered.");
    }

    @Test
    void emptyMail() {
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered(""));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("Please enter an email address");
    }

    @Test
    void nullEmail() {
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered(null));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("Please enter an email address");
    }

    @Test
    void invalidEmail() {
        Throwable throwable = catchThrowable(() -> whenEmailIsRegistered("email"));
        assertThat(throwable).isInstanceOf(BadRequest.class).hasMessage("email is not a valid email address");
    }

    private void whenEmailIsRegistered(String email) {
        registerNewsletter.register(email);
    }
}