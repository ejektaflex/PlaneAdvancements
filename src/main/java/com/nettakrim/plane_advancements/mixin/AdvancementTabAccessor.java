package com.nettakrim.plane_advancements.mixin;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementTab.class)
public interface AdvancementTabAccessor {
    @Accessor("widgets")
    Map<AdvancementEntry, AdvancementWidget> getWidgets();

    @Accessor("originX")
    double getOriginX();
    @Accessor("originY")
    double getOriginY();

}
