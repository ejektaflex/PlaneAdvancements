package com.nettakrim.plane_advancements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(betteradvancements.common.gui.BetterAdvancementWidget.class)
public interface BetterAdvancementWidgetAccessor {
    @Accessor("parent")
    betteradvancements.common.gui.BetterAdvancementWidget getParent();
}
