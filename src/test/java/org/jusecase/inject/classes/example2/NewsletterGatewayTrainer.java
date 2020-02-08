package org.jusecase.inject.classes.example2;

import java.util.HashSet;
import java.util.Set;

public class NewsletterGatewayTrainer implements NewsletterGateway {
    private final Set<String> emails = new HashSet<>();

    @Override
    public void addRecipient(String email) throws DuplicateKeyException {
        if (isRecipient(email)) {
            throw new DuplicateKeyException("E-Mail already registered.");
        }
        emails.add(email);
    }

    @Override
    public boolean isRecipient(String email) {
        return emails.contains(email);
    }
}
