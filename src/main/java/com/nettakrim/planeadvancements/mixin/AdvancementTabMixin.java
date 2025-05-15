package com.nettakrim.planeadvancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.nettakrim.planeadvancements.*;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin implements AdvancementTabInterface {
    @Shadow @Final @Mutable
    private Map<AdvancementEntry, AdvancementWidgetInterface> widgets;

    @Shadow @Final @Mutable private AdvancementWidget rootWidget;
    @Shadow @Final @Mutable private PlacedAdvancement root;
    @Shadow @Final @Mutable private AdvancementDisplay display;

    @Shadow private int minPanX;
    @Shadow private int maxPanX;
    @Shadow private int minPanY;
    @Shadow private int maxPanY;

    @Shadow private double originX;
    @Shadow private double originY;

    @Shadow private boolean initialized;

    @Shadow @Final private AdvancementsScreen screen;
    @Unique private int temperature;

    @Unique private TreeType currentType = TreeType.DEFAULT;
    @Unique private float currentRepulsion = PlaneAdvancementsClient.repulsion;
    @Unique private int currentGridWidth;
    @Unique private boolean treeNeedsUpdate;

    @Unique private AdvancementWidget rootBackup = null;
    @Unique private Map<AdvancementEntry, AdvancementWidgetInterface> widgetsBackup = null;

    @Inject(at = @At("HEAD"), method = "render")
    private void render(DrawContext context, int x, int y, CallbackInfo ci) {
        if (!initialized) {
            planeAdvancements$heatGraph();
        }

        if (PlaneAdvancementsClient.isMergedAndSpring()) {
            if (PlaneAdvancementsClient.mergedTreeNeedsUpdate) {
                AdvancementCluster.initialiseTree(planeAdvancements$getRoot());
                PlaneAdvancementsClient.mergedTreeNeedsUpdate = false;
            }
        } else if (treeNeedsUpdate) {
            AdvancementCluster.initialiseTree(planeAdvancements$getRoot());
            treeNeedsUpdate = false;
        }

        if (currentGridWidth != PlaneAdvancementsClient.gridWidth && PlaneAdvancementsClient.treeType == TreeType.GRID) {
            planeAdvancements$applyClusters(AdvancementCluster.getGridClusters(planeAdvancements$getRoot()));
            planeAdvancements$updateRange(planeAdvancements$getWidth(), planeAdvancements$getHeight());
            planeAdvancements$centerPan(planeAdvancements$getWidth(), planeAdvancements$getHeight());
            currentGridWidth = PlaneAdvancementsClient.gridWidth;
        }

        if (currentType != PlaneAdvancementsClient.treeType) {
            planeAdvancements$updateRange(planeAdvancements$getWidth(), planeAdvancements$getHeight());
            planeAdvancements$centerPan(planeAdvancements$getWidth(), planeAdvancements$getHeight());
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
        int steps = MathHelper.ceil(MathHelper.sqrt(temperature/10f));
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
                planeAdvancements$updateRange(planeAdvancements$getWidth(), planeAdvancements$getHeight());
            } else {
                for (AdvancementWidgetInterface widget : widgets.values()) {
                    widget.planeAdvancements$updatePos();
                }
            }
        }
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "getTitle")
    private Text setTitle(Text original) {
        if (PlaneAdvancementsClient.isMergedAndSpring()) {
            return Text.translatable(PlaneAdvancementsClient.MOD_ID+".merged_tab");
        }
        return original;
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "isClickOnTab")
    private boolean hideTab(boolean original) {
        return original && !PlaneAdvancementsClient.isMergedAndSpring();
    }

    @Inject(at = @At("TAIL"), method = "addWidget")
    private void widgetAdded(CallbackInfo callbackInfo) {
        treeNeedsUpdate = true;
        PlaneAdvancementsClient.mergedTreeNeedsUpdate = true;
    }

    @Override
    public void planeAdvancements$heatGraph() {
        temperature = PlaneAdvancementsClient.getTemperature();
    }

    @Override
    public Map<AdvancementEntry, AdvancementWidgetInterface> planeAdvancements$getWidgets() {
        return widgets;
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
    public void planeAdvancements$updateRange(int width, int height) {
        currentType = PlaneAdvancementsClient.treeType;

        minPanX = Integer.MAX_VALUE;
        maxPanX = Integer.MIN_VALUE;
        minPanY = Integer.MAX_VALUE;
        maxPanY = Integer.MIN_VALUE;

        for (AdvancementWidgetInterface widget : widgets.values()) {
            widget.planeAdvancements$updatePos();

            int i = widget.planeAdvancements$getX();
            int j = i + 28;
            int k = widget.planeAdvancements$getY();
            int l = k + 27;
            minPanX = Math.min(minPanX, i);
            maxPanX = Math.max(maxPanX, j);
            minPanY = Math.min(minPanY, k);
            maxPanY = Math.max(maxPanY, l);
        }

        if (PlaneAdvancementsClient.treeType == TreeType.SPRING) {
            minPanX -= width/2;
            maxPanX += width/2;
            minPanY -= height/2;
            maxPanY += height/2;
        } else if (maxPanX - minPanX > width) {
            maxPanX += 4;
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
        for (AdvancementWidgetInterface widget : widgets.values()) {
            widget.planeAdvancements$getCurrentPos().add(offsetX, offsetY);
            widget.planeAdvancements$updatePos();
        }
    }

    @Override
    public void planeAdvancements$centerPan(int width, int height) {
        if (PlaneAdvancementsClient.compatMode == CompatMode.FULLSCREEN) {
            this.originX = (width - (maxPanX + minPanX)) >> 1;
            this.originY = (height - (maxPanY + minPanY)) >> 1;
        } else {
            this.originX = width - ((this.maxPanX + this.minPanX) >> 1);
            this.originY = height - ((this.maxPanY + this.minPanY) >> 1);
        }
    }

    @Override
    public void planeAdvancements$applyClusters(List<AdvancementCluster> clusters) {
        for (AdvancementCluster cluster : clusters) {
            cluster.applyPosition(28, 27);
        }
    }

    @Override
    public void planeAdvancements$setMerged(Collection<AdvancementTabInterface> tabs) {
        if (widgetsBackup != null) {
            return;
        }

        widgetsBackup = widgets;
        widgets = new HashMap<>(widgetsBackup);
        PlacedAdvancement placedAdvancement = new PlacedAdvancement(PlaneAdvancementsClient.mergedEntry, null);
        AdvancementWidget newRoot = new AdvancementWidget((AdvancementTab)(Object)this, MinecraftClient.getInstance(), placedAdvancement, PlaneAdvancementsClient.mergedDisplay);
        //noinspection DataFlowIssue
        AdvancementWidgetInterface newRootInterface = (AdvancementWidgetInterface)newRoot;

        tabs.forEach(tab -> {
            AdvancementWidgetInterface tabRoot = tab.planeAdvancements$getRoot();
            tabRoot.planeAdvancements$setParent(newRootInterface);
            newRootInterface.planeAdvancements$getChildren().add(tabRoot);
            widgets.putAll(tab.planeAdvancements$getWidgets());
        });
        widgets.put(PlaneAdvancementsClient.mergedEntry, newRootInterface);

        rootBackup = rootWidget;
        rootWidget = newRoot;
        display = PlaneAdvancementsClient.mergedDisplay;
        root = placedAdvancement;

        planeAdvancements$updateRange(planeAdvancements$getWidth(), planeAdvancements$getHeight());
        planeAdvancements$centerPan(planeAdvancements$getWidth(), planeAdvancements$getHeight());

        planeAdvancements$heatGraph();
    }

    @Override
    public void planeAdvancements$clearMerged(Collection<AdvancementTabInterface> tabs) {
        if (widgetsBackup == null) {
            return;
        }

        widgets = widgetsBackup;
        widgetsBackup = null;

        rootWidget = rootBackup;
        display = ((AdvancementWidgetInterface)rootWidget).planeAdvancements$getDisplay();
        root = ((AdvancementWidgetInterface)rootWidget).planeAdvancements$getPlaced();

        tabs.forEach(tab -> {
            AdvancementWidgetInterface tabRoot = tab.planeAdvancements$getRoot();
            tabRoot.planeAdvancements$setParent(null);
        });

        planeAdvancements$updateRange(planeAdvancements$getWidth(), planeAdvancements$getHeight());
        planeAdvancements$centerPan(planeAdvancements$getWidth(), planeAdvancements$getHeight());

        planeAdvancements$heatGraph();
    }

    @Unique
    private int planeAdvancements$getWidth() {
        if (PlaneAdvancementsClient.compatMode == CompatMode.FULLSCREEN) {
            return ((FullscreenInterface)screen).advancementsfullscreen$getWindowWidth(false);
        } else {
            return 117;
        }
    }

    @Unique
    private int planeAdvancements$getHeight() {
        if (PlaneAdvancementsClient.compatMode == CompatMode.FULLSCREEN) {
            return ((FullscreenInterface)screen).advancementsfullscreen$getWindowHeight(false);
        } else {
            return 56;
        }
    }
}