package com.nettakrim.planeadvancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nettakrim.planeadvancements.*;
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
public abstract class AdvancementWidgetMixin implements AdvancementWidgetInterface {
    @Shadow @Final @Mutable
    private int x;
    @Shadow @Final @Mutable
    private int y;

    @Shadow @Nullable private AdvancementWidget parent;
    @Shadow @Final private List<AdvancementWidgetInterface> children;

    @Shadow @Final private AdvancementDisplay display;
    @Shadow @Final private PlacedAdvancement advancement;

    @Unique Vector2f defaultPos;
    @Unique Vector2f gridPos;
    @Unique TreePosition treePos;

    @Unique boolean isClusterRoot;

    @Shadow public abstract boolean shouldRender(int originX, int originY, int mouseX, int mouseY);

    @Shadow public abstract void renderLines(DrawContext context, int x, int y, boolean border);

    @Shadow @Final private int width;

    @Inject(at = @At("TAIL"), method = "<init>")
    void initPos(AdvancementTab tab, MinecraftClient client, PlacedAdvancement advancement, AdvancementDisplay display, CallbackInfo ci) {
        defaultPos = new Vector2f(x, y);
        gridPos = new Vector2f(x, y);
        treePos = PlaneAdvancementsClient.positions.computeIfAbsent(advancement.getAdvancement(), k -> new TreePosition(x, y));
    }

    @WrapMethod(method = "renderLines")
    void renderLines(DrawContext context, int x, int y, boolean border, Operation<Void> original) {
        // remove root lines for grid mode
        if (isClusterRoot && PlaneAdvancementsClient.treeType == TreeType.GRID) {
            for (AdvancementWidgetInterface advancementWidget : children) {
                advancementWidget.planeAdvancements$renderLines(context, x, y, border);
            }
            return;
        }

        if (PlaneAdvancementsClient.getCurrentLineType() == LineType.DEFAULT) {
            original.call(context, x, y, border);
            return;
        }

        if (parent != null) {
            AdvancementWidgetInterface.renderCustomLines(context, x, y, this.x, this.y, parent.getX(), parent.getY(), border, -1);
        }

        for (AdvancementWidgetInterface advancementWidget : children) {
            advancementWidget.planeAdvancements$renderLines(context, x, y, border);
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
        return children;
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
        return treePos.getCurrentPosition();
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
        // noinspection EqualsBetweenInconvertibleTypes
        return other.equals(parent) || children.contains(other);
    }

    @Override
    public AdvancementDisplay planeAdvancements$getDisplay() {
        return display;
    }

    @Override
    public PlacedAdvancement planeAdvancements$getPlaced() {
        return advancement;
    }

    @Override
    public void planeAdvancements$setGridPos(Vector2f pos) {
        defaultPos.add(pos, gridPos);
        planeAdvancements$updatePos();

        for (AdvancementWidgetInterface child : children) {
            if (child.planeAdvancements$renderClusterLines()) {
                child.planeAdvancements$setGridPos(pos);
            }
        }
    }

    @Override
    public boolean planeAdvancements$isRoot() {
        return display.getX() == 0;
    }

    @Override
    public void planeAdvancements$setClusterRoot(boolean isClusterRoot) {
        this.isClusterRoot = isClusterRoot;
    }

    @Override
    public boolean planeAdvancements$renderClusterLines() {
        return !isClusterRoot;
    }

    @Override
    public int planeAdvancements$getX() {
        return x;
    }

    @Override
    public int planeAdvancements$getY() {
        return y;
    }

    @Override
    public void planeAdvancements$renderLines(DrawContext context, int x, int y, boolean border) {
        renderLines(context, x, y, border);
    }

    @Override
    public void planeAdvancements$setParent(AdvancementWidgetInterface widget) {
        parent = (AdvancementWidget)widget;
    }

    @Override
    public void planeAdvancements$addChild(AdvancementWidgetInterface widget) {
        children.add(widget);
    }
}
