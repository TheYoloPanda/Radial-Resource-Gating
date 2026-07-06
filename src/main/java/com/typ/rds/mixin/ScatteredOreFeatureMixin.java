package com.typ.rds.mixin;

import com.typ.rds.worldgen.OreFeatureInterception;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScatteredOreFeature.class)
public abstract class ScatteredOreFeatureMixin {
    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At("HEAD"), cancellable = true)
    private void radialresourcegating$blockOutsideGate(
            FeaturePlaceContext<OreConfiguration> context,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (OreFeatureInterception.shouldCancel(context)) {
            callback.setReturnValue(false);
        }
    }
}
