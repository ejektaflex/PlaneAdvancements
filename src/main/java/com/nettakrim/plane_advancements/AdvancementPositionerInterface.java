package com.nettakrim.plane_advancements;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public interface AdvancementPositionerInterface {
    Vector2f planeAdvancements$getOffset();
    void planeAdvancements$setOffset(Vector2f offset);

    List<AdvancementPositionerInterface> planeAdvancements$getChildren(boolean includeThis);
    Vector3f planeAdvancements$getClusterSize();

    float planeAdvancements$getDepth();
    float planeAdvancements$getRow();
}
