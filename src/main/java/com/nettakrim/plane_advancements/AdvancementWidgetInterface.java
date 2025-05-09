package com.nettakrim.plane_advancements;

import net.minecraft.advancement.AdvancementDisplay;
import org.joml.Vector2f;

import java.util.List;

public interface AdvancementWidgetInterface {
    List<AdvancementWidgetInterface> planeAdvancements$getChildren();

    default Vector2f planeAdvancements$getCurrentPos() {
        return switch (PlaneAdvancementsClient.treeType) {
            case DEFAULT -> planeAdvancements$getDefaultPos();
            case SPRING -> planeAdvancements$getTreePos();
            case GRID -> planeAdvancements$getGridPos();
        };
    }
    void planeAdvancements$updatePos();

    Vector2f planeAdvancements$getDefaultPos();
    Vector2f planeAdvancements$getTreePos();
    Vector2f planeAdvancements$getGridPos();

    boolean planeAdvancements$isHovering(int mouseX, int mouseY);

    boolean planeAdvancements$isConnected(AdvancementWidgetInterface other);

    AdvancementDisplay planeAdvancements$getDisplay();

    void planeAdvancements$setGridPos(Vector2f pos);
    boolean planeAdvancements$isRoot();
}
