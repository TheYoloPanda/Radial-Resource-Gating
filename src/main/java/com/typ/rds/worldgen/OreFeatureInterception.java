package com.typ.rds.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class OreFeatureInterception {
    private OreFeatureInterception() {
    }

    public static boolean shouldCancel(FeaturePlaceContext<OreConfiguration> context) {
        ResourceKey<Level> dimension = context.level().getLevel().dimension();
        BlockPos origin = context.origin();
        OreConfiguration configuration = context.config();
        return OreGatingPolicy.shouldBlockOreFeature(dimension, origin, configuration);
    }
}
