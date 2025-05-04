package com.nettakrim.plane_advancements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "betteradvancements.common.gui.BetterAdvancementWidget.class", remap = false)
public interface BetterAdvancementWidgetAccessor {
    @Accessor("parent")
    betteradvancements.common.gui.BetterAdvancementWidget getParent();
}
