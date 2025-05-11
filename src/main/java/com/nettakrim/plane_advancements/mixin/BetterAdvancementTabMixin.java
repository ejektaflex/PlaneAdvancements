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
public abstract class BetterAdvancementTabMixin implements AdvancementTabInterface {
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

    @Unique private TreeType currentType = TreeType.DEFAULT;
    @Unique private float currentRepulsion = PlaneAdvancementsClient.repulsion;

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
            planeAdvancements$heatGraph();
        }

        if (currentType != PlaneAdvancementsClient.treeType) {
            planeAdvancements$updateRange();
            planeAdvancements$centerPan(width, height);
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
            for (BetterAdvancementWidget widgetA : widgets.values()) {
                AdvancementWidgetInterface ducky = (AdvancementWidgetInterface) widgetA;
                for (BetterAdvancementWidget widgetB : widgets.values()) {
                    ducky.planeAdvancements$applySpringForce((AdvancementWidgetInterface) widgetB, 0.1f, PlaneAdvancementsClient.repulsion);
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

    @Inject(at = @At("TAIL"), method = "scroll")
    private void fixPan(double x, double y, int width, int height, CallbackInfo ci) {
        if (maxX - minX <= width) {
            scrollX = (width - (maxX + minX))/2;
        }
        if (maxY - minY <= height) {
            scrollY = (height - (maxY + minY))/2;
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
        currentType = PlaneAdvancementsClient.treeType;

        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
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
    }

    @Override
    public void planeAdvancements$centerPan(int width, int height) {
        scrollX = (width - (maxX + minX))/2;
        scrollY = (height - (maxY + minY))/2;
    }

    @Override
    public void planeAdvancements$applyClusters(List<AdvancementCluster> clusters) {
        for (AdvancementCluster cluster : clusters) {
            cluster.applyPosition(32, 27);
        }
    }
}