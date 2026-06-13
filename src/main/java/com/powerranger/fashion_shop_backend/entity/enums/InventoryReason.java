package com.powerranger.fashion_shop_backend.entity.enums;

import jakarta.persistence.EnumeratedValue;

import java.util.Arrays;

public enum InventoryReason {
    purchase("purchase"),
    sale("sale"),
    return_("return"),
    cancellation("cancellation"),
    adjustment("adjustment");

    @EnumeratedValue
    private final String value;

    InventoryReason(String value) {
        this.value = value;
    }

    public static InventoryReason fromValue(String value) {
        return Arrays.stream(values())
                .filter(reason -> reason.value.equalsIgnoreCase(value) || reason.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown inventory reason: " + value));
    }
}
