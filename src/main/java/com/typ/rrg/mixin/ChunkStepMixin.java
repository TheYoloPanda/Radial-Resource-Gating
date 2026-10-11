package com.typ.rrg.mixin;

import com.typ.rrg.worldgen.OreGateResolver;
import com.typ.rrg.worldgen.OreVeinGenerationContext;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkStep.class)
public abstract class ChunkStepMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void radialresourcegating$registerOreVeinDimension(
            WorldGenContext worldGenContext,
            StaticCache2D<GenerationChunkHolder> cache,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback
    ) {
        OreVeinGenerationContext.registerChunk(chunk, OreGateResolver.resolve(worldGenContext.level()));
    }

    @Inject(method = "apply", at = @At("RETURN"))
    private void radialresourcegating$unregisterOreVeinDimension(
            WorldGenContext worldGenContext,
            StaticCache2D<GenerationChunkHolder> cache,
            ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback
    ) {
        callback.getReturnValue().whenComplete((result, throwable) -> OreVeinGenerationContext.unregisterChunk(chunk));
    }
}
