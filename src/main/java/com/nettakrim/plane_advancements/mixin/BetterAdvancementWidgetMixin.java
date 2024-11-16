package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementWidget")
public class BetterAdvancementWidgetMixin {
    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lbetteradvancements/common/gui/BetterAdvancementWidget;drawConnection(Lnet/minecraft/client/gui/DrawContext;Lbetteradvancements/common/gui/BetterAdvancementWidget;IIZ)V"), method = "drawConnectivity")
    private boolean renderLines(betteradvancements.common.gui.BetterAdvancementWidget instance, DrawContext context, betteradvancements.common.gui.BetterAdvancementWidget parent, int x, int y, boolean border) {
        return ((BetterAdvancementWidgetAccessor)parent).getParent() != null;
    }
}