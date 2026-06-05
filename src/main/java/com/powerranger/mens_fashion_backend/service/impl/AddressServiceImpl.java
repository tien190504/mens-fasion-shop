package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.dto.request.AddressRequest;
import com.powerranger.mens_fashion_backend.dto.response.AddressResponse;
import com.powerranger.mens_fashion_backend.entity.Address;
import com.powerranger.mens_fashion_backend.entity.User;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.mapper.AddressMapper;
import com.powerranger.mens_fashion_backend.repository.AddressRepository;
import com.powerranger.mens_fashion_backend.repository.UserRepository;
import com.powerranger.mens_fashion_backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listByUser(String email) {
        return addressRepository.findByUserEmail(email).stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse create(String email, AddressRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setProvince(request.province());
        address.setDistrict(request.district());
        address.setWard(request.ward());
        address.setStreetAddress(request.streetAddress());
        address.setDefault(request.isDefault() != null ? request.isDefault() : false);

        if (address.isDefault()) {
            resetOtherDefaults(user.getId());
        }

        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse update(String email, Long id, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Address not found"));

        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setProvince(request.province());
        address.setDistrict(request.district());
        address.setWard(request.ward());
        address.setStreetAddress(request.streetAddress());
        address.setDefault(request.isDefault() != null ? request.isDefault() : false);

        if (address.isDefault()) {
            resetOtherDefaults(address.getUser().getId());
        }

        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(String email, Long id) {
        Address address = addressRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Address not found"));
        addressRepository.delete(address);
    }

    private void resetOtherDefaults(Long userId) {
        List<Address> addresses = addressRepository.findAll();
        for (Address addr : addresses) {
            if (addr.getUser().getId().equals(userId) && addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        }
    }
}
