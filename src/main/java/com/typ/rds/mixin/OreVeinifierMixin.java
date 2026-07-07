package com.typ.rds.mixin;

import com.typ.rds.worldgen.OreGatingPolicy;
import com.typ.rds.worldgen.OreVeinGenerationContext;
import com.typ.rds.worldgen.ResolvedOreGate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.OreVeinifier;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinifier.class)
public abstract class OreVeinifierMixin {
    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void radialresourcegating$blockOutsideGate(
            DensityFunction veinToggle,
            DensityFunction veinRidged,
            DensityFunction veinGap,
            PositionalRandomFactory random,
            CallbackInfoReturnable<NoiseChunk.BlockStateFiller> callback
    ) {
        ResolvedOreGate gate = OreVeinGenerationContext.currentGate();
        if (gate == null) {
            return;
        }

        NoiseChunk.BlockStateFiller vanillaFiller = callback.getReturnValue();
        callback.setReturnValue(context -> calculateInsideGate(gate, vanillaFiller, context));
    }

    private static BlockState calculateInsideGate(
            ResolvedOreGate gate,
            NoiseChunk.BlockStateFiller vanillaFiller,
            DensityFunction.FunctionContext context
    ) {
        if (OreGatingPolicy.shouldBlockOreVein(gate, context.blockX(), context.blockZ())) {
            return null;
        }
        return vanillaFiller.calculate(context);
    }
}
