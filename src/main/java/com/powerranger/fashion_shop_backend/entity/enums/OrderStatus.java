package com.powerranger.fashion_shop_backend.entity.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PACKING,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    RETURNED;

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (this == nextStatus) return true;
        
        return switch (this) {
            case PENDING -> nextStatus == CONFIRMED || nextStatus == CANCELLED;
            case CONFIRMED -> nextStatus == PACKING || nextStatus == CANCELLED;
            case PACKING -> nextStatus == SHIPPING || nextStatus == CANCELLED;
            case SHIPPING -> nextStatus == DELIVERED || nextStatus == CANCELLED || nextStatus == RETURNED;
            case DELIVERED -> nextStatus == RETURNED;
            case CANCELLED, RETURNED -> false;
        };
    }
}
