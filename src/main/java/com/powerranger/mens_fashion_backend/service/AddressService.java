package com.powerranger.mens_fashion_backend.service;

import com.powerranger.mens_fashion_backend.dto.request.AddressRequest;
import com.powerranger.mens_fashion_backend.dto.response.AddressResponse;
import java.util.List;

public interface AddressService {
    List<AddressResponse> listByUser(String email);
    AddressResponse create(String email, AddressRequest request);
    AddressResponse update(String email, Long id, AddressRequest request);
    void delete(String email, Long id);
}
