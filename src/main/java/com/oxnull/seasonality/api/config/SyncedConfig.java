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
package com.oxnull.seasonality.api.config;

import com.oxnull.seasonality.core.Seasonality;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages configuration options that need to be synchronized 
 * between the server and clients
 */
public class SyncedConfig {

    // Private map to encapsulate the state and prevent external tampering
    private static final Map<String, SyncedConfigEntry> OPTIONS_TO_SYNC = new HashMap<>();

    /**
     * Registers a new synced option with its default value
     */
    public static void addOption(ISyncedOption option, String defaultValue) {
        OPTIONS_TO_SYNC.put(option.getOptionName(), new SyncedConfigEntry(defaultValue));
    }

    /**
     * Gets the boolean value of a synced option
     * Returns false if the value is invalid or missing
     */
    public static boolean getBooleanValue(ISyncedOption option) {
        String value = getValue(option);
        return value != null && Boolean.parseBoolean(value);
    }

    /**
     * Gets the integer value of a synced option
     * Returns 0 if the value is invalid or missin
     */
    public static int getIntValue(ISyncedOption option) {
        String value = getValue(option);
        if (value == null) return 0;
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Seasonality.LOGGER.warn("Failed to parse integer for option {}: {}", option.getOptionName(), value);
            return 0;
        }
    }

    /**
     * Gets the raw string value of a synced option
     */
    public static String getValue(ISyncedOption option) {
        SyncedConfigEntry entry = OPTIONS_TO_SYNC.get(option.getOptionName());
        return entry != null ? entry.value : null;
    }

    /**
     * Restores all synced options to their default values
     */
    public static void restoreDefaults() {
        for (SyncedConfigEntry entry : OPTIONS_TO_SYNC.values()) {
            entry.value = entry.defaultValue;
        }
    }

    /**
     * Internal class to hold the current and default values of a config entry
     */
    public static class SyncedConfigEntry {
        public String value;
        public final String defaultValue;

        public SyncedConfigEntry(String defaultValue) {
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }
    }
    
    public static Map<String, SyncedConfigEntry> getOptions() { return OPTIONS_TO_SYNC; }
}
