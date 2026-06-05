package com.powerranger.mens_fashion_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
    @NotBlank(message = "Recipient name is required") String recipientName,
    @NotBlank(message = "Recipient phone is required") String phone,
    @NotBlank(message = "Province is required") String province,
    @NotBlank(message = "District is required") String district,
    String ward,
    @NotBlank(message = "Street address is required") String streetAddress,
    Boolean isDefault
) {}
