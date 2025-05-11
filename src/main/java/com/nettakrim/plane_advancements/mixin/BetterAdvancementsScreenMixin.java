package com.nettakrim.plane_advancements.mixin;

import betteradvancements.common.gui.BetterAdvancementTab;
import betteradvancements.common.gui.BetterAdvancementsScreen;
import com.nettakrim.plane_advancements.AdvancementTabInterface;
import com.nettakrim.plane_advancements.AdvancementWidgetInterface;
import com.nettakrim.plane_advancements.PlaneAdvancementsClient;
import com.nettakrim.plane_advancements.TreeType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    void click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab == null || button != 1) {
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

    @Inject(at = @At("HEAD"), method = "keyPressed")
    private void keyPresses(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab == null) {
            return;
        }

        if (keyCode == InputUtil.GLFW_KEY_G) {
            PlaneAdvancementsClient.treeType = PlaneAdvancementsClient.treeType.next();
            AdvancementTabInterface tab = (AdvancementTabInterface)selectedTab;
            tab.planeAdvancements$updateRange();

            int left = SIDE + (width - internalWidth) / 2;
            int top = TOP + (height - internalHeight) / 2;

            int right = internalWidth - SIDE + (width - internalWidth) / 2;
            int bottom = internalHeight - SIDE + (height - internalHeight) / 2;

            int boxLeft = left + PADDING;
            int boxTop = top + 2*PADDING;
            int boxRight = right - PADDING;
            int boxBottom = bottom - PADDING;

            int width = boxRight - boxLeft;
            int height = boxBottom - boxTop;

            tab.planeAdvancements$centerPan(width, height);
            PlaneAdvancementsClient.draggedWidget = null;
        }

        if (keyCode == InputUtil.GLFW_KEY_H) {
            PlaneAdvancementsClient.lineType.put(PlaneAdvancementsClient.treeType, PlaneAdvancementsClient.getCurrentLinetype().next());
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
            return;
        }
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$getTreePos().add((float)deltaX/BetterAdvancementsScreen.zoom, (float)deltaY/BetterAdvancementsScreen.zoom);
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$updatePos();
        assert selectedTab != null;
        ((AdvancementTabInterface)selectedTab).planeAdvancements$heatGraph();
        cir.setReturnValue(true);
        cir.cancel();
    }
}