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
 * Provides color palettes and saturation multipliers for foliage and grass
 * based on the current climatic phase
 */
public interface ISeasonalPalette {
    
    /** @return The hex color overlay applied to grass blocks */
    int getGrassOverlay();
    
    /** @return The saturation multiplier for grass. -1 implies no change */
    float getGrassSaturationMultiplier();
    
    /** @return The hex color overlay applied to leaves/foliage */
    int getFoliageOverlay();
    
    /** @return The saturation multiplier for foliage. -1 implies no change */
    float getFoliageSaturationMultiplier();
    
    /** @return The specific tint color applied to Birch leaves */
    int getBirchColor();
}
