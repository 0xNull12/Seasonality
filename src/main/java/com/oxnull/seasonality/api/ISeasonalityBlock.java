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

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;

public interface ISeasonalityBlock {
    
    /**
     * @return The class of the ItemBlock used to register this block
     */
    Class<? extends ItemBlock> getItemClass();
    
    /**
     * @return Properties that should be preset when the block is placed
     */
    IProperty<?>[] getPresetProperties();
    
    /**
     * @return Properties that do not affect the block's rendering (metadata)
     */
    IProperty<?>[] getNonRenderingProperties();
    
    /**
     * Gets the registry name variant for a specific block state
     * @param state The block state
     * @return The string representation of the state for the model
     */
    String getStateName(IBlockState state);
}
