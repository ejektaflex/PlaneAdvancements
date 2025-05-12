package com.nettakrim.planeadvancements.mixin;

import com.nettakrim.planeadvancements.AdvancementTabInterface;
import com.nettakrim.planeadvancements.AdvancementWidgetInterface;
import com.nettakrim.planeadvancements.PlaneAdvancementsClient;
import com.nettakrim.planeadvancements.TreeType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementsScreen", remap = false)
public class BetterAdvancementsScreenMixin extends Screen {
    @Shadow private int internalWidth;
    @Shadow private int internalHeight;

    @Shadow private static int SIDE;
    @Shadow private static int TOP;
    @Shadow private static int PADDING;

    protected BetterAdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked", cancellable = true, remap = true)
    void click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        AdvancementTabInterface selectedTab = getSelectedTab();

        if (selectedTab == null || button != 1) {
            PlaneAdvancementsClient.clearUIHover();
            if (PlaneAdvancementsClient.hoveredUI()) {
                super.mouseClicked(mouseX, mouseY, button);
                cir.setReturnValue(null);
            }
            return;
        }
        PlaneAdvancementsClient.draggedWidget = null;

        int left = SIDE + (width - internalWidth) / 2;
        int top = TOP + (height - internalHeight) / 2;

        double panX = selectedTab.planeAdvancements$getPanX();
        double panY = selectedTab.planeAdvancements$getPanY();
        int x = MathHelper.floor(mouseX - left - PADDING);
        int y = MathHelper.floor(mouseY - top - 2*PADDING);

        for (Iterator<AdvancementWidgetInterface> it = selectedTab.planeAdvancements$getWidgets(); it.hasNext();) {
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

    @Inject(at = @At("HEAD"), method = "mouseDragged", cancellable = true, remap = true)
    void drag(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (PlaneAdvancementsClient.draggedWidget == null || PlaneAdvancementsClient.treeType != TreeType.SPRING) {
            if (PlaneAdvancementsClient.selectedUI()) {
                cir.setReturnValue(super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
            }
            return;
        }
        AdvancementTabInterface selectedTab = getSelectedTab();
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$getTreePos().add((float)deltaX/BetterAdvancementsScreenAccessor.getZoom(), (float)deltaY/BetterAdvancementsScreenAccessor.getZoom());
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$updatePos();
        assert selectedTab != null;
        selectedTab.planeAdvancements$heatGraph();
        cir.setReturnValue(true);
    }

    @Inject(at = @At("TAIL"), method = "render", remap = true)
    void render(DrawContext context, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        PlaneAdvancementsClient.renderUI(context, mouseX, mouseY, tickDelta);
    }

    @Inject(at = @At("TAIL"), method = "init", remap = true)
    void init(CallbackInfo ci) {
        addSelectableChild(PlaneAdvancementsClient.treeButton);
        addSelectableChild(PlaneAdvancementsClient.repulsionSlider);
        addSelectableChild(PlaneAdvancementsClient.gridWidthSlider);
        addSelectableChild(PlaneAdvancementsClient.lineButton);
    }

    @Unique
    private AdvancementTabInterface getSelectedTab() {
        try {
            return (AdvancementTabInterface)this.getClass().getDeclaredField("selectedTab").get(this);
        } catch (Exception ignored) {
            return null;
        }
    }
}