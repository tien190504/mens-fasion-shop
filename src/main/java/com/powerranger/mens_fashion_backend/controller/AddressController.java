package com.powerranger.mens_fashion_backend.controller;

import com.powerranger.mens_fashion_backend.common.ApiResponse;
import com.powerranger.mens_fashion_backend.dto.request.AddressRequest;
import com.powerranger.mens_fashion_backend.dto.response.AddressResponse;
import com.powerranger.mens_fashion_backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> listByUser(JwtAuthenticationToken jwt) {
        List<AddressResponse> response = addressService.listByUser(jwt.getName());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            JwtAuthenticationToken jwt,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.create(jwt.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Address created", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(
            JwtAuthenticationToken jwt,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.update(jwt.getName(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Address updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(JwtAuthenticationToken jwt, @PathVariable Long id) {
        addressService.delete(jwt.getName(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok("Address deleted", null));
    }
}
