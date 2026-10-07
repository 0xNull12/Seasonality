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

/**
 * Enum representing configuration options related to seasons 
 * that are synchronized between server and client
 */
public enum SeasonalityOption implements ISyncedOption {
    
    DAY_DURATION("Day Duration"),
    SUB_SEASON_DURATION("Sub Season Duration"),
    STARTING_SUB_SEASON("Starting Sub Season"),
    PROGRESS_SEASON_WHILE_OFFLINE("Progress Season While Offline"),
    ADVANCE_SEASON_WHILE_SLEEPING("Skip time with Sleeping"),
    CHANGE_DAYLIGHT_WITH_SEASONS("Change Daylight with Seasons");
    
    private final String optionName;

    SeasonalityOption(String name) {
        this.optionName = name;
    }

    @Override
    public String getOptionName() {
        return this.optionName;
    }
}
