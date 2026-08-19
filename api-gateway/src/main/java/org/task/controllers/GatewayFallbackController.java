package org.task.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayFallbackController {
    @RequestMapping("/fallback/book-service")
    public ResponseEntity<String> bookServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("book-service is temporarily unavailable");
    }

    @RequestMapping("/fallback/user-service")
    public ResponseEntity<String> userServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("user-service is temporarily unavailable");
    }
}
