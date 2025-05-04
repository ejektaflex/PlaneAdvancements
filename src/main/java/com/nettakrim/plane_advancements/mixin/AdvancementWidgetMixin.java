package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nettakrim.plane_advancements.AdvancementWidgetInterface;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AdvancementWidget.class)
public class AdvancementWidgetMixin implements AdvancementWidgetInterface {
    @Shadow @Final @Mutable
    private int x;
    @Shadow @Final @Mutable
    private int y;

    @Shadow @Final private List<AdvancementWidget> children;

    @Shadow @Nullable private AdvancementWidget parent;
    @Unique
    Vector2f pos;

    @Inject(at = @At("TAIL"), method = "<init>")
    void initPos(AdvancementTab tab, MinecraftClient client, PlacedAdvancement advancement, AdvancementDisplay display, CallbackInfo ci) {
        pos = new Vector2f(x, y);
    }

    @Inject(at = @At("HEAD"), method = "renderWidgets")
    void render(DrawContext context, int x, int y, CallbackInfo ci) {
        this.x = Math.round(pos.x);
        this.y = Math.round(pos.y);
    }

    @WrapMethod(method = "renderLines")
    void renderLines(DrawContext context, int x, int y, boolean border, Operation<Void> original) {
        if (parent != null) {
            MatrixStack matrixStack = context.getMatrices();
            matrixStack.push();
            matrixStack.translate(x+this.x + 16.5, y+this.y + 13.5, 0);
            int offsetX = parent.getX()-this.x;
            int offsetY = parent.getY()-this.y;
            matrixStack.multiply(new Quaternionf(new AxisAngle4f((float)Math.atan2(offsetY, offsetX), 0, 0, 1)));
            int distance = MathHelper.floor(MathHelper.sqrt(offsetX*offsetX + offsetY*offsetY));
            context.drawHorizontalLine(0, distance,-1,-16777216);
            context.drawHorizontalLine(0, distance, 1,-16777216);
            context.drawHorizontalLine(0, distance, 0,-1);
            matrixStack.pop();
        }

        for (AdvancementWidget advancementWidget : children) {
            advancementWidget.renderLines(context, x, y, border);
        }
    }

    @Override
    public List<AdvancementWidget> planeAdvancements$getChildren() {
        return children;
    }

    @Override
    public Vector2f planeAdvancements$getPos() {
        return pos;
    }

    @Override
    public void planeAdvancements$setPos(Vector2f pos) {
        this.pos = pos;
    }

    @Override
    public boolean planeAdvancements$isHovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + 26 && mouseY >= y && mouseY <= y + 26;
    }

    @Override
    public void planeAdvancements$spring(AdvancementWidget other, float speed) {
        AdvancementWidgetInterface ducky = (AdvancementWidgetInterface)other;
        float distance = ducky.planeAdvancements$getPos().distance(pos)/30f;
        if (distance == 0) {
            return;
        }

        Vector2f direction = new Vector2f(pos).sub(ducky.planeAdvancements$getPos());

        //TODO try doing attraction as linear and repulsion as inverse square (https://en.wikipedia.org/wiki/Force-directed_graph_drawing)
        if ((other.equals(parent) || children.contains(other)) && distance > 1) {
            direction.normalize(distance*distance*-speed);
        } else {
            float force = 1f/Math.max(distance, 0.01f);

            direction.normalize(force*speed*0.2f);
        }

        pos.add(direction);
        ducky.planeAdvancements$getPos().sub(direction);
    }
}
