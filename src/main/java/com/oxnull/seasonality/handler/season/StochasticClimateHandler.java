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

import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.AgriculturalConfig;
import com.oxnull.seasonality.config.BiomeClimateRegistry;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.init.ConfigLoader;
import com.oxnull.seasonality.season.SeasonalityASMHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

public class StochasticClimateHandler {
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != net.minecraftforge.fml.relauncher.Side.SERVER) return;

        WorldServer world = (WorldServer) event.world;
        if (!TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) return;

        AnnualSeason.SubPhase subPhase = SeasonalityAPI.getTemporalState(world).getSubPhase();
        AnnualSeason season = subPhase.getParentSeason();

        if (ConfigLoader.temporalConfig.changeWeatherFrequency) {
            handleWeatherChanges(world, season);
        }

        if (season == AnnualSeason.WINTER) return;

        int randChance;
        switch (subPhase) {
            case EARLY_SPRING: randChance = 16; break;
            case MID_SPRING:   randChance = 12; break;
            case LATE_SPRING:  randChance = 8;  break;
            default:           randChance = 4;  break;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        Iterator<Chunk> iterator = world.getPersistentChunkIterable(world.getPlayerChunkMap().getChunkIterator());

        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            if (world.rand.nextInt(randChance) != 0) continue;
            int x = chunk.x << 4;
            int z = chunk.z << 4;
            int randOffset = world.rand.nextInt(256);
            
            mutablePos.setPos(x + (randOffset & 15), 0, z + ((randOffset >> 4) & 15));
            BlockPos precipPos = world.getPrecipitationHeight(mutablePos);
            Biome biome = world.getBiome(precipPos);

            if (!BiomeClimateRegistry.isAffectedBySeasons(biome)) continue;

            boolean firstBlock = true;
            for (int y = precipPos.getY(); y >= 0; y--) {
                mutablePos.setY(y);
                Block block = world.getBlockState(mutablePos).getBlock();
                if (block == Blocks.SNOW_LAYER && SeasonalityASMHelper.getFloatTemperature(world, biome, mutablePos) >= 0.15F) {
                    world.setBlockToAir(mutablePos);
                    break;
                }
                if (!firstBlock && block == Blocks.ICE && SeasonalityASMHelper.getFloatTemperature(world, biome, mutablePos) >= 0.15F) {
                    ((BlockIce) Blocks.ICE).turnIntoWater(world, mutablePos);
                    break;
                }
                firstBlock = false;
            }
        }
    }

    private void handleWeatherChanges(WorldServer world, AnnualSeason season) {
        if (season == AnnualSeason.WINTER) {
            if (world.getWorldInfo().isThundering()) world.getWorldInfo().setThundering(false);
            if (!world.getWorldInfo().isRaining() && world.getWorldInfo().getRainTime() > 36000) {
                world.getWorldInfo().setRainTime(world.rand.nextInt(24000) + 12000);
            }
        } else if (season == AnnualSeason.SPRING) {
            if (!world.getWorldInfo().isRaining() && world.getWorldInfo().getRainTime() > 96000) {
                world.getWorldInfo().setRainTime(world.rand.nextInt(84000) + 12000);
            }
        } else if (season == AnnualSeason.SUMMER) {
            if (!world.getWorldInfo().isThundering() && world.getWorldInfo().getThunderTime() > 36000) {
                world.getWorldInfo().setThunderTime(world.rand.nextInt(24000) + 12000);
            }
        }
    }
}