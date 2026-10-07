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

import com.oxnull.seasonality.api.config.SeasonalityOption;
import com.oxnull.seasonality.core.Seasonality;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TemporalConfig extends SyncedConfigHandler {
    public static final String CAT_TIME = "Time Settings";
    public static final String CAT_WEATHER = "Weather Settings";
    public static final String CAT_AESTHETICS = "Aesthetic Settings";
    public static final String CAT_DIMENSIONS = "Dimension Settings";

    public static TemporalConfig instance;

    public boolean generateSnow;
    public boolean generateIce;
    public boolean changeWeatherFrequency;

    public boolean changeGrassColour;
    public boolean changeFoliageColour;
    public boolean changeBirchColour;

    private static volatile Set<Integer> whitelistedDimensionsCache = Collections.emptySet();

    public TemporalConfig(File configFile) {
        super(configFile, "TemporalClimatics Settings");
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void loadConfiguration() {
        try {
            // Time Settings
            addSyncedInteger(SeasonalityOption.DAY_DURATION, 24000, CAT_TIME, "The duration of a Minecraft day in ticks", 20, Integer.MAX_VALUE);
            addSyncedInteger(SeasonalityOption.SUB_SEASON_DURATION, 7, CAT_TIME, "The duration of a sub-phase in days", 1, Integer.MAX_VALUE);
            addSyncedInteger(SeasonalityOption.STARTING_SUB_SEASON, 5, CAT_TIME, "Starting sub-phase (0=Random, 1-3=Spring, 4-6=Summer, 7-9=Autumn, 10-12=Winter)", 0, 12);
            addSyncedBoolean(SeasonalityOption.PROGRESS_SEASON_WHILE_OFFLINE, true, CAT_TIME, "Progress seasons while server is empty");
            addSyncedBoolean(SeasonalityOption.ADVANCE_SEASON_WHILE_SLEEPING, true, CAT_TIME, "Advance seasons when all players sleep");
            addSyncedBoolean(SeasonalityOption.CHANGE_DAYLIGHT_WITH_SEASONS, true, CAT_TIME, "Modify sunrise/sunset times based on season");

            // Weather Settings
            generateSnow = config.getBoolean("Generate Snow", CAT_WEATHER, true, "Enable snow generation in winter");
            generateIce = config.getBoolean("Generate Ice", CAT_WEATHER, true, "Enable water freezing in winter");
            changeWeatherFrequency = config.getBoolean("Change Weather Frequency", CAT_WEATHER, true, "Modify rain/snow frequency per season");

            // Aesthetic Settings
            changeGrassColour = config.getBoolean("Change Grass Colour", CAT_AESTHETICS, true, "Tint grass based on season");
            changeFoliageColour = config.getBoolean("Change Foliage Colour", CAT_AESTHETICS, true, "Tint leaves based on season");
            changeBirchColour = config.getBoolean("Change Birch Colour", CAT_AESTHETICS, true, "Tint birch leaves based on season");

            // Dimension Settings
            String[] dims = config.getStringList("Whitelisted Dimensions", CAT_DIMENSIONS, new String[]{"0"}, "Dimensions where seasons apply");

            Set<Integer> newCache = new HashSet<>();
            for (String dimStr : dims) {
                try {
                    newCache.add(Integer.parseInt(dimStr.trim()));
                } catch (NumberFormatException e) {
                    Seasonality.LOGGER.warn("Invalid dimension ID in the configuration: '{}'", dimStr);
                }
            }
            whitelistedDimensionsCache = Collections.unmodifiableSet(newCache);
        } catch (Exception e) {
            Seasonality.LOGGER.error("Failed to load the configuration", e);
        } finally {
            if (config.hasChanged()) config.save();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if (event.getModID().equals(Seasonality.MOD_ID)) {
            Seasonality.LOGGER.info("Updated configuration");
            loadConfiguration();
        }
    }

    public static boolean isDimensionWhitelisted(int dimension) {
        return whitelistedDimensionsCache.contains(dimension);
    }
}