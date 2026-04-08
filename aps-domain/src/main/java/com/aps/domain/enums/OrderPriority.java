package com.aps.domain.enums;

public enum OrderPriority {
    URGENT(1),
    HIGH(2),
    NORMAL(3),
    LOW(4);

    private final int level;

    OrderPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
