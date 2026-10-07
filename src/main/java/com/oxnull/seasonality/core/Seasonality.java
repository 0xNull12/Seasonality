/*
 * Copyright (C) 2026 0xNull
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://gnu.org>.
 */
package com.oxnull.seasonality.core;

import com.oxnull.seasonality.command.SeasonalityCommand;
import com.oxnull.seasonality.init.ConfigLoader;
import com.oxnull.seasonality.init.BlockRegistry;
import com.oxnull.seasonality.init.CropFertilityCache;
import com.oxnull.seasonality.init.EventBusRegistrar;
import com.oxnull.seasonality.init.ItemRegistry;
import com.oxnull.seasonality.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
    modid = Seasonality.MOD_ID,
    name = Seasonality.MOD_NAME,
    version = Seasonality.VERSION,
    acceptedMinecraftVersions = "[1.12.2]",
    dependencies = "required-after:mixinbooter@[10.13,);"
)
public class Seasonality {

    public static final String MOD_ID = "seasonality";
    public static final String MOD_NAME = "Seasonality";
    public static final String VERSION = "0.1.0-beta";
    
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static Seasonality INSTANCE;

    @SidedProxy(
        clientSide = "com.oxnull.seasonality.proxy.ClientProxy",
        serverSide = "com.oxnull.seasonality.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static File configDirectory;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDirectory = new File(event.getModConfigurationDirectory(), MOD_ID);
        if (!configDirectory.exists()) configDirectory.mkdirs();

        ConfigLoader.preInit(configDirectory);
        BlockRegistry.registerBlocks();
        ItemRegistry.registerItems();
        EventBusRegistrar.registerPreInitHandlers();

        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerEventListeners();
        LOGGER.info("Seasonality loaded enjoy the weather changes! Does anyone actually read the game log?");
        ConfigLoader.init(configDirectory);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.registerPostEventListeners();
        CropFertilityCache.init();
        EventBusRegistrar.registerPostInitHandlers();
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new SeasonalityCommand());
    }
}
