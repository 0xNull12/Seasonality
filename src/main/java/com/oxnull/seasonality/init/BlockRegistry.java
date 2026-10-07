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
package com.oxnull.seasonality.init;

import com.oxnull.seasonality.block.BlockGreenhouseGlass;
import com.oxnull.seasonality.block.BlockTemporalSensor;
import com.oxnull.seasonality.tileentity.TileEntityTemporalSensor;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.util.inventory.SeasonalityCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockRegistry {
    public static void registerBlocks() {
        registerBlock(new BlockGreenhouseGlass(), "greenhouse_glass");

        registerBlock(new BlockTemporalSensor(BlockTemporalSensor.SeasonalDetectorType.SPRING), "season_sensor_spring");
        registerBlock(new BlockTemporalSensor(BlockTemporalSensor.SeasonalDetectorType.SUMMER), "season_sensor_summer");
        registerBlock(new BlockTemporalSensor(BlockTemporalSensor.SeasonalDetectorType.AUTUMN), "season_sensor_autumn");
        registerBlock(new BlockTemporalSensor(BlockTemporalSensor.SeasonalDetectorType.WINTER), "season_sensor_winter");
		GameRegistry.registerTileEntity(TileEntityTemporalSensor.class, new ResourceLocation(Seasonality.MOD_ID, "season_sensor"));
    }

    private static void registerBlock(Block block, String name) {
        registerBlock(block, name, SeasonalityCreativeTab.INSTANCE);
    }

    private static void registerBlock(Block block, String name, net.minecraft.creativetab.CreativeTabs tab) {
        block.setRegistryName(Seasonality.MOD_ID, name);
        block.setTranslationKey(name);
        if (tab != null) block.setCreativeTab(tab);

        ForgeRegistries.BLOCKS.register(block);

        ItemBlock itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(block.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock);

        Seasonality.proxy.registerItemSided(itemBlock);

        Seasonality.proxy.registerBlockSided(block);
    }
}