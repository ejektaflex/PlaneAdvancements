package com.nettakrim.planeadvancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nettakrim.planeadvancements.*;
import net.minecraft.advancement.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin extends Screen {
    @Shadow @Nullable private AdvancementTab selectedTab;

    @Shadow @Final private Map<AdvancementEntry, AdvancementTabInterface> tabs;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    void click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab == null || button != 1) {
            PlaneAdvancementsClient.clearUIHover();
            return;
        }
        PlaneAdvancementsClient.draggedWidget = null;
        AdvancementTabInterface tab = (AdvancementTabInterface)selectedTab;

        double panX = tab.planeAdvancements$getPanX();
        double panY = tab.planeAdvancements$getPanY();
        int x = MathHelper.floor(mouseX-((this.width - 252) >> 1)-9);
        int y = MathHelper.floor(mouseY-((this.height - 140) >> 1)-18);

        for (AdvancementWidgetInterface widget : tab.planeAdvancements$getWidgets().values()) {
            if (widget.planeAdvancements$isHovering(panX, panY, x, y)) {
                PlaneAdvancementsClient.draggedWidget = widget;
                return;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (PlaneAdvancementsClient.draggedWidget != null) {
            PlaneAdvancementsClient.draggedWidget = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Inject(at = @At("HEAD"), method = "mouseDragged", cancellable = true)
    void drag(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (PlaneAdvancementsClient.draggedWidget == null || PlaneAdvancementsClient.treeType != TreeType.SPRING) {
            if (PlaneAdvancementsClient.selectedUI()) {
                cir.setReturnValue(super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
            }
            return;
        }
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$getTreePos().add((float)deltaX, (float)deltaY);
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$updatePos();
        assert selectedTab != null;
        ((AdvancementTabInterface)selectedTab).planeAdvancements$heatGraph();
        cir.setReturnValue(true);
    }

    @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Ljava/util/Map;size()I"), method = "drawWindow")
    int hideTabs(int original) {
        if (PlaneAdvancementsClient.isMerged()) {
            return 0;
        }
        return original;
    }

    @Inject(at = @At("HEAD"), method = "render")
    void merge(DrawContext context, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        if(selectedTab != null) {
            if (PlaneAdvancementsClient.isMerged()) {
                ((AdvancementTabInterface)selectedTab).planeAdvancements$setMerged(tabs.values());
            } else {
                ((AdvancementTabInterface)selectedTab).planeAdvancements$clearMerged(tabs.values());
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    void render(DrawContext context, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        PlaneAdvancementsClient.renderUI(context, mouseX, mouseY, tickDelta);
    }

    @Inject(at = @At("TAIL"), method = "init")
    void init(CallbackInfo ci) {
        addSelectableChild(PlaneAdvancementsClient.treeButton);
        addSelectableChild(PlaneAdvancementsClient.repulsionSlider);
        addSelectableChild(PlaneAdvancementsClient.gridWidthSlider);
        addSelectableChild(PlaneAdvancementsClient.lineButton);
        addSelectableChild(PlaneAdvancementsClient.mergedButton);
    }
}
