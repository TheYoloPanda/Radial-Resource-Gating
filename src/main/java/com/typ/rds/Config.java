package com.typ.rds;

import java.util.List;
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

    record DimensionConfig(
            ModConfigSpec.BooleanValue enabled,
            ModConfigSpec.IntValue centerX,
            ModConfigSpec.IntValue centerZ,
            ModConfigSpec.IntValue range
    ) {
    }
}
