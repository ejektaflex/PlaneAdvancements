package com.nettakrim.plane_advancements.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.nettakrim.plane_advancements.AdvancementPositionerInterface;
import com.nettakrim.plane_advancements.PlaneAdvancementsClient;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.PlacedAdvancement;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Mixin(AdvancementPositioner.class)
public abstract class AdvancementPositionerMixin implements AdvancementPositionerInterface {
    @Shadow @Final @Nullable private AdvancementPositioner parent;
    @Shadow @Final private List<AdvancementPositioner> children;

    @Shadow private int depth;
    @Shadow private float row;

    @Unique private float xPos;
    @Unique private float yPos;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementDisplay;setPos(FF)V"), method = "method_53710")
    private void setPosition(AdvancementDisplay instance, float x, float y, Operation<Void> original) {
        if (depth <= 1) {
            xPos -= x;
            yPos -= y;
        } else {
            AdvancementPositionerInterface positioner = (AdvancementPositionerInterface)parent;
            if (positioner != null) {
                planeAdvancements$setOffset(positioner.planeAdvancements$getOffset());
            }
        }

        instance.setPos(xPos + x, yPos + y);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementPositioner;apply()V"), method = "arrangeForTree")
    private static void arrangeIntoGrid(PlacedAdvancement root, CallbackInfo ci, @Local AdvancementPositioner rootPositioner) {
        PlaneAdvancementsClient.arrangeIntoGrid((AdvancementPositionerInterface)rootPositioner);
    }

    @Override
    public Vector2f planeAdvancements$getOffset() {
        return new Vector2f(xPos, yPos);
    }

    @Override
    public void planeAdvancements$setOffset(Vector2f offset) {
        xPos = offset.x;
        yPos = offset.y;
    }

    @Override
    public List<AdvancementPositionerInterface> planeAdvancements$getChildren(boolean includeThis) {
        List<AdvancementPositionerInterface> childrenInterfaces = new ArrayList<>(children.size()+(includeThis ? 1 : 0));
        if (includeThis) childrenInterfaces.add(this);

        for (AdvancementPositioner child : children) {
            childrenInterfaces.add((AdvancementPositionerInterface)child);
        }
        return childrenInterfaces;
    }

    @Override
    public Vector3f planeAdvancements$getClusterSize() {
        if (depth == 0) {
            return new Vector3f(1,0,0);
        }

        float widthMax = 1;
        float heightMax = 0;
        float heightMin = 0;

        Stack<AdvancementPositionerInterface> stack = new Stack<>();
        stack.addAll(planeAdvancements$getChildren(false));
        while (!stack.isEmpty()) {
            AdvancementPositionerInterface positioner = stack.pop();
            stack.addAll(positioner.planeAdvancements$getChildren(false));

            float width = positioner.planeAdvancements$getDepth();
            if (width > widthMax) {
                widthMax = width;
            }

            float height = positioner.planeAdvancements$getRow()-row;
            if (height > heightMax) {
                heightMax = height;
            }
            if (height < heightMin) {
                heightMin = height;
            }
        }

        return new Vector3f(widthMax, heightMin, heightMax);
    }

    @Override
    public float planeAdvancements$getDepth() {
        return depth;
    }

    @Override
    public float planeAdvancements$getRow() {
        return row;
    }
}
