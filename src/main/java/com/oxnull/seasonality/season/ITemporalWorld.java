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

import com.oxnull.seasonality.api.season.AnnualSeason;
import net.minecraft.util.math.BlockPos;

public interface ITemporalWorld {
    boolean canSnowAtInSeason(BlockPos pos, boolean checkLight, AnnualSeason season);
    boolean canBlockFreezeInSeason(BlockPos pos, boolean noWaterAdj, AnnualSeason season);
}
