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
import com.oxnull.seasonality.api.season.ITemporalState;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.BiomeClimateRegistry;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.init.ConfigLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

/**
 * Determines temperature and climate change calculations based on the current season
 */
public class SeasonalityASMHelper {
    public static boolean canSnowAtInSeason(World world, BlockPos pos, boolean checkLight, @Nullable ITemporalState state) {
        return canSnowAtInSeason(world, pos, checkLight, state, false);
    }

    public static boolean canSnowAtInSeason(World world, BlockPos pos, boolean checkLight, @Nullable ITemporalState state, boolean useUnmodifiedTemperature) {
        Biome biome = world.getBiome(pos);

        if (!BiomeClimateRegistry.isAffectedBySeasons(biome)) {
            return canSnowAtVanilla(world, pos, checkLight, biome);
        }

        boolean dimensionWhitelisted = TemporalConfig.isDimensionWhitelisted(world.provider.getDimension());
        float temperature = biome.getTemperature(pos);

        if (!useUnmodifiedTemperature && dimensionWhitelisted) {
            if (BiomeClimateRegistry.usesTropicalCycle(biome)) return false;
            temperature = getFloatTemperature(world, biome, pos);
        }

        if (temperature >= 0.15F) {
            return false;
        } else if (biome.getDefaultTemperature() >= 0.15F && !ConfigLoader.temporalConfig.generateSnow) {
            return false;
        } else if (checkLight) {
            if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                IBlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock().isAir(blockState, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean canBlockFreezeInSeason(World world, BlockPos pos, boolean noWaterAdj, @Nullable ITemporalState state) {
        return canBlockFreezeInSeason(world, pos, noWaterAdj, state, false);
    }

    public static boolean canBlockFreezeInSeason(World world, BlockPos pos, boolean noWaterAdj, @Nullable ITemporalState state, boolean useUnmodifiedTemperature) {
        Biome biome = world.getBiome(pos);

        if (!BiomeClimateRegistry.isAffectedBySeasons(biome)) {
            return canBlockFreezeVanilla(world, pos, noWaterAdj, biome);
        }

        boolean dimensionWhitelisted = TemporalConfig.isDimensionWhitelisted(world.provider.getDimension());
        float temperature = biome.getTemperature(pos);

        if (!useUnmodifiedTemperature && dimensionWhitelisted) {
            if (BiomeClimateRegistry.usesTropicalCycle(biome)) return false;
            temperature = getFloatTemperature(world, biome, pos);
        }

        if (temperature >= 0.15F) {
            return false;
        } else if (biome.getDefaultTemperature() >= 0.15F && !ConfigLoader.temporalConfig.generateIce) {
            return false;
        } else {
            if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                IBlockState iblockstate = world.getBlockState(pos);
                Block block = iblockstate.getBlock();

                if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && iblockstate.getValue(BlockLiquid.LEVEL) == 0) {
                    if (!noWaterAdj) return true;
                    boolean flag = world.isWater(pos.west()) && world.isWater(pos.east()) && world.isWater(pos.north()) && world.isWater(pos.south());
                    if (!flag) return true;
                }
            }
            return false;
        }
    }

    public static boolean isRainingAtInSeason(World world, BlockPos pos, ITemporalState state) {
        Biome biome = world.getBiome(pos);

        if (BiomeClimateRegistry.usesTropicalCycle(biome) && BiomeClimateRegistry.isAffectedBySeasons(biome) && TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) {
            AnnualSeason.TropicalCycle tropicalCycle = state.getTropicalCycle();
            switch (tropicalCycle) {
                case MID_DRY: return false;
                case MID_WET: return true;
                default: return biome.canRain();
            }
        }

        if (biome.getEnableSnow() || world.canSnowAt(pos, false)) return false;
        return biome.canRain();
    }

    public static float getFloatTemperature(World world, Biome biome, BlockPos pos) {
        if (!TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) {
            return biome.getTemperature(pos);
        }
        return getFloatTemperature(new TemporalCalendar(SeasonalityAPI.getTemporalState(world).getCycleTicks()).getSubPhase(), biome, pos);
    }

    public static float getFloatTemperature(AnnualSeason.SubPhase subPhase, Biome biome, BlockPos pos) {
        boolean tropicalBiome = BiomeClimateRegistry.usesTropicalCycle(biome);
        float biomeTemp = biome.getTemperature(pos);

        if (!tropicalBiome && biome.getDefaultTemperature() <= 0.8F && BiomeClimateRegistry.isAffectedBySeasons(biome)) {
            switch (subPhase) {
                case LATE_SPRING:
                case EARLY_AUTUMN:
                    biomeTemp = MathHelper.clamp(biomeTemp - 0.1F, -0.5F, 2.0F); break;
                case MID_SPRING:
                case MID_AUTUMN:
                    biomeTemp = MathHelper.clamp(biomeTemp - 0.2F, -0.5F, 2.0F); break;
                case EARLY_SPRING:
                case LATE_AUTUMN:
                    biomeTemp = MathHelper.clamp(biomeTemp - 0.4F, -0.5F, 2.0F); break;
                case EARLY_WINTER:
                case MID_WINTER:
                case LATE_WINTER:
                    biomeTemp = MathHelper.clamp(biomeTemp - 0.8F, -0.5F, 2.0F); break;
            }
        }
        return biomeTemp;
    }

    public static boolean shouldRenderRainSnow(World world, Biome biome) {
        if (BiomeClimateRegistry.usesTropicalCycle(biome) && BiomeClimateRegistry.isAffectedBySeasons(biome) && TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) {
            AnnualSeason.TropicalCycle tropicalCycle = SeasonalityAPI.getTemporalState(world).getTropicalCycle();
            switch (tropicalCycle) {
                case MID_DRY: return false;
                case MID_WET: return true;
                default: return biome.canRain() || biome.getEnableSnow();
            }
        }
        return biome.canRain() || biome.getEnableSnow();
    }

    public static boolean shouldAddRainParticles(World world, Biome biome) {
        if (BiomeClimateRegistry.usesTropicalCycle(biome) && BiomeClimateRegistry.isAffectedBySeasons(biome) && TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) {
            AnnualSeason.TropicalCycle tropicalCycle = SeasonalityAPI.getTemporalState(world).getTropicalCycle();
            switch (tropicalCycle) {
                case MID_DRY: return false;
                case MID_WET: return true;
                default: return biome.canRain();
            }
        }
        return biome.canRain();
    }

    private static boolean canSnowAtVanilla(World world, BlockPos pos, boolean checkLight, Biome biome) {
        float temperature = biome.getTemperature(pos);
        if (temperature >= 0.15F) return false;

        if (checkLight) {
            if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
                IBlockState state = world.getBlockState(pos);
                return state.getBlock().isAir(state, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos);
            }
            return false;
        }
        return true;
    }

    private static boolean canBlockFreezeVanilla(World world, BlockPos pos, boolean noWaterAdj, Biome biome) {
        float temperature = biome.getTemperature(pos);
        if (temperature >= 0.15F) return false;

        if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && iblockstate.getValue(BlockLiquid.LEVEL) == 0) {
                if (!noWaterAdj) return true;
                boolean flag = world.isWater(pos.west()) && world.isWater(pos.east()) && world.isWater(pos.north()) && world.isWater(pos.south());
                return !flag;
            }
        }
        return false;
    }
}
