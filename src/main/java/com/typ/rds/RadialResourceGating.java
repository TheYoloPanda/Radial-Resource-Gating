package com.typ.rds;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(RadialResourceGating.MODID)
public class RadialResourceGating {
    public static final String MODID = "radialresourcegating";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RadialResourceGating(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onConfigLoad);
        modEventBus.addListener(Config::onConfigReload);
    }
}
