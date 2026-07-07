package com.typ.rds.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ResolvedOreGate(ResourceKey<Level> dimension, int centerX, int centerZ, int range) {
}
