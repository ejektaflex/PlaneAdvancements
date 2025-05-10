package com.nettakrim.plane_advancements.mixin;

import betteradvancements.common.gui.BetterAdvancementWidget;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.nettakrim.plane_advancements.*;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementTab", remap = false)
public class BetterAdvancementTabMixin implements AdvancementTabInterface {
    @Shadow
    @Final
    private Map<AdvancementEntry, BetterAdvancementWidget> widgets;

    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;

    @Shadow private int scrollX;
    @Shadow private int scrollY;
    
    @Shadow @Final private BetterAdvancementWidget root;

    @Unique
    private int temperature = -1;

    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lbetteradvancements/common/gui/BetterAdvancementWidget;drawConnectivity(Lnet/minecraft/client/gui/DrawContext;IIZ)V"), method = "drawContents")
    private boolean renderLines(BetterAdvancementWidget instance, DrawContext context, int x, int y, boolean border) {
        // remove root lines for grid mode
        if (PlaneAdvancementsClient.treeType != TreeType.GRID) {
            return true;
        }

        for (AdvancementWidgetInterface child : ((AdvancementWidgetInterface)instance).planeAdvancements$getChildren()) {
            for (AdvancementWidgetInterface childChild : child.planeAdvancements$getChildren()) {
                ((BetterAdvancementWidget)childChild).drawConnectivity(context, x, y, border);
            }
        }
        return false;
    }

    @Inject(at = @At("HEAD"), method = "drawContents")
    private void render(DrawContext context, int left, int top, int width, int height, float zoom, CallbackInfo ci) {
        // shadowing centered is inconsistent, for some reason
        if (temperature == -1) {
            planeAdvancements$arrangeIntoGrid();
            planeAdvancements$updateRange();
            planeAdvancements$centerPan(width, height);
            planeAdvancements$heatGraph();
        }

        if (temperature <= 0) {
            return;
        }
        temperature--;

        // always update spring graph forces, so that it can settle while not visible
        int steps = MathHelper.ceil(MathHelper.sqrt(temperature /10f));
        for (int i = 0; i < steps; i++) {
            for (BetterAdvancementWidget widgetA : widgets.values()) {
                AdvancementWidgetInterface ducky = (AdvancementWidgetInterface) widgetA;
                for (BetterAdvancementWidget widgetB : widgets.values()) {
                    ducky.planeAdvancements$applySpringForce((AdvancementWidgetInterface) widgetB, 0.1f, 0.1f);
                }
            }
        }

        if (PlaneAdvancementsClient.treeType == TreeType.SPRING) {
            // == 1 so it updates on the last update tick
            if (temperature%60 == 1) {
                planeAdvancements$updateRange();
            } else {
                for (BetterAdvancementWidget widget : widgets.values()) {
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
        return (AdvancementWidgetInterface) root;
    }

    @Override
    public double planeAdvancements$getPanX() {
        return scrollX;
    }

    @Override
    public double planeAdvancements$getPanY() {
        return scrollY;
    }

    @Override
    public void planeAdvancements$updateRange() {
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;

        for (BetterAdvancementWidget widget : widgets.values()) {
            ((AdvancementWidgetInterface)widget).planeAdvancements$updatePos();

            int i = widget.getX();
            int j = i + 28;
            int k = widget.getY();
            int l = k + 27;
            minX = Math.min(minX, i);
            maxX = Math.max(maxX, j);
            minY = Math.min(minY, k);
            maxY = Math.max(maxY, l);
        }

        // min pan only works as 0, so if it does extend too far, everything needs to be offset to compensate
        int offsetX = MathHelper.ceil(-minX/16f)*16;
        int offsetY = MathHelper.ceil(-minY/16f)*16;
        minX = 0;
        minY = 0;
        maxX += offsetX;
        maxY += offsetY;
        scrollX -= offsetX;
        scrollY -= offsetY;
        for (BetterAdvancementWidget widget : widgets.values()) {
            AdvancementWidgetInterface ducky = (AdvancementWidgetInterface)widget;
            ducky.planeAdvancements$getCurrentPos().add(offsetX, offsetY);
            ducky.planeAdvancements$updatePos();
        }
    }

    @Override
    public void planeAdvancements$centerPan(int width, int height) {
        this.scrollX = width - ((this.maxX + this.minX) >> 1);
        this.scrollY = height - ((this.maxY + this.minY) >> 1);
    }

    @Override
    public void planeAdvancements$applyClusters(List<AdvancementCluster> clusters) {
        for (AdvancementCluster cluster : clusters) {
            cluster.applyPosition(32, 27);
        }
    }
}