package org.task.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.task.dto.PageResponse;
import org.task.dto.filter.OrderFilter;
import org.task.dto.request.OrderRequest;
import org.task.dto.response.OrderResponse;
import org.task.service.OrderService;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/orders", "/orders"})
@Validated
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @ModelAttribute OrderFilter filter,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt"
            )
            Pageable pageable
            ) {
        return ResponseEntity.ok(orderService.findAllResponses(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable @Positive Long orderId
    ) {
        return ResponseEntity.ok(orderService.findResponseById(orderId));
    }
}
