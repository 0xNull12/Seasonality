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
package com.oxnull.seasonality.item;

import com.google.common.collect.ImmutableSet;
import com.oxnull.seasonality.api.ISeasonalityBlock;
import com.oxnull.seasonality.util.BlockStateUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSeasonalityBlock extends ItemBlock {
    private final ISeasonalityBlock seasonalityBlock;

    public ItemSeasonalityBlock(Block block) {
        super(block);
        if (block instanceof ISeasonalityBlock) {
            this.seasonalityBlock = (ISeasonalityBlock) block;
        } else {
            throw new IllegalArgumentException("ItemSeasonalityBlock must be created with a block implementing ISeasonalityBlock");
        }
        this.setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (this.isInCreativeTab(tab)) {
            ImmutableSet<IBlockState> presets = BlockStateUtils.getBlockPresets(this.block);
            if (presets.isEmpty()) {
                subItems.add(new ItemStack(this.block, 1, 0));
            } else {
                for (IBlockState state : presets) {
                    subItems.add(new ItemStack(this.block, 1, this.block.getMetaFromState(state)));
                }
            }
        }
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        ImmutableSet<IBlockState> presets = BlockStateUtils.getBlockPresets(this.block);
        if (presets.isEmpty()) {
            return super.getTranslationKey();
        } else {
            int meta = stack.getMetadata();
            IBlockState oldState = block.getStateFromMeta(meta);
            IBlockState newState = BlockStateUtils.getPresetState(oldState);
            return super.getTranslationKey() + "." + seasonalityBlock.getStateName(newState);
        }
    }
}
