package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {
    @WrapWithCondition(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;renderLines(Lnet/minecraft/client/gui/DrawContext;IIZ)V"), method = "render")
    private boolean renderLines(AdvancementWidget instance, DrawContext context, int x, int y, boolean border) {
        for (AdvancementWidget child : ((AdvancementWidgetAccessor)instance).getChildren()) {
            for (AdvancementWidget childChild : ((AdvancementWidgetAccessor)child).getChildren()) {
                childChild.renderLines(context, x, y, border);
            }
        }
        return false;
    }
}