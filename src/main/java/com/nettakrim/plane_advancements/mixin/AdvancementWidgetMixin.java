package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nettakrim.plane_advancements.AdvancementWidgetInterface;
import com.nettakrim.plane_advancements.PlaneAdvancementsClient;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
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

    @WrapMethod(method = "renderLines")
    void renderLines(DrawContext context, int x, int y, boolean border, Operation<Void> original) {
        if (PlaneAdvancementsClient.lineType == PlaneAdvancementsClient.LineType.DEFAULT) {
            original.call(context, x, y, border);
            return;
        }

        if (parent != null) {
            PlaneAdvancementsClient.renderLines(context, x, y, this.x, this.y, parent.getX(), parent.getY(), border);
        }

        for (AdvancementWidget advancementWidget : children) {
            advancementWidget.renderLines(context, x, y, border);
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "shouldRender")
    private boolean forceTooltipIfDragged(boolean original) {
        if (PlaneAdvancementsClient.dragging != null) {
            return PlaneAdvancementsClient.dragging == this;
        }
        return original;
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
    public void planeAdvancements$updatePos() {
        if (this.x != MathHelper.floor(pos.x) && this.x != MathHelper.ceil(pos.x)) {
            this.x = Math.round(pos.x);
        }
        if (this.y != MathHelper.floor(pos.y) && this.y != MathHelper.ceil(pos.y)) {
            this.y = Math.round(pos.y);
        }
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

        if ((other.equals(parent) || children.contains(other)) && distance > 1) {
            direction.normalize(distance*-speed);
        } else {
            direction.normalize(speed/Math.max(distance*distance, 0.01f));
        }

        if (this != PlaneAdvancementsClient.dragging) {
            pos.add(direction);
        } if (ducky != PlaneAdvancementsClient.dragging) {
            ducky.planeAdvancements$getPos().sub(direction);
        }
    }
}
