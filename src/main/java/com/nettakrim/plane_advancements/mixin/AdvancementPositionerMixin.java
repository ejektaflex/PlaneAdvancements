package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nettakrim.plane_advancements.AdvancementPositionerInterface;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementPositioner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementPositioner.class)
public class AdvancementPositionerMixin implements AdvancementPositionerInterface {
    @Shadow private int depth;

    @Shadow @Final @Nullable private AdvancementPositioner parent;
    @Unique private float xPos;
    @Unique private float yPos;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementDisplay;setPos(FF)V"), method = "method_53710")
    private void setPosition(AdvancementDisplay instance, float x, float y, Operation<Void> original) {
        if (depth == 0) {
            original.call(instance, x, y);
            return;
        }

        if (depth == 1) {
            xPos = (float)(Math.random() * 10) - x;
            yPos = (float)(Math.random() * 10) - y;
        } else {
            AdvancementPositionerInterface positioner = (AdvancementPositionerInterface)parent;
            if (positioner != null) {
                xPos = positioner.planeAdvancements$getX();
                yPos = positioner.planeAdvancements$getY();
            }
        }

        instance.setPos(xPos + x, yPos + y);
    }

    @Override
    public float planeAdvancements$getX() {
        return xPos;
    }

    @Override
    public float planeAdvancements$getY() {
        return yPos;
    }
}
