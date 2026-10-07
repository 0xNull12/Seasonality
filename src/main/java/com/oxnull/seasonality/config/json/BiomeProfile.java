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
package com.oxnull.seasonality.config.json;

public class BiomeProfile {
    
    public final boolean affectedBySeasons;
    public final boolean usesTropicalCycle;
    public final boolean disablesCropGrowth;

    public BiomeProfile(boolean affectedBySeasons, boolean usesTropicalCycle, boolean disablesCropGrowth) {
        this.affectedBySeasons = affectedBySeasons;
        this.usesTropicalCycle = usesTropicalCycle;
        this.disablesCropGrowth = disablesCropGrowth;
    }
}
