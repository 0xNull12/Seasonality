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
package com.oxnull.seasonality.block;

import com.oxnull.seasonality.api.ISeasonalityBlock;
import com.oxnull.seasonality.item.ItemSeasonalityBlock;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockGreenhouseGlass extends BlockBreakable implements ISeasonalityBlock {
    public BlockGreenhouseGlass() {
        super(Material.GLASS, false);
        this.setHardness(0.3F);
        this.setHarvestLevel("pickaxe", 0);
        this.setSoundType(SoundType.GLASS);
    }

    @Override public Class<? extends ItemBlock> getItemClass() { return ItemSeasonalityBlock.class; }
    @Override public IProperty<?>[] getPresetProperties() { return new IProperty<?>[] {}; }
    @Override public IProperty<?>[] getNonRenderingProperties() { return null; }
    @Override public String getStateName(IBlockState state) { return ""; }

    @Override
    public int quantityDropped(Random random) { return 0; }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.TRANSLUCENT; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    protected boolean canSilkHarvest() { return true; }

    @Override
    public int getLightOpacity(IBlockState state) { return 0; }
}
