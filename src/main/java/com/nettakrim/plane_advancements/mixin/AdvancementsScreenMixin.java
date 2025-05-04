package com.nettakrim.plane_advancements.mixin;

import com.nettakrim.plane_advancements.AdvancementWidgetInterface;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin extends Screen {
    @Shadow @Nullable private AdvancementTab selectedTab;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private AdvancementWidgetInterface dragging;

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    void click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (selectedTab == null || button != 0) {
            return;
        }
        dragging = null;
        AdvancementTabAccessor tabAccessor = (AdvancementTabAccessor)selectedTab;

        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        int x = MathHelper.floor(mouseX-tabAccessor.getOriginX()-i-9);
        int y = MathHelper.floor(mouseY-tabAccessor.getOriginY()-j-18);

        for (AdvancementWidget advancementWidget : ((AdvancementTabAccessor)selectedTab).getWidgets().values()) {
            AdvancementWidgetInterface ducky = (AdvancementWidgetInterface) advancementWidget;
            if (ducky.planeAdvancements$isHovering(x, y)) {
                dragging = ducky;
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mouseDragged", cancellable = true)
    void drag(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (dragging == null) {
            return;
        }
        dragging.planeAdvancements$getPos().add((float)deltaX, (float)deltaY);
        cir.setReturnValue(true);
        cir.cancel();
    }
}
