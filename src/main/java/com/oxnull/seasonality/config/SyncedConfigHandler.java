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

import com.oxnull.seasonality.api.config.ISyncedOption;
import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.core.Seasonality;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public abstract class SyncedConfigHandler {
    protected final Configuration config;
    protected final String description;

    protected SyncedConfigHandler(File configFile, String description) {
        this.config = new Configuration(configFile);
        this.description = description;
        loadConfiguration();
        MinecraftForge.EVENT_BUS.register(this);
        ModConfigRegistry.registerHandler(this);
    }

    protected abstract void loadConfiguration();

    protected void addSyncedBoolean(ISyncedOption option, boolean defaultValue, String category, String comment) {
        boolean val = config.getBoolean(option.getOptionName(), category, defaultValue, comment);
        SyncedConfig.addOption(option, String.valueOf(val));
    }

    protected void addSyncedInteger(ISyncedOption option, int defaultValue, String category, String comment, int min, int max) {
        int val = config.getInt(option.getOptionName(), category, defaultValue, min, max, comment);
        SyncedConfig.addOption(option, String.valueOf(val));
    }

    protected void addSyncedFloat(ISyncedOption option, float defaultValue, String category, String comment, float min, float max) {
        float val = config.getFloat(option.getOptionName(), category, defaultValue, min, max, comment);
        SyncedConfig.addOption(option, String.valueOf(val));
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(Seasonality.MOD_ID)) {
            loadConfiguration();
        }
    }
}
