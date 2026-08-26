package org.task.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.task.repositories.ReviewRepository;

@Component("securityAuthorization")
public class SecurityAuthorization {
    private final ReviewRepository reviewRepository;

    public SecurityAuthorization(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public boolean isSelf(Long userId, Authentication authentication) {
        Long authenticatedUserId = userId(authentication);
        return authenticatedUserId != null && authenticatedUserId.equals(userId);
    }

    public boolean isReviewOwner(Long reviewId, Authentication authentication) {
        Long authenticatedUserId = userId(authentication);
        if (authenticatedUserId == null) {
            return false;
        }
        return reviewRepository.findById(reviewId)
                .map(review -> review.getUserId().equals(authenticatedUserId))
                .orElse(false);
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt.getClaim("user_id");
    }
}
