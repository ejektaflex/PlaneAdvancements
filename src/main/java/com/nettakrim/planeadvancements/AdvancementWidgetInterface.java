package com.nettakrim.planeadvancements;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import java.util.List;

public interface AdvancementWidgetInterface {
    List<AdvancementWidgetInterface> planeAdvancements$getChildren();
    AdvancementWidgetInterface planeAdvancements$getParent();
    void planeAdvancements$setParent(AdvancementWidgetInterface widget);

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

    boolean planeAdvancements$isHovering(double originX, double originY, int mouseX, int mouseY);

    AdvancementDisplay planeAdvancements$getDisplay();
    PlacedAdvancement planeAdvancements$getPlaced();

    void planeAdvancements$setGridPos(Vector2f pos);
    boolean planeAdvancements$isRoot();

    void planeAdvancements$setClusterRoot(boolean isClusterRoot);
    boolean planeAdvancements$renderClusterLines();

    float springScale = 32f;
    float maxAttraction = springScale/2.5f;

    default void planeAdvancements$applySpringForce(AdvancementWidgetInterface other, float attraction, float repulsion) {
        // if the attraction force is big enough, the two advancements being attracted would collide in a single step
        if (attraction > maxAttraction) {
            repulsion *= maxAttraction/attraction;
            attraction = maxAttraction;
        }

        Vector2f direction = new Vector2f(planeAdvancements$getTreePos());
        float distance = other.planeAdvancements$getTreePos().distance(direction)/springScale;
        if (distance == 0) {
            return;
        }

        direction.sub(other.planeAdvancements$getTreePos());

        if ((planeAdvancements$getParent() == other || other.planeAdvancements$getParent() == this) && distance > 1) {
            direction.normalize(distance*-attraction);
        } else {
            direction.normalize(repulsion/Math.max(distance*distance, 0.01f));
        }

        // adding force to both seems to improve stability
        if (this != PlaneAdvancementsClient.draggedWidget) {
            planeAdvancements$getTreePos().add(direction);
        } if (other != PlaneAdvancementsClient.draggedWidget) {
            other.planeAdvancements$getTreePos().sub(direction);
        }
    }

    static void renderCustomLines(DrawContext context, int x, int y, int startX, int startY, int endX, int endY, boolean border, int innerColor) {
        int offsetX = endX-startX;
        int offsetY = endY-startY;

        Matrix3x2fStack matrixStack = context.getMatrices();
        matrixStack.pushMatrix();

        if (PlaneAdvancementsClient.getCurrentLineType() == LineType.ROTATED) {
            matrixStack.translate(x+startX + 16.5f, y+startY + 13.5f);
            matrixStack.rotate((float)Math.atan2(offsetY, offsetX));
            int distance = MathHelper.floor(MathHelper.sqrt(offsetX*offsetX + offsetY*offsetY));
            if (border) {
                context.drawHorizontalLine(0, distance, -1, -16777216);
                context.drawHorizontalLine(0, distance, 1, -16777216);
            } else {
                context.drawHorizontalLine(0, distance, 0, innerColor);
            }
        } else {
            matrixStack.translate(x+startX + 15.5f, y+startY + 12.5f);
            int absX = MathHelper.abs(offsetX);
            int absY = MathHelper.abs(offsetY);
            boolean isX = absX < absY;
            int xPos = isX ? (absX < 15 ? offsetX / 2 : offsetX) : 0;
            int yPos = !isX ? (absY < 15 ? offsetY / 2 : offsetY) : 0;
            int xLength = absX < 15 ? 0 : offsetX;
            int yLength = absY < 15 ? 0 : offsetY;

            if (border) {
                offsetX /= absX == 0 ? 1 : absX;
                offsetY /= absY == 0 ? 1 : absY;
                context.drawHorizontalLine(-offsetX, xLength+offsetX, yPos-1, -16777216);
                context.drawHorizontalLine(-offsetX, xLength+offsetX, yPos+1, -16777216);
                context.drawVerticalLine(xPos-1, yLength+offsetY, -offsetY, -16777216);
                context.drawVerticalLine(xPos+1, yLength+offsetY, -offsetY, -16777216);
            } else {
                context.drawHorizontalLine(0, xLength, yPos, innerColor);
                context.drawVerticalLine(xPos, yLength, 0, innerColor);
            }
        }

        matrixStack.popMatrix();
    }

    int planeAdvancements$getX();
    int planeAdvancements$getY();
    void planeAdvancements$renderLines(DrawContext context, int x, int y, boolean border);
}
