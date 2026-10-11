package com.typ.rrg.worldgen;

import com.typ.rrg.Config;
import com.typ.rrg.RadialResourceGating;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class OreGateResolver {
    private static final Set<String> WARNED_UNRESOLVED_CENTERS = ConcurrentHashMap.newKeySet();

    private OreGateResolver() {
    }

    public static ResolvedOreGate resolve(ServerLevel level) {
        return resolve(Config.resolved(), level);
    }

    public static ResolvedOreGate resolve(Config.ResolvedConfig config, ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        Config.DimensionGate gate = findEnabledGate(config, dimension);
        if (gate == null) {
            return null;
        }

        if (gate.centerMode() == Config.CenterMode.FIXED) {
            return resolveFixedGate(dimension, gate);
        }

        return resolveWorldSpawnGate(level, gate);
    }

    static Config.DimensionGate findEnabledGate(Config.ResolvedConfig config, ResourceKey<Level> dimension) {
        Config.DimensionGate gate = config.dimensions().get(dimension);
        return gate != null && gate.enabled() ? gate : null;
    }

    static ResolvedOreGate resolveFixedGate(ResourceKey<Level> dimension, Config.DimensionGate gate) {
        return new ResolvedOreGate(
                dimension,
                gate.centerX(),
                gate.centerZ(),
                gate.range(),
                gate.insideBlockedBlocks()
        );
    }

    private static ResolvedOreGate resolveWorldSpawnGate(ServerLevel level, Config.DimensionGate gate) {
        ServerLevel overworld = level.getServer().overworld();
        double scale = DimensionType.getTeleportationScale(overworld.dimensionType(), level.dimensionType());
        if (!Double.isFinite(scale)) {
            warnUnresolvedCenter(level.dimension(), "invalid dimension coordinate scale");
            return null;
        }

        BlockPos spawn = overworld.getSharedSpawnPos();
        return resolveWorldSpawnGate(level.dimension(), gate, spawn, scale);
    }

    static ResolvedOreGate resolveWorldSpawnGate(
            ResourceKey<Level> dimension,
            Config.DimensionGate gate,
            BlockPos spawn,
            double scale
    ) {
        int centerX = scaleCoordinate(spawn.getX(), scale);
        int centerZ = scaleCoordinate(spawn.getZ(), scale);
        return new ResolvedOreGate(dimension, centerX, centerZ, gate.range(), gate.insideBlockedBlocks());
    }

    private static int scaleCoordinate(int coordinate, double scale) {
        double scaled = coordinate * scale;
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (scaled < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return Mth.floor(scaled);
    }

    private static void warnUnresolvedCenter(ResourceKey<Level> dimension, String reason) {
        String warningKey = dimension.location() + "|" + reason;
        if (WARNED_UNRESOLVED_CENTERS.add(warningKey)) {
            RadialResourceGating.LOGGER.warn(
                    "Could not resolve world-spawn ore gate center for dimension {}: {}",
                    dimension.location(),
                    reason
            );
        }
    }
}
