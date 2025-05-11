package com.nettakrim.planeadvancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nettakrim.planeadvancements.AdvancementWidgetInterface;
import com.nettakrim.planeadvancements.LineType;
import com.nettakrim.planeadvancements.PlaneAdvancementsClient;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin implements AdvancementWidgetInterface {
    @Shadow @Final @Mutable
    private int x;
    @Shadow @Final @Mutable
    private int y;

    @Shadow @Nullable private AdvancementWidget parent;
    @Shadow @Final private List<AdvancementWidget> children;

    @Shadow @Final private AdvancementDisplay display;

    @Unique Vector2f defaultPos;
    @Unique Vector2f treePos;
    @Unique Vector2f gridPos;

    @Shadow public abstract boolean shouldRender(int originX, int originY, int mouseX, int mouseY);

    @Inject(at = @At("TAIL"), method = "<init>")
    void initPos(AdvancementTab tab, MinecraftClient client, PlacedAdvancement advancement, AdvancementDisplay display, CallbackInfo ci) {
        defaultPos = new Vector2f(x, y);
        treePos = new Vector2f(x, y);
        gridPos = new Vector2f(x, y);
    }

    @WrapMethod(method = "renderLines")
    void renderLines(DrawContext context, int x, int y, boolean border, Operation<Void> original) {
        if (PlaneAdvancementsClient.getCurrentLineType() == LineType.DEFAULT) {
            original.call(context, x, y, border);
            return;
        }

        if (parent != null) {
            AdvancementWidgetInterface.renderLines(context, x, y, this.x, this.y, parent.getX(), parent.getY(), border, -1);
        }

        for (AdvancementWidget advancementWidget : children) {
            advancementWidget.renderLines(context, x, y, border);
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "shouldRender")
    private boolean forceTooltipIfDragged(boolean original) {
        if (PlaneAdvancementsClient.draggedWidget != null) {
            return PlaneAdvancementsClient.draggedWidget == this;
        }
        return original;
    }

    @Override
    public List<AdvancementWidgetInterface> planeAdvancements$getChildren() {
        ArrayList<AdvancementWidgetInterface> childList = new ArrayList<>(children.size());
        for (AdvancementWidget child : children) {
            childList.add((AdvancementWidgetInterface)child);
        }
        return childList;
    }

    @Override
    public void planeAdvancements$updatePos() {
        Vector2f pos = planeAdvancements$getCurrentPos();

        if (this.x != MathHelper.floor(pos.x) && this.x != MathHelper.ceil(pos.x)) {
            this.x = Math.round(pos.x);
        }
        if (this.y != MathHelper.floor(pos.y) && this.y != MathHelper.ceil(pos.y)) {
            this.y = Math.round(pos.y);
        }
    }

    @Override
    public Vector2f planeAdvancements$getDefaultPos() {
        return defaultPos;
    }

    @Override
    public Vector2f planeAdvancements$getTreePos() {
        return treePos;
    }

    @Override
    public Vector2f planeAdvancements$getGridPos() {
        return gridPos;
    }

    @Override
    public boolean planeAdvancements$isHovering(double originX, double originY, int mouseX, int mouseY) {
        return shouldRender((int)originX, (int)originY, mouseX, mouseY);
    }

    public boolean planeAdvancements$isConnected(AdvancementWidgetInterface other) {
        //noinspection EqualsBetweenInconvertibleTypes,SuspiciousMethodCalls
        return other.equals(parent) || children.contains(other);
    }

    @Override
    public AdvancementDisplay planeAdvancements$getDisplay() {
        return display;
    }

    @Override
    public void planeAdvancements$setGridPos(Vector2f pos) {
        gridPos.add(pos);
        planeAdvancements$updatePos();

        if (planeAdvancements$isRoot()) {
            return;
        }

        for (AdvancementWidget child : children) {
            ((AdvancementWidgetInterface)child).planeAdvancements$setGridPos(pos);
        }
    }

    @Override
    public boolean planeAdvancements$isRoot() {
        return display.getX() == 0;
    }
}
