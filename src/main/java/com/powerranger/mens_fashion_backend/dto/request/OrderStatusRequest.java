package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrderStatusRequest(
    @NotBlank(message = "Status is required") String status,
    String note
) {}
