package com.nettakrim.plane_advancements;

public enum LineType {
    DEFAULT,
    SMART,
    ROTATED;

    public LineType next() {
        return values()[(ordinal()+1) % values().length];
    }
}
