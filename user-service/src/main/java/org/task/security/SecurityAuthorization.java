package org.task.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.task.repositories.OrderRepository;

@Component("securityAuthorization")
public class SecurityAuthorization {
    private final OrderRepository orderRepository;

    public SecurityAuthorization(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public boolean isSelf(Long userId, Authentication authentication) {
        Long authenticatedUserId = userId(authentication);
        return authenticatedUserId != null && authenticatedUserId.equals(userId);
    }

    public boolean isOrderOwner(Long orderId, Authentication authentication) {
        Long authenticatedUserId = userId(authentication);
        if (authenticatedUserId == null) {
            return false;
        }
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(authenticatedUserId))
                .orElse(false);
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt.getClaim("user_id");
    }
}
