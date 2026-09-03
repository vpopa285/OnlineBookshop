package org.task.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.task.service.JwtTokenService;

@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {
    private final JwtTokenService jwtTokenService;

    public String token() {
        return jwtTokenService.createServiceToken();
    }
}
