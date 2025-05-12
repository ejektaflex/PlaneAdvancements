package com.nettakrim.planeadvancements.mixin;

import betteradvancements.common.gui.BetterAdvancementTab;
import betteradvancements.common.gui.BetterAdvancementsScreen;
import com.nettakrim.planeadvancements.AdvancementTabInterface;
import com.nettakrim.planeadvancements.AdvancementWidgetInterface;
import com.nettakrim.planeadvancements.PlaneAdvancementsClient;
import com.nettakrim.planeadvancements.TreeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementsScreen", remap = false)
public class BetterAdvancementsScreenMixin extends Screen {
    @Shadow @Nullable private BetterAdvancementTab selectedTab;

    @Shadow private int internalWidth;
    @Shadow private int internalHeight;

    @Shadow private static int SIDE;
    @Shadow private static int TOP;
    @Shadow private static int PADDING;

    protected BetterAdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true)
    void click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab == null || button != 1) {
            PlaneAdvancementsClient.clearUIHover();
            if (PlaneAdvancementsClient.hoveredUI()) {
                super.mouseClicked(mouseX, mouseY, button);
                cir.cancel();
            }
            return;
        }
        PlaneAdvancementsClient.draggedWidget = null;
        AdvancementTabInterface tab = (AdvancementTabInterface)selectedTab;

        int left = SIDE + (width - internalWidth) / 2;
        int top = TOP + (height - internalHeight) / 2;

        double panX = tab.planeAdvancements$getPanX();
        double panY = tab.planeAdvancements$getPanY();
        int x = MathHelper.floor(mouseX - left - PADDING);
        int y = MathHelper.floor(mouseY - top - 2*PADDING);

        for (Iterator<AdvancementWidgetInterface> it = tab.planeAdvancements$getWidgets(); it.hasNext();) {
            AdvancementWidgetInterface widget = it.next();
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
                cir.cancel();
            }
            return;
        }
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$getTreePos().add((float)deltaX/BetterAdvancementsScreen.zoom, (float)deltaY/BetterAdvancementsScreen.zoom);
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$updatePos();
        assert selectedTab != null;
        ((AdvancementTabInterface)selectedTab).planeAdvancements$heatGraph();
        cir.setReturnValue(true);
        cir.cancel();
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
    }
}