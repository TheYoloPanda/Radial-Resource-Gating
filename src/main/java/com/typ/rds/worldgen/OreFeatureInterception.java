package com.typ.rds.worldgen;

import com.typ.rds.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class OreFeatureInterception {
    private OreFeatureInterception() {
    }

    public static boolean shouldCancel(FeaturePlaceContext<OreConfiguration> context) {
        Config.ResolvedConfig config = Config.resolved();
        ResolvedOreGate gate = OreGateResolver.resolve(config, context.level().getLevel());
        if (gate == null) {
            return false;
        }

        BlockPos origin = context.origin();
        OreConfiguration configuration = context.config();
        return OreGatingPolicy.shouldBlockOreFeature(config, gate, origin, configuration);
    }
}
