package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.nettakrim.plane_advancements.AdvancementWidgetInterface;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {
    @Shadow @Final private Map<AdvancementEntry, AdvancementWidget> widgets;

    @Shadow private int minPanX;
    @Shadow private int maxPanX;
    @Shadow private int minPanY;
    @Shadow private int maxPanY;

    @Shadow private double originX;
    @Shadow private double originY;

    @Unique int t;

    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;renderLines(Lnet/minecraft/client/gui/DrawContext;IIZ)V"), method = "render")
    private boolean renderLines(AdvancementWidget instance, DrawContext context, int x, int y, boolean border) {
        // remove root lines for grid mode

        //for (AdvancementWidget child : ((AdvancementWidgetAccessor)instance).getChildren()) {
        //    for (AdvancementWidget childChild : ((AdvancementWidgetAccessor)child).getChildren()) {
        //        childChild.renderLines(context, x, y, border);
        //    }
        //}
        //return false;
        return true;
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void render(DrawContext context, int x, int y, CallbackInfo ci) {
        boolean update = t%60 == 0;

        for (AdvancementWidget widgetA : widgets.values()) {
            AdvancementWidgetInterface ducky = (AdvancementWidgetInterface)widgetA;
            for (AdvancementWidget widgetB : widgets.values()) {
                ducky.planeAdvancements$spring(widgetB, 0.1f);
            }

            if (update) {
                int i = widgetA.getX();
                int j = i + 28;
                int k = widgetA.getY();
                int l = k + 27;
                this.minPanX = Math.min(this.minPanX, i);
                this.maxPanX = Math.max(this.maxPanX, j);
                this.minPanY = Math.min(this.minPanY, k);
                this.maxPanY = Math.max(this.maxPanY, l);
            } else {
                ducky.planeAdvancements$updatePos();
            }
        }

        if (update) {
            // min pan only works as 0, so if it does extend too far, everything needs to be offset to compensate
            int offsetX = MathHelper.ceil(-minPanX/16f)*16;
            int offsetY = MathHelper.ceil(-minPanY/16f)*16;
            minPanX = 0;
            minPanY = 0;
            maxPanX += offsetX;
            maxPanY += offsetY;
            originX -= offsetX;
            originY -= offsetY;
            for (AdvancementWidget widget : widgets.values()) {
                AdvancementWidgetInterface ducky = (AdvancementWidgetInterface)widget;
                ducky.planeAdvancements$getPos().add(offsetX, offsetY);
                ducky.planeAdvancements$updatePos();
            }
        }
        t++;
    }
}