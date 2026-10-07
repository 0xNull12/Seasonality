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
package com.oxnull.seasonality.season;

import com.google.common.base.Preconditions;
import com.oxnull.seasonality.api.config.SeasonalityOption;
import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.ITemporalState;

public final class TemporalCalendar implements ITemporalState {
    
    public static final TemporalCalendar ZERO = new TemporalCalendar(0);
    public final int time;

    public TemporalCalendar(int time) {
        Preconditions.checkArgument(time >= 0, "Time cannot be negative");
        this.time = time;
    }

    @Override
    public int getDayDuration() {
        int value = SyncedConfig.getIntValue(SeasonalityOption.DAY_DURATION);
        return value > 0 ? value : 24000;
    }

    @Override
    public int getSubPhaseDuration() {
        int dayDuration = getDayDuration();
        int subPhaseDays = SyncedConfig.getIntValue(SeasonalityOption.SUB_SEASON_DURATION);
        return dayDuration * (subPhaseDays > 0 ? subPhaseDays : 7);
    }

    @Override
    public int getSeasonDuration() {
        return getSubPhaseDuration() * 3;
    }

    @Override
    public int getCycleDuration() {
        return getSubPhaseDuration() * AnnualSeason.SubPhase.VALUES.length;
    }

    @Override
    public int getCycleTicks() {
        return this.time;
    }

    @Override
    public int getDay() {
        return this.time / getDayDuration();
    }

    @Override
    public AnnualSeason.SubPhase getSubPhase() {
        int subPhaseDuration = getSubPhaseDuration();
        if (subPhaseDuration <= 0) return AnnualSeason.SubPhase.VALUES[0];

        int index = (this.time / subPhaseDuration) % AnnualSeason.SubPhase.VALUES.length;
        return AnnualSeason.SubPhase.VALUES[index];
    }

    @Override
    public AnnualSeason getAnnualSeason() {
        return this.getSubPhase().getParentSeason();
    }

    @Override
    public AnnualSeason.TropicalCycle getTropicalCycle() {
        int subPhaseDuration = getSubPhaseDuration();
        if (subPhaseDuration <= 0) return AnnualSeason.TropicalCycle.VALUES[0];

        int index = ((((this.time / subPhaseDuration) + 11) / 2) + 5) % AnnualSeason.TropicalCycle.VALUES.length;
        return AnnualSeason.TropicalCycle.VALUES[index];
    }
}
