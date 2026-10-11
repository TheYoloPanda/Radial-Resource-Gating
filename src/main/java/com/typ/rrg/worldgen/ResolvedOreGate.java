package com.typ.rrg.worldgen;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record ResolvedOreGate(
        ResourceKey<Level> dimension,
        int centerX,
        int centerZ,
        int range,
        Set<Block> insideBlockedBlocks
) {
}
