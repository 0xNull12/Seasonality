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
package com.oxnull.seasonality.config;

import com.oxnull.seasonality.core.Seasonality;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Seasonality.MOD_ID, name = "seasonality_climate", category = "")
@Mod.EventBusSubscriber(modid = Seasonality.MOD_ID)
public class ClimateConfig {
    public static General general = new General();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Seasonality.MOD_ID)) {
            ConfigManager.sync(Seasonality.MOD_ID, Config.Type.INSTANCE);
            Seasonality.LOGGER.info("ClimateConfig reloaded in-game: shouldRecalculateSnow={}, delay={}min",
                    general.shouldRecalculateSnow, general.timeToRecalculateSnow);
        }
    }

    public static class General {
        @Config.Comment("If true, the server recalculates snow/ice on chunk load to match the current season")
        public boolean shouldRecalculateSnow = true;

        @Config.Comment("Minutes that must pass since a chunk's las recalculation before it can be recalculated again. 0 = always recalculate")
        @Config.RangeInt(min = 0, max = 1440)
        public int timeToRecalculateSnow = 20;
    }
}