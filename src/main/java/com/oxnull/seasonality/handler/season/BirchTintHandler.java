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
package com.oxnull.seasonality.handler.season;

import com.oxnull.seasonality.api.season.ISeasonalPalette;
import com.oxnull.seasonality.config.BiomeClimateRegistry;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.init.ConfigLoader;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;

import javax.annotation.Nullable;

public class BirchTintHandler {
    public static void init() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
            @Override
            public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
                BlockPlanks.EnumType plankType = state.getValue(BlockOldLeaf.VARIANT);

                if (plankType == BlockPlanks.EnumType.SPRUCE) {
                    return ColorizerFoliage.getFoliageColorPine();
                } else if (plankType == BlockPlanks.EnumType.BIRCH) {
                    int birchColor = ColorizerFoliage.getFoliageColorBirch();
                    EntityPlayer player = Minecraft.getMinecraft().player;
                    
                    if (player == null || worldIn == null || pos == null) return birchColor;

                    int dimension = player.dimension;
                    if (ConfigLoader.temporalConfig.changeBirchColour && TemporalConfig.isDimensionWhitelisted(dimension)) {
                        Biome biome = worldIn.getBiome(pos);
                        if (BiomeClimateRegistry.isAffectedBySeasons(biome)) {
                            ISeasonalPalette palette = BiomeClimateRegistry.usesTropicalCycle(biome)
                                ? TemporalManager.getClientCalendar().getTropicalCycle() 
                                : TemporalManager.getClientCalendar().getSubPhase();
                            birchColor = palette.getBirchColor();
                        }
                    }
                    return birchColor;
                } else {
                    return worldIn != null && pos != null ? BiomeColorHelper.getFoliageColorAtPos(worldIn, pos) : ColorizerFoliage.getFoliageColorBasic();
                }
            }
        }, Blocks.LEAVES);
    }
}