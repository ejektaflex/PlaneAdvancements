package com.nettakrim.planeadvancements;

import org.joml.Vector2f;

public class TreePosition {
    public TreePosition(int x, int y) {
        tabPosition = new Vector2f(x, y);
        globalPosition = new Vector2f(x, y);
    }

    public Vector2f tabPosition;
    public Vector2f globalPosition;

    public Vector2f getCurrentPosition() {
        return PlaneAdvancementsClient.isMergedAndSpring() ? globalPosition : tabPosition;
    }
}
