package com.nettakrim.planeadvancements.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "betteradvancements.common.advancements.BetterDisplayInfo", remap = false)
public interface BetterDisplayInfoAccessor {
    @Invoker int callGetCompletedLineColor();
    @Invoker int callGetUnCompletedLineColor();
}
