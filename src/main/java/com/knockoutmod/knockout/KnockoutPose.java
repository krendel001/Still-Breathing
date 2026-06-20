package com.knockoutmod.knockout;

public enum KnockoutPose {
    LYING;

    public static KnockoutPose fromId(int id) {
        KnockoutPose[] values = values();
        if (id < 0 || id >= values.length) {
            return LYING;
        }
        return values[id];
    }
}
