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

import com.oxnull.seasonality.api.season.ISeasonalPalette;
import com.oxnull.seasonality.config.BiomeClimateRegistry;
import com.oxnull.seasonality.handler.NetworkDispatcher;
import com.oxnull.seasonality.handler.season.*;
import com.oxnull.seasonality.season.TemporalCalendar;
import com.oxnull.seasonality.util.SeasonalColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventBusRegistrar {
    private static final TemporalManager TEMPORAL_MANAGER = new TemporalManager();

    public static void registerPreInitHandlers() {
        NetworkDispatcher.init();

        MinecraftForge.EVENT_BUS.register(TEMPORAL_MANAGER);
        MinecraftForge.TERRAIN_GEN_BUS.register(TEMPORAL_MANAGER);

        com.oxnull.seasonality.api.season.SeasonalityAPI.registerDataProvider(TEMPORAL_MANAGER);

        MinecraftForge.EVENT_BUS.register(new StochasticClimateHandler());
        MinecraftForge.EVENT_BUS.register(new SleepCycleInterceptor());
        MinecraftForge.EVENT_BUS.register(new CropGrowthInterceptor());

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            registerSeasonColourHandlers();
        }
    }

    public static void registerPostInitHandlers() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            BirchTintHandler.init();
        }
    }

    // --- Client Side Color Optimization ---

    @SideOnly(Side.CLIENT)
    private static BiomeColorHelper.ColorResolver originalGrassResolver;
    @SideOnly(Side.CLIENT)
    private static BiomeColorHelper.ColorResolver originalFoliageResolver;

    @SideOnly(Side.CLIENT)
    private static int cachedTick = -1;
    @SideOnly(Side.CLIENT)
    private static int cachedDim = Integer.MIN_VALUE;
    @SideOnly(Side.CLIENT)
    private static ISeasonalPalette cachedPalette;

    @SideOnly(Side.CLIENT)
    private static ISeasonalPalette getOptimizedPalette(int dimension) {
        int currentTick = TemporalManager.clientCycleTicks.getOrDefault(dimension, 0);
        if (currentTick != cachedTick || dimension != cachedDim) {
            cachedTick = currentTick;
            cachedDim = dimension;
            TemporalCalendar calendar = new TemporalCalendar(currentTick);
            cachedPalette = calendar.getSubPhase();
        }
        return cachedPalette;
    }

    @SideOnly(Side.CLIENT)
    private static void registerSeasonColourHandlers() {
        originalGrassResolver = BiomeColorHelper.GRASS_COLOR;
        originalFoliageResolver = BiomeColorHelper.FOLIAGE_COLOR;

        BiomeColorHelper.GRASS_COLOR = (biome, pos) -> {
            int dim = Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.dimension : 0;
            ISeasonalPalette palette = BiomeClimateRegistry.usesTropicalCycle(biome)
                    ? new TemporalCalendar(TemporalManager.clientCycleTicks.getOrDefault(dim, 0)).getTropicalCycle()
                    : getOptimizedPalette(dim);

            return SeasonalColorUtil.applySeasonalGrassColouring(palette, biome, originalGrassResolver.getColorAtPos(biome, pos));
        };

        BiomeColorHelper.FOLIAGE_COLOR = (biome, pos) -> {
            int dim = Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.dimension : 0;
            ISeasonalPalette palette = BiomeClimateRegistry.usesTropicalCycle(biome)
                    ? new TemporalCalendar(TemporalManager.clientCycleTicks.getOrDefault(dim, 0)).getTropicalCycle()
                    : getOptimizedPalette(dim);

            return SeasonalColorUtil.applySeasonalFoliageColouring(palette, biome, originalFoliageResolver.getColorAtPos(biome, pos));
        };
    }
}