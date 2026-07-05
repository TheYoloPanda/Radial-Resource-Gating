package com.typ.rds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final DimensionConfig OVERWORLD = defineDimension("overworld", 128);
    static final DimensionConfig NETHER = defineDimension("nether", 384);
    static final DimensionConfig END = defineDimension("end", 128);

    static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_TAGS = defineStringList(
            "blockedTags",
            List.of("c:ores"),
            "c:ores"
    );
    static final ModConfigSpec.ConfigValue<List<? extends String>> ADDITIONAL_BLOCKED_BLOCKS = defineStringList(
            "additionalBlockedBlocks",
            List.of(),
            ""
    );
    static final ModConfigSpec.ConfigValue<List<? extends String>> OUTSIDE_ALLOWED_BLOCKS = defineStringList(
            "outsideAllowedBlocks",
            List.of(),
            ""
    );

    static final ModConfigSpec SPEC = BUILDER.build();
    private static final Set<String> WARNED_INVALID_CONFIG_VALUES = ConcurrentHashMap.newKeySet();
    private static volatile ResolvedConfig resolved = resolveRawConfig();

    private Config() {
    }

    private static DimensionConfig defineDimension(String name, int defaultRange) {
        BUILDER.push(name);
        try {
            return new DimensionConfig(
                    BUILDER.define("enabled", true),
                    BUILDER.defineInRange("centerX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
                    BUILDER.defineInRange("centerZ", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
                    BUILDER.defineInRange("range", defaultRange, 0, Integer.MAX_VALUE)
            );
        } finally {
            BUILDER.pop();
        }
    }

    private static ModConfigSpec.ConfigValue<List<? extends String>> defineStringList(
            String name,
            List<String> defaults,
            String defaultElement
    ) {
        return BUILDER.defineListAllowEmpty(name, defaults, () -> defaultElement, value -> value instanceof String);
    }

    private static List<String> rawStringList(ModConfigSpec.ConfigValue<List<? extends String>> value) {
        return List.copyOf(value.get());
    }

    private static ResolvedConfig resolveRawConfig() {
        Map<ResourceKey<Level>, DimensionGate> dimensions = new HashMap<>();
        dimensions.put(Level.OVERWORLD, resolveDimension(OVERWORLD));
        dimensions.put(Level.NETHER, resolveDimension(NETHER));
        dimensions.put(Level.END, resolveDimension(END));

        return new ResolvedConfig(
                Map.copyOf(dimensions),
                resolveTags("blockedTags", rawStringList(BLOCKED_TAGS)),
                resolveBlocks("additionalBlockedBlocks", rawStringList(ADDITIONAL_BLOCKED_BLOCKS)),
                resolveBlocks("outsideAllowedBlocks", rawStringList(OUTSIDE_ALLOWED_BLOCKS))
        );
    }

    private static DimensionGate resolveDimension(DimensionConfig config) {
        return new DimensionGate(
                config.enabled().getAsBoolean(),
                config.centerX().getAsInt(),
                config.centerZ().getAsInt(),
                config.range().getAsInt()
        );
    }

    private static Set<TagKey<Block>> resolveTags(String listName, List<String> rawValues) {
        Set<TagKey<Block>> tags = new HashSet<>();
        for (String rawValue : rawValues) {
            ResourceLocation id = ResourceLocation.tryParse(rawValue);
            if (id != null) {
                tags.add(TagKey.create(Registries.BLOCK, id));
            } else {
                warnInvalidOnce(listName, rawValue, "malformed tag id");
            }
        }
        return Set.copyOf(tags);
    }

    private static Set<Block> resolveBlocks(String listName, List<String> rawValues) {
        Set<Block> blocks = new HashSet<>();
        for (String rawValue : rawValues) {
            ResourceLocation id = ResourceLocation.tryParse(rawValue);
            if (id == null) {
                warnInvalidOnce(listName, rawValue, "malformed block id");
            } else if (BuiltInRegistries.BLOCK.containsKey(id)) {
                blocks.add(BuiltInRegistries.BLOCK.get(id));
            } else {
                warnInvalidOnce(listName, rawValue, "unknown block id");
            }
        }
        return Set.copyOf(blocks);
    }

    private static void warnInvalidOnce(String listName, String rawValue, String reason) {
        String warningKey = listName + "|" + rawValue;
        if (WARNED_INVALID_CONFIG_VALUES.add(warningKey)) {
            RadialResourceGating.LOGGER.warn("Ignoring invalid {} entry '{}': {}", listName, rawValue, reason);
        }
    }

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            resolved = resolveRawConfig();
        }
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            resolved = resolveRawConfig();
        }
    }

    public static ResolvedConfig resolved() {
        return resolved;
    }

    record DimensionConfig(
            ModConfigSpec.BooleanValue enabled,
            ModConfigSpec.IntValue centerX,
            ModConfigSpec.IntValue centerZ,
            ModConfigSpec.IntValue range
    ) {
    }

    public record DimensionGate(boolean enabled, int centerX, int centerZ, int range) {
    }

    public record ResolvedConfig(
            Map<ResourceKey<Level>, DimensionGate> dimensions,
            Set<TagKey<Block>> blockedTags,
            Set<Block> additionalBlockedBlocks,
            Set<Block> outsideAllowedBlocks
    ) {
    }
}
