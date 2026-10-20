package com.debuglab.reloop.model;

public enum UrgencyLevel {
    LOW(5),
    MEDIUM(15),
    HIGH(25),
    CRITICAL(40);

    private final int weight;

    UrgencyLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
