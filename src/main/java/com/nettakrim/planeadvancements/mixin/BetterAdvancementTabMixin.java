package com.nettakrim.planeadvancements.mixin;

import com.nettakrim.planeadvancements.*;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementTab", remap = false)
public abstract class BetterAdvancementTabMixin implements AdvancementTabInterface {
    @Shadow
    @Final
    private Map<AdvancementEntry, AdvancementWidgetInterface> widgets;

    @Shadow private int minX;
    @Shadow private int maxX;
    @Shadow private int minY;
    @Shadow private int maxY;

    @Shadow private int scrollX;
    @Shadow private int scrollY;
    
    @Unique private AdvancementWidgetInterface root;

    @Unique
    private int temperature = -1;

    @Unique private TreeType currentType = TreeType.DEFAULT;
    @Unique private float currentRepulsion = PlaneAdvancementsClient.repulsion;
    @Unique private int currentGridWidth;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(MinecraftClient client, @Coerce Object screen, @Coerce Object type, int index, PlacedAdvancement root, AdvancementDisplay display, CallbackInfo ci) {
        try {
            this.root = (AdvancementWidgetInterface)this.getClass().getDeclaredField("root").get(this);
        } catch (Exception ignored) {}
    }

    @Inject(at = @At("HEAD"), method = "drawContents")
    private void render(DrawContext context, int left, int top, int width, int height, float zoom, CallbackInfo ci) {
        // shadowing centered is inconsistent, for some reason
        if (temperature == -1) {
            planeAdvancements$heatGraph();
            planeAdvancements$centerPan(width, height);
        }

        if (currentGridWidth != PlaneAdvancementsClient.gridWidth && PlaneAdvancementsClient.treeType == TreeType.GRID) {
            planeAdvancements$applyClusters(AdvancementCluster.getGridClusters(planeAdvancements$getRoot()));
            planeAdvancements$updateRange(width, height);
            planeAdvancements$centerPan(width, height);
            currentGridWidth = PlaneAdvancementsClient.gridWidth;
        }

        if (currentType != PlaneAdvancementsClient.treeType) {
            planeAdvancements$updateRange(width, height);
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
            for (AdvancementWidgetInterface widgetA : widgets.values()) {
                for (AdvancementWidgetInterface widgetB : widgets.values()) {
                    widgetA.planeAdvancements$applySpringForce(widgetB, 0.1f, PlaneAdvancementsClient.repulsion);
                }
            }
        }

        if (PlaneAdvancementsClient.treeType == TreeType.SPRING) {
            // == 1 so it updates on the last update tick
            if (temperature%60 == 1) {
                planeAdvancements$updateRange(width, height);
            } else {
                for (AdvancementWidgetInterface widget : widgets.values()) {
                    widget.planeAdvancements$updatePos();
                }
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "scroll")
    private void fixPan(double x, double y, int width, int height, CallbackInfo ci) {
        if (PlaneAdvancementsClient.treeType != TreeType.SPRING) {
            return;
        }

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
        return widgets.values().stream().iterator();
    }

    @Override
    public AdvancementWidgetInterface planeAdvancements$getRoot() {
        return root;
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
    public void planeAdvancements$updateRange(int width, int height) {
        currentType = PlaneAdvancementsClient.treeType;

        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;

        for (AdvancementWidgetInterface widget : widgets.values()) {
            widget.planeAdvancements$updatePos();

            int i = widget.planeAdvancements$getX();
            int j = i + 28;
            int k = widget.planeAdvancements$getY();
            int l = k + 27;
            minX = Math.min(minX, i);
            maxX = Math.max(maxX, j);
            minY = Math.min(minY, k);
            maxY = Math.max(maxY, l);
        }

        int paddingX = PlaneAdvancementsClient.treeType == TreeType.SPRING ? width/2 : 16;
        int paddingY = PlaneAdvancementsClient.treeType == TreeType.SPRING ? height/2 : 16;
        if (maxX - minX > width) {
            minX -= paddingX;
            maxX += paddingX;
        }
        if (maxY - minY > height) {
            minY -= paddingY;
            maxY += paddingY;
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