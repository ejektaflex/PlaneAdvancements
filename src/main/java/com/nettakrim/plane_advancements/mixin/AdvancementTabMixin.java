package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.nettakrim.plane_advancements.*;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin implements AdvancementTabInterface {
    @Shadow @Final private Map<AdvancementEntry, AdvancementWidget> widgets;

    @Shadow private int minPanX;
    @Shadow private int maxPanX;
    @Shadow private int minPanY;
    @Shadow private int maxPanY;

    @Shadow private double originX;
    @Shadow private double originY;

    @Shadow private boolean initialized;
    @Shadow @Final private AdvancementWidget rootWidget;

    @Unique private int temperature;

    @Unique private TreeType currentType = TreeType.DEFAULT;
    @Unique private float currentRepulsion = PlaneAdvancementsClient.repulsion;

    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;renderLines(Lnet/minecraft/client/gui/DrawContext;IIZ)V"), method = "render")
    private boolean renderLines(AdvancementWidget instance, DrawContext context, int x, int y, boolean border) {
        // remove root lines for grid mode
        if (PlaneAdvancementsClient.treeType != TreeType.GRID) {
            return true;
        }

        for (AdvancementWidgetInterface child : ((AdvancementWidgetInterface)instance).planeAdvancements$getChildren()) {
            for (AdvancementWidgetInterface childChild : child.planeAdvancements$getChildren()) {
                ((AdvancementWidget)childChild).renderLines(context, x, y, border);
            }
        }
        return false;
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void render(DrawContext context, int x, int y, CallbackInfo ci) {
        if (!initialized) {
            planeAdvancements$arrangeIntoGrid();
            planeAdvancements$heatGraph();
        }

        if (currentType != PlaneAdvancementsClient.treeType) {
            planeAdvancements$updateRange();
            planeAdvancements$centerPan(117, 56);
        }

        if (currentRepulsion != PlaneAdvancementsClient.repulsion) {
            currentRepulsion = PlaneAdvancementsClient.repulsion;
            planeAdvancements$heatGraph();
        }

        if (temperature <= 0) {
            return;
        }
        temperature--;

        // always update spring graph forces, so that it can settle while not visible
        int steps = MathHelper.ceil(MathHelper.sqrt(temperature /10f));
        for (int i = 0; i < steps; i++) {
            for (AdvancementWidget widgetA : widgets.values()) {
                AdvancementWidgetInterface ducky = (AdvancementWidgetInterface) widgetA;
                for (AdvancementWidget widgetB : widgets.values()) {
                    ducky.planeAdvancements$applySpringForce((AdvancementWidgetInterface) widgetB, 0.1f, PlaneAdvancementsClient.repulsion);
                }
            }
        }

        if (PlaneAdvancementsClient.treeType == TreeType.SPRING) {
            // == 1 so it updates on the last update tick
            if (temperature%60 == 1) {
                planeAdvancements$updateRange();
            } else {
                for (AdvancementWidget widget : widgets.values()) {
                    ((AdvancementWidgetInterface)widget).planeAdvancements$updatePos();
                }
            }
        }
    }

    @Override
    public void planeAdvancements$heatGraph() {
        temperature = 1000;
    }

    @Override
    public Iterator<AdvancementWidgetInterface> planeAdvancements$getWidgets() {
        return widgets.values().stream().map(w -> (AdvancementWidgetInterface)w).iterator();
    }

    @Override
    public AdvancementWidgetInterface planeAdvancements$getRoot() {
        return (AdvancementWidgetInterface)rootWidget;
    }

    @Override
    public double planeAdvancements$getPanX() {
        return originX;
    }

    @Override
    public double planeAdvancements$getPanY() {
        return originY;
    }

    @Override
    public void planeAdvancements$updateRange() {
        currentType = PlaneAdvancementsClient.treeType;

        minPanX = Integer.MAX_VALUE;
        maxPanX = Integer.MIN_VALUE;
        minPanY = Integer.MAX_VALUE;
        maxPanY = Integer.MIN_VALUE;

        for (AdvancementWidget widget : widgets.values()) {
            ((AdvancementWidgetInterface)widget).planeAdvancements$updatePos();

            int i = widget.getX();
            int j = i + 28;
            int k = widget.getY();
            int l = k + 27;
            minPanX = Math.min(minPanX, i);
            maxPanX = Math.max(maxPanX, j);
            minPanY = Math.min(minPanY, k);
            maxPanY = Math.max(maxPanY, l);
        }

        // min pan only works as 0, so if it does extend too far, everything needs to be offset to compensate
        int offsetX = MathHelper.ceil(-minPanX/16f)*16;
        int offsetY = MathHelper.ceil(-minPanY/16f)*16;

        if (offsetX == 0 && offsetY == 0) {
            return;
        }

        minPanX = 0;
        minPanY = 0;
        maxPanX += offsetX;
        maxPanY += offsetY;
        originX -= offsetX;
        originY -= offsetY;
        for (AdvancementWidget widget : widgets.values()) {
            AdvancementWidgetInterface ducky = (AdvancementWidgetInterface)widget;
            ducky.planeAdvancements$getCurrentPos().add(offsetX, offsetY);
            ducky.planeAdvancements$updatePos();
        }
    }

    @Override
    public void planeAdvancements$centerPan(int width, int height) {
        this.originX = width - ((this.maxPanX + this.minPanX) >> 1);
        this.originY = height - ((this.maxPanY + this.minPanY) >> 1);
    }

    @Override
    public void planeAdvancements$applyClusters(List<AdvancementCluster> clusters) {
        for (AdvancementCluster cluster : clusters) {
            cluster.applyPosition(28, 27);
        }
    }
}