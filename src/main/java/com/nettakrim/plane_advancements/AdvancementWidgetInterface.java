package com.nettakrim.plane_advancements;

import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.joml.Vector2f;

import java.util.List;

public interface AdvancementWidgetInterface {
    List<AdvancementWidget> planeAdvancements$getChildren();

    Vector2f planeAdvancements$getPos();
    void planeAdvancements$updatePos();

    boolean planeAdvancements$isHovering(int mouseX, int mouseY);

    void planeAdvancements$spring(AdvancementWidget other, float speed);
}
