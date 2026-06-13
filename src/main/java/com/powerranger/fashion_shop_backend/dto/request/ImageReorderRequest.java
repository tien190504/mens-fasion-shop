package com.powerranger.fashion_shop_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ImageReorderRequest(
    @NotEmpty(message = "Images list cannot be empty") List<@Valid ImageOrderRequest> images
) {}
