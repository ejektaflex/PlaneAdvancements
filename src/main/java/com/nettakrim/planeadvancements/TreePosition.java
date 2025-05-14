package com.nettakrim.planeadvancements;

import org.joml.Vector2f;

public class TreePosition {
    public TreePosition() {
        tabPosition = new Vector2f(Float.NaN);
        globalPosition = new Vector2f(Float.NaN);
    }

    public final Vector2f tabPosition;
    public final Vector2f globalPosition;

    public Vector2f getCurrentPosition() {
        return PlaneAdvancementsClient.isMergedAndSpring() ? globalPosition : tabPosition;
    }
}
