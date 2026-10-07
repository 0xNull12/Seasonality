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
package com.oxnull.seasonality.api;

import com.oxnull.seasonality.core.Seasonality;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(Seasonality.MOD_ID)
public class SeasonalityBlocks {

    @ObjectHolder("greenhouse_glass")
    public static final Block GREENHOUSE_GLASS = null;

    @ObjectHolder("season_sensor_spring")
    public static final Block SEASON_SENSOR_SPRING = null;

    @ObjectHolder("season_sensor_summer")
    public static final Block SEASON_SENSOR_SUMMER = null;

    @ObjectHolder("season_sensor_autumn")
    public static final Block SEASON_SENSOR_AUTUMN = null;

    @ObjectHolder("season_sensor_winter")
    public static final Block SEASON_SENSOR_WINTER = null;

    private SeasonalityBlocks() {}
}
