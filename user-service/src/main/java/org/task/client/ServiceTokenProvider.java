package org.task.client;

import org.springframework.stereotype.Component;
import org.task.service.JwtTokenService;

@Component
public class ServiceTokenProvider {
    private final JwtTokenService jwtTokenService;

    public ServiceTokenProvider(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public String token() {
        return jwtTokenService.createServiceToken();
    }
}
