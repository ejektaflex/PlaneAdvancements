package com.nettakrim.plane_advancements;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Stack;

public class AdvancementCluster {
    public final Vector2f pos;
    public final Vector2f size;
    protected final float offsetY;
    protected final AdvancementWidgetInterface root;

    public AdvancementCluster(AdvancementWidgetInterface root) {
        Vector3f size = getClusterSize(root);

        this.pos = new Vector2f(0,0);
        this.size = new Vector2f(size.x, (size.z-size.y)+1);
        this.offsetY = -size.y;
        this.root = root;
    }

    public static Vector3f getClusterSize(AdvancementWidgetInterface root) {
        if (root.planeAdvancements$isRoot()) {
            return new Vector3f(1,0,0);
        }

        float widthMax = 1;
        float heightMax = 0;
        float heightMin = 0;

        Stack<AdvancementWidgetInterface> stack = new Stack<>();
        stack.addAll(root.planeAdvancements$getChildren());
        while (!stack.isEmpty()) {
            AdvancementWidgetInterface advancement = stack.pop();
            stack.addAll(advancement.planeAdvancements$getChildren());

            float width = advancement.planeAdvancements$getDisplay().getX();
            if (width > widthMax) {
                widthMax = width;
            }

            float height = advancement.planeAdvancements$getDisplay().getY()-root.planeAdvancements$getDisplay().getY();
            if (height > heightMax) {
                heightMax = height;
            }
            if (height < heightMin) {
                heightMin = height;
            }
        }

        return new Vector3f(widthMax, heightMin, heightMax);
    }

    public void applyPosition() {
        pos.y += offsetY;

        pos.mul(28, 27);
        pos.sub(root.planeAdvancements$getPos());

        root.planeAdvancements$setGridPos(pos);
    }
}
