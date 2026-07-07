package com.typ.rds.mixin;

import com.typ.rds.worldgen.OreVeinGenerationContext;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {
    @Inject(method = "forChunk", at = @At("HEAD"))
    private static void radialresourcegating$enterOreVeinDimension(
            ChunkAccess chunk,
            RandomState state,
            DensityFunctions.BeardifierOrMarker beardifierOrMarker,
            NoiseGeneratorSettings noiseGeneratorSettings,
            Aquifer.FluidPicker fluidPicker,
            Blender blender,
            CallbackInfoReturnable<NoiseChunk> callback
    ) {
        OreVeinGenerationContext.enterNoiseChunk(chunk);
    }

    @Inject(method = "forChunk", at = @At("RETURN"))
    private static void radialresourcegating$exitOreVeinDimension(
            ChunkAccess chunk,
            RandomState state,
            DensityFunctions.BeardifierOrMarker beardifierOrMarker,
            NoiseGeneratorSettings noiseGeneratorSettings,
            Aquifer.FluidPicker fluidPicker,
            Blender blender,
            CallbackInfoReturnable<NoiseChunk> callback
    ) {
        OreVeinGenerationContext.exitNoiseChunk(chunk);
    }
}
