package com.nettakrim.planeadvancements.mixin;

import com.nettakrim.planeadvancements.PlaneAdvancementsClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("RETURN"))
    private void saveSpace(CallbackInfo ci) {
        PlaneAdvancementsClient.positions.clear();
    }
}
