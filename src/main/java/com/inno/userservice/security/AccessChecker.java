package com.inno.userservice.security;

import com.inno.userservice.repository.PaymentCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("accessChecker")
@RequiredArgsConstructor
public class AccessChecker {

    private final PaymentCardRepository paymentCardRepository;

    public boolean isSelf(Long userId) {
        AuthUser current = currentUser();
        return current != null && Objects.equals(current.userId(), userId);
    }

    public boolean ownsCard(Long cardId) {
        AuthUser current = currentUser();
        if (current == null) {
            return false;
        }

        return paymentCardRepository.findOwnerIdById(cardId)
                .map(ownerId -> Objects.equals(ownerId, current.userId()))
                .orElse(false);
    }

    private AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
            return user;
        }

        return null;
    }
}
