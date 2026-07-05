package com.typ.rds.worldgen;

import com.typ.rds.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class OreGatingPolicy {
    private OreGatingPolicy() {
    }

    public static boolean isOutsideEnabledGate(
            Config.ResolvedConfig config,
            ResourceKey<Level> dimension,
            BlockPos origin
    ) {
        Config.DimensionGate gate = config.dimensions().get(dimension);
        if (gate == null || !gate.enabled()) {
            return false;
        }

        return !isInsideInclusiveSquare(gate, origin);
    }

    private static boolean isInsideInclusiveSquare(Config.DimensionGate gate, BlockPos origin) {
        long minX = (long) gate.centerX() - gate.range();
        long maxX = (long) gate.centerX() + gate.range();
        long minZ = (long) gate.centerZ() - gate.range();
        long maxZ = (long) gate.centerZ() + gate.range();
        return origin.getX() >= minX
                && origin.getX() <= maxX
                && origin.getZ() >= minZ
                && origin.getZ() <= maxZ;
    }

    private static boolean containsBlockedTarget(Config.ResolvedConfig config, OreConfiguration oreConfiguration) {
        for (OreConfiguration.TargetBlockState targetBlockState : oreConfiguration.targetStates) {
            BlockState targetState = targetBlockState.state;
            if (isBlockedTarget(config, targetState)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlockedTarget(Config.ResolvedConfig config, BlockState targetState) {
        Block targetBlock = targetState.getBlock();
        if (config.outsideAllowedBlocks().contains(targetBlock)) {
            return false;
        }

        if (config.additionalBlockedBlocks().contains(targetBlock)) {
            return true;
        }

        for (TagKey<Block> blockedTag : config.blockedTags()) {
            if (targetState.is(blockedTag)) {
                return true;
            }
        }
        return false;
    }
}
