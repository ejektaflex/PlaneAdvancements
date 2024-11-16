package com.nettakrim.plane_advancements;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class AdvancementCluster {
    public final Vector2f pos;
    public final Vector2f size;
    protected final float offsetY;
    protected final AdvancementPositionerInterface positioner;

    public AdvancementCluster(AdvancementPositionerInterface positioner) {
        Vector3f size = positioner.planeAdvancements$getClusterSize();

        this.pos = new Vector2f(0,0);
        this.size = new Vector2f(size.x, (size.z-size.y)+1);
        this.offsetY = -size.y;
        this.positioner = positioner;
    }

    public void applyPosition() {
        pos.y += offsetY;
        positioner.planeAdvancements$setOffset(pos);
    }
}
