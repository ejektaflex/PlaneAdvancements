package com.nettakrim.plane_advancements.mixin;

import com.nettakrim.plane_advancements.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin extends Screen {
    @Shadow @Nullable private AdvancementTab selectedTab;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    void click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab == null || button != 1) {
            return;
        }
        PlaneAdvancementsClient.draggedWidget = null;
        AdvancementTabInterface tab = (AdvancementTabInterface)selectedTab;

        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        int x = MathHelper.floor(mouseX-tab.planeAdvancements$getPanX()-i-9);
        int y = MathHelper.floor(mouseY-tab.planeAdvancements$getPanY()-j-18);

        for (Iterator<AdvancementWidgetInterface> it = tab.planeAdvancements$getWidgets(); it.hasNext();) {
            AdvancementWidgetInterface widget = it.next();
            if (widget.planeAdvancements$isHovering(x, y)) {
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
            PlaneAdvancementsClient.treeType = TreeType.values()[(PlaneAdvancementsClient.treeType.ordinal() + 1) % TreeType.values().length];
            AdvancementTabInterface tab = (AdvancementTabInterface)selectedTab;
            tab.planeAdvancements$updateRange();
            tab.planeAdvancements$centerPan();
        }

        if (keyCode == InputUtil.GLFW_KEY_H) {
            PlaneAdvancementsClient.lineType = LineType.values()[(PlaneAdvancementsClient.lineType.ordinal() + 1) % LineType.values().length];
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
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$getTreePos().add((float)deltaX, (float)deltaY);
        PlaneAdvancementsClient.draggedWidget.planeAdvancements$updatePos();
        cir.setReturnValue(true);
        cir.cancel();
    }
}
