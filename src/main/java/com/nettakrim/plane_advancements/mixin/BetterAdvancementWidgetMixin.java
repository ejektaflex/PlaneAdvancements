package com.nettakrim.plane_advancements.mixin;

import betteradvancements.common.gui.BetterAdvancementTab;
import betteradvancements.common.gui.BetterAdvancementWidget;
import betteradvancements.common.gui.BetterAdvancementsScreen;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nettakrim.plane_advancements.AdvancementWidgetInterface;
import com.nettakrim.plane_advancements.LineType;
import com.nettakrim.plane_advancements.PlaneAdvancementsClient;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementWidget", remap = false)
public abstract class BetterAdvancementWidgetMixin implements AdvancementWidgetInterface {
    @Shadow
    private int x;
    @Shadow
    private int y;

    @Shadow @Nullable
    private BetterAdvancementWidget parent;
    @Shadow @Final private List<BetterAdvancementWidget> children;

    @Shadow @Final private AdvancementDisplay displayInfo;

    @Unique Vector2f defaultPos;
    @Unique Vector2f treePos;
    @Unique Vector2f gridPos;

    @Shadow public abstract boolean isMouseOver(double scrollX, double scrollY, double mouseX, double mouseY, float zoom);

    @Inject(at = @At("TAIL"), method = "<init>")
    void initPos(BetterAdvancementTab tab, MinecraftClient client, PlacedAdvancement advancement, AdvancementDisplay display, CallbackInfo ci) {
        defaultPos = new Vector2f(x, y);
        treePos = new Vector2f(x, y);
        gridPos = new Vector2f(x, y);
    }

    @WrapMethod(method = "drawConnection")
    private void renderLines(DrawContext context, betteradvancements.common.gui.BetterAdvancementWidget parent, int x, int y, boolean border, Operation<Void> original) {
        if (PlaneAdvancementsClient.getCurrentLinetype() == LineType.DEFAULT) {
            original.call(context, parent, x, y, border);
            return;
        }

        if (parent != null) {
            AdvancementWidgetInterface.renderLines(context, x, y, this.x, this.y, parent.getX(), parent.getY(), border);
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "isMouseOver")
    private boolean forceTooltipIfDragged(boolean original) {
        if (PlaneAdvancementsClient.draggedWidget != null) {
            return PlaneAdvancementsClient.draggedWidget == this;
        }
        return original;
    }

    @Override
    public List<AdvancementWidgetInterface> planeAdvancements$getChildren() {
        ArrayList<AdvancementWidgetInterface> childList = new ArrayList<>(children.size());
        for (BetterAdvancementWidget child : children) {
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
    public boolean planeAdvancements$isHovering(int mouseX, int mouseY) {
        return isMouseOver(0, 0, mouseX, mouseY, BetterAdvancementsScreen.zoom);
    }

    public boolean planeAdvancements$isConnected(AdvancementWidgetInterface other) {
        //noinspection EqualsBetweenInconvertibleTypes,SuspiciousMethodCalls
        return other.equals(parent) || children.contains(other);
    }

    @Override
    public AdvancementDisplay planeAdvancements$getDisplay() {
        return displayInfo;
    }

    @Override
    public void planeAdvancements$setGridPos(Vector2f pos) {
        gridPos.add(pos);
        planeAdvancements$updatePos();

        if (planeAdvancements$isRoot()) {
            return;
        }

        for (BetterAdvancementWidget child : children) {
            ((AdvancementWidgetInterface)child).planeAdvancements$setGridPos(pos);
        }
    }

    @Override
    public boolean planeAdvancements$isRoot() {
        return displayInfo.getX() == 0;
    }
}