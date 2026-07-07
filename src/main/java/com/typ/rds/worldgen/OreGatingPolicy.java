package com.typ.rds.worldgen;

import com.typ.rds.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class OreGatingPolicy {
    private OreGatingPolicy() {
    }

    public static boolean isOutsideEnabledGate(
            ResolvedOreGate gate,
            BlockPos origin
    ) {
        return !isInsideInclusiveSquare(gate, origin);
    }

    public static boolean shouldBlockOreFeature(
            Config.ResolvedConfig config,
            ResolvedOreGate gate,
            BlockPos origin,
            OreConfiguration oreConfiguration
    ) {
        if (!isOutsideEnabledGate(gate, origin)) {
            return false;
        }

        return containsBlockedTarget(config, oreConfiguration);
    }

    public static boolean shouldBlockOreVein(ResolvedOreGate gate, int x, int z) {
        return !isInsideInclusiveSquare(gate, x, z);
    }

    private static boolean isInsideInclusiveSquare(ResolvedOreGate gate, BlockPos origin) {
        return isInsideInclusiveSquare(gate, origin.getX(), origin.getZ());
    }

    private static boolean isInsideInclusiveSquare(ResolvedOreGate gate, int x, int z) {
        long minX = (long) gate.centerX() - gate.range();
        long maxX = (long) gate.centerX() + gate.range();
        long minZ = (long) gate.centerZ() - gate.range();
        long maxZ = (long) gate.centerZ() + gate.range();
        return x >= minX
                && x <= maxX
                && z >= minZ
                && z <= maxZ;
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
