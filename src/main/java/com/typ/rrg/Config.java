package com.typ.rrg;

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
    private static final boolean DEFAULT_DIMENSION_ENABLED = true;
    private static final CenterMode DEFAULT_CENTER_MODE = CenterMode.WORLD_SPAWN;
    private static final int DEFAULT_CENTER = 0;
    private static final int DEFAULT_OVERWORLD_RANGE = 256;
    private static final int DEFAULT_NETHER_RANGE = 256;
    private static final int DEFAULT_END_RANGE = 256;
    private static final List<String> DEFAULT_BLOCKED_TAGS = List.of("c:ores");

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final DimensionConfig OVERWORLD = defineDimension("overworld", DEFAULT_OVERWORLD_RANGE);
    static final DimensionConfig NETHER = defineDimension("nether", DEFAULT_NETHER_RANGE);
    static final DimensionConfig END = defineDimension("end", DEFAULT_END_RANGE);

    static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_TAGS = defineStringList(
            "blockedTags",
            DEFAULT_BLOCKED_TAGS,
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
    private static volatile ResolvedConfig resolved = resolveDefaultConfig();

    private Config() {
    }

    private static DimensionConfig defineDimension(String name, int defaultRange) {
        BUILDER.push(name);
        try {
            return new DimensionConfig(
                    BUILDER.define("enabled", true),
                    BUILDER.define(
                            "centerMode",
                            DEFAULT_CENTER_MODE.configValue(),
                            CenterMode::isConfigValue
                    ),
                    BUILDER.defineInRange("centerX", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
                    BUILDER.defineInRange("centerZ", 0, Integer.MIN_VALUE, Integer.MAX_VALUE),
                    BUILDER.defineInRange("range", defaultRange, 0, Integer.MAX_VALUE),
                    defineStringList("insideBlockedBlocks", List.of(), "")
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
        dimensions.put(Level.OVERWORLD, resolveDimension("overworld", OVERWORLD));
        dimensions.put(Level.NETHER, resolveDimension("nether", NETHER));
        dimensions.put(Level.END, resolveDimension("end", END));

        return new ResolvedConfig(
                Map.copyOf(dimensions),
                resolveTags("blockedTags", rawStringList(BLOCKED_TAGS)),
                resolveBlocks("additionalBlockedBlocks", rawStringList(ADDITIONAL_BLOCKED_BLOCKS)),
                resolveBlocks("outsideAllowedBlocks", rawStringList(OUTSIDE_ALLOWED_BLOCKS))
        );
    }

    private static ResolvedConfig resolveDefaultConfig() {
        Map<ResourceKey<Level>, DimensionGate> dimensions = new HashMap<>();
        dimensions.put(Level.OVERWORLD, defaultDimension(DEFAULT_OVERWORLD_RANGE));
        dimensions.put(Level.NETHER, defaultDimension(DEFAULT_NETHER_RANGE));
        dimensions.put(Level.END, defaultDimension(DEFAULT_END_RANGE));

        return new ResolvedConfig(
                Map.copyOf(dimensions),
                resolveTags("blockedTags", DEFAULT_BLOCKED_TAGS),
                Set.of(),
                Set.of()
        );
    }

    private static DimensionGate defaultDimension(int range) {
        return new DimensionGate(
                DEFAULT_DIMENSION_ENABLED,
                DEFAULT_CENTER_MODE,
                DEFAULT_CENTER,
                DEFAULT_CENTER,
                range,
                Set.of()
        );
    }

    private static DimensionGate resolveDimension(String name, DimensionConfig config) {
        return new DimensionGate(
                config.enabled().getAsBoolean(),
                CenterMode.fromConfigValue(name, config.centerMode().get()),
                config.centerX().getAsInt(),
                config.centerZ().getAsInt(),
                config.range().getAsInt(),
                resolveBlocks(name + ".insideBlockedBlocks", rawStringList(config.insideBlockedBlocks()))
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
            ModConfigSpec.ConfigValue<String> centerMode,
            ModConfigSpec.IntValue centerX,
            ModConfigSpec.IntValue centerZ,
            ModConfigSpec.IntValue range,
            ModConfigSpec.ConfigValue<List<? extends String>> insideBlockedBlocks
    ) {
    }

    public enum CenterMode {
        WORLD_SPAWN("world_spawn"),
        FIXED("fixed");

        private final String configValue;

        CenterMode(String configValue) {
            this.configValue = configValue;
        }

        public String configValue() {
            return configValue;
        }

        static List<String> configValues() {
            return List.of(WORLD_SPAWN.configValue, FIXED.configValue);
        }

        static boolean isConfigValue(Object value) {
            if (!(value instanceof String rawValue)) {
                return false;
            }

            for (CenterMode mode : values()) {
                if (mode.configValue.equalsIgnoreCase(rawValue)) {
                    return true;
                }
            }
            return false;
        }

        static CenterMode fromConfigValue(String dimensionName, String rawValue) {
            for (CenterMode mode : values()) {
                if (mode.configValue.equalsIgnoreCase(rawValue)) {
                    return mode;
                }
            }

            warnInvalidOnce(dimensionName + ".centerMode", rawValue, "expected world_spawn or fixed");
            return DEFAULT_CENTER_MODE;
        }
    }

    public record DimensionGate(
            boolean enabled,
            CenterMode centerMode,
            int centerX,
            int centerZ,
            int range,
            Set<Block> insideBlockedBlocks
    ) {
    }

    public record ResolvedConfig(
            Map<ResourceKey<Level>, DimensionGate> dimensions,
            Set<TagKey<Block>> blockedTags,
            Set<Block> additionalBlockedBlocks,
            Set<Block> outsideAllowedBlocks
    ) {
    }
}
