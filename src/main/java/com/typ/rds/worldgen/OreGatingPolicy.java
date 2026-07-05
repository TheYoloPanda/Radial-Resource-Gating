package com.typ.rds.worldgen;

import com.typ.rds.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

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

        long minX = (long) gate.centerX() - gate.range();
        long maxX = (long) gate.centerX() + gate.range();
        long minZ = (long) gate.centerZ() - gate.range();
        long maxZ = (long) gate.centerZ() + gate.range();
        return origin.getX() < minX
                || origin.getX() > maxX
                || origin.getZ() < minZ
                || origin.getZ() > maxZ;
    }
}
