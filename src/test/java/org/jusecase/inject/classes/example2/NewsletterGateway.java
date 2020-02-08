package org.jusecase.inject.classes.example2;

public interface NewsletterGateway {
    void addRecipient(String email) throws DuplicateKeyException;
    boolean isRecipient(String email);
}
