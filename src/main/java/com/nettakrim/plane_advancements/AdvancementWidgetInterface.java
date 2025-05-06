package com.nettakrim.plane_advancements;

import net.minecraft.advancement.AdvancementDisplay;
import org.joml.Vector2f;

import java.util.List;

public interface AdvancementWidgetInterface {
    List<AdvancementWidgetInterface> planeAdvancements$getChildren();

    Vector2f planeAdvancements$getPos();
    void planeAdvancements$updatePos();

    boolean planeAdvancements$isHovering(int mouseX, int mouseY);

    boolean planeAdvancements$isConnected(AdvancementWidgetInterface other);

    AdvancementDisplay planeAdvancements$getDisplay();

    void planeAdvancements$setGridPos(Vector2f pos);
    boolean planeAdvancements$isRoot();
}
