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

import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.AgriculturalConfig;
import com.oxnull.seasonality.config.BiomeClimateRegistry;
import com.oxnull.seasonality.config.TemporalConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CropFertilityCache {
    private static final Set<String> springPlants = new HashSet<>();
    private static final Set<String> summerPlants = new HashSet<>();
    private static final Set<String> autumnPlants = new HashSet<>();
    private static final Set<String> winterPlants = new HashSet<>();
    private static final Set<String> allListedPlants = new HashSet<>();

    private static final HashMap<String, Integer> seedSeasons = new HashMap<>();

    public static void init() {
        initSeasonCrops(AgriculturalConfig.fertility.springCrops, springPlants, 1);
        initSeasonCrops(AgriculturalConfig.fertility.summerCrops, summerPlants, 2);
        initSeasonCrops(AgriculturalConfig.fertility.autumnCrops, autumnPlants, 4);
        initSeasonCrops(AgriculturalConfig.fertility.winterCrops, winterPlants, 8);
    }

    public static boolean isCropFertile(String cropName, World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        if (BiomeClimateRegistry.isCropGrowthDisabled(biome)) return false;

        if (!AgriculturalConfig.general.seasonalCrops 
            || !TemporalConfig.isDimensionWhitelisted(world.provider.getDimension()) 
            || !BiomeClimateRegistry.isAffectedBySeasons(biome)) {
            return true;
        }

        if (BiomeClimateRegistry.usesTropicalCycle(biome)) {
            return summerPlants.contains(cropName) || !allListedPlants.contains(cropName);
        } else {
            if (biome.getTemperature(pos) < 0.15F) return winterPlants.contains(cropName);

            AnnualSeason season = SeasonalityAPI.getTemporalState(world).getAnnualSeason();
            switch (season) {
                case SPRING: return springPlants.contains(cropName);
                case SUMMER: return summerPlants.contains(cropName);
                case AUTUMN: return autumnPlants.contains(cropName);
                case WINTER: return winterPlants.contains(cropName);
            }
            
            if (!allListedPlants.contains(cropName)) {
                return season != AnnualSeason.WINTER || AgriculturalConfig.general.ignoreUnlistedCrops;
            }
        }
        return false;
    }

    private static void initSeasonCrops(String[] seeds, Set<String> cropSet, int bitmask) {
        for (String seedId : seeds) {
            ResourceLocation rl = new ResourceLocation(seedId);
            Item item = ForgeRegistries.ITEMS.getValue(rl);

            String plantName = null;

            if (item instanceof IPlantable) {
                IBlockState plantState = ((IPlantable) item).getPlant(null, null);
                if (plantState != null) plantName = plantState.getBlock().getRegistryName().toString();
            } else {
                Block block = ForgeRegistries.BLOCKS.getValue(rl);
                if (block != null && block != Blocks.AIR) plantName = block.getRegistryName().toString();
            }

            if (plantName != null) {
                cropSet.add(plantName);
                if (bitmask != 0) {
                    allListedPlants.add(plantName);
                    seedSeasons.merge(seedId, bitmask, (oldVal, newVal) -> oldVal | newVal);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void setupTooltips(ItemTooltipEvent event) {
        if (AgriculturalConfig.general.enableCropTooltips && AgriculturalConfig.general.seasonalCrops) {
            ResourceLocation rl = event.getItemStack().getItem().getRegistryName();
            if (rl == null) return;

            Integer mask = seedSeasons.get(rl.toString());
            if (mask != null) {
                event.getToolTip().add(I18n.format("tooltip.seasonality.fertile_season"));
                if ((mask & 15) == 15) {
                    event.getToolTip().add(TextFormatting.LIGHT_PURPLE + I18n.format("tooltip.seasonality.season.all"));
                } else {
                    if ((mask & 1) != 0) event.getToolTip().add(TextFormatting.GREEN + I18n.format("tooltip.seasonality.season.spring"));
                    if ((mask & 2) != 0) event.getToolTip().add(TextFormatting.YELLOW + I18n.format("tooltip.seasonality.season.summer"));
                    if ((mask & 4) != 0) event.getToolTip().add(TextFormatting.GOLD + I18n.format("tooltip.seasonality.season.autumn"));
                    if ((mask & 8) != 0) event.getToolTip().add(TextFormatting.AQUA + I18n.format("tooltip.seasonality.season.winter"));
                }
            }
        }
    }
}