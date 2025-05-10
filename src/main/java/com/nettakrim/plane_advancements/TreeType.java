package com.nettakrim.plane_advancements;

public enum TreeType {
    DEFAULT,
    SPRING,
    GRID;

    public TreeType next() {
        return values()[(ordinal()+1) % values().length];
    }
}
