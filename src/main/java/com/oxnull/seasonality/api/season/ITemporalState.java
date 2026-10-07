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
package com.oxnull.seasonality.api.season;

/**
 * Represents the current state of the temporal/seasonal cycle in a world
 */
public interface ITemporalState {

    /** @return The duration of a single day in ticks */
    int getDayDuration();

    /** @return The duration of a single sub-phase in ticks */
    int getSubPhaseDuration();

    /** @return The duration of a full annual season in ticks */
    int getSeasonDuration();

    /** @return The duration of an entire cycle (a year) in ticks */
    int getCycleDuration();

    /** @return The total elapsed ticks in the current overall cycle */
    int getCycleTicks();

    /** @return The current day number in the cycle */
    int getDay();

    /** @return The current micro-season/sub-phase */
    AnnualSeason.SubPhase getSubPhase();

    /** @return The current primary annual season */
    AnnualSeason getAnnualSeason();

    /** @return The current tropical cycle (Wet/Dry) */
    AnnualSeason.TropicalCycle getTropicalCycle();
}
