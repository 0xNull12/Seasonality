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
package com.oxnull.seasonality.handler;

import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.ClimateConfig;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.data.ChunkTimestampTracker;
import com.oxnull.seasonality.season.SeasonalityASMHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClimateRecalculationHandler {
    private static final Queue<Chunk> recalculationQueue = new ArrayDeque<>();
    private static final ConcurrentHashMap<String, Long> trackedPlayerChunks = new ConcurrentHashMap<>();
    
    private int recalculationCooldown = 0;
    private static final int RECALCULATION_INTERVAL = 200; // 10 seconds

    private boolean isRecalculationEnabled(World world) {
        return !world.isRemote
                && world.provider.getDimension() == 0
                && ClimateConfig.general.shouldRecalculateSnow;
    }

    private int getDelayMinutes() {
        int delay = ClimateConfig.general.timeToRecalculateSnow;
        return Math.max(0, delay);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) return;
        World world = event.world;
        if (!isRecalculationEnabled(world)) return;

        if (--recalculationCooldown <= 0) {
            recalculationCooldown = RECALCULATION_INTERVAL;
            checkPlayerMovement(world);
        }
        if (recalculationQueue.isEmpty()) return;

        int processed = 0;
        AnnualSeason.SubPhase subPhase = SeasonalityAPI.getTemporalState(world).getSubPhase();

        while (!recalculationQueue.isEmpty() && processed < 20) {
            Chunk chunk = recalculationQueue.poll();
            if (chunk == null || chunk.unloadQueued || !chunk.isLoaded()) continue;

            if (processChunk(world, chunk, subPhase)) {
                processed++;
                ChunkTimestampTracker.setChunkTimeStamp(chunk, currentTimeMinutes());
            } else {
                recalculationQueue.offer(chunk);
            }
        }
    }

    private void scheduleChunksAroundPlayer(World world, int centerX, int centerZ, int currentTime) {
        int radius = 5;
        int delay = getDelayMinutes();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(centerX + x, centerZ + z);
                if (chunk != null && chunk.isLoaded() && needsRecalculation(chunk, currentTime, delay)) {
                    if (!recalculationQueue.contains(chunk)) {
                        recalculationQueue.offer(chunk);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoaded(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (!isRecalculationEnabled(world)) return;
        Chunk chunk = event.getChunk();
        if (needsRecalculation(chunk, currentTimeMinutes(), getDelayMinutes())) {
            recalculationQueue.offer(chunk);
        }
    }

    private boolean needsRecalculation(Chunk chunk, int currentTime, int delayMinutes) {
        int lastRecalc = ChunkTimestampTracker.getChunkTimeStamp(chunk);
        if (lastRecalc == 0) return true;
        return (currentTime - lastRecalc) >= delayMinutes;
    }

    private static int currentTimeMinutes() {
        return (int) (System.currentTimeMillis() / 60000L);
    }

    private void checkPlayerMovement(World world) {
        int currentTimeMinutes = (int) (System.currentTimeMillis() / 60000L);
        Set<String> currentPlayerNames = new HashSet<>();
        for (EntityPlayer player : world.playerEntities) {
            String name = player.getName();
            currentPlayerNames.add(name);
            
            int chunkX = MathHelper.floor(player.posX / 16.0D);
            int chunkZ = MathHelper.floor(player.posZ / 16.0D);
            long currentChunkKey = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);

            Long lastChunkKey = trackedPlayerChunks.get(name);
            if (lastChunkKey == null || lastChunkKey != currentChunkKey) {
                trackedPlayerChunks.put(name, currentChunkKey);
                scheduleChunksAroundPlayer(world, chunkX, chunkZ, currentTimeMinutes);
            }
        }
        
        trackedPlayerChunks.keySet().removeIf(name -> !currentPlayerNames.contains(name));
    }

    private boolean processChunk(World world, Chunk chunk, AnnualSeason.SubPhase subPhase) {
        boolean success = true;
        int baseX = chunk.x * 16;
        int baseZ = chunk.z * 16;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                mutablePos.setPos(baseX + x, 0, baseZ + z);
                BlockPos precipPos = chunk.getPrecipitationHeight(mutablePos);
                BlockPos downPos = precipPos.down();
                if (world.canBlockFreezeWater(downPos)) {
                    success &= world.setBlockState(downPos, Blocks.ICE.getDefaultState(), 2);
                }
                if (world.canSnowAt(precipPos, true)) {
                    success &= world.setBlockState(precipPos, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }

                if (shouldMelt(world, precipPos, subPhase)) {
                    if (world.getBlockState(downPos).getBlock() == Blocks.ICE) {
                        success &= world.setBlockState(downPos, Blocks.WATER.getDefaultState(), 2);
                    }
                    if (world.getBlockState(precipPos).getBlock() == Blocks.SNOW_LAYER) {
                        success &= world.setBlockState(precipPos, Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
        }
        return success;
    }

    @SubscribeEvent
    public void onChunkUnLoaded(ChunkEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != 0) return;
        recalculationQueue.remove(event.getChunk());
    }

    @SubscribeEvent
    public void playerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        World world = event.player.world;
        if (!isRecalculationEnabled(world)) return;
        
        Seasonality.LOGGER.debug("Seasonality: Player {} joined, scheduling chunk recalculation", event.player.getName());
        
        int chunkX = MathHelper.floor(event.player.posX / 16.0D);
        int chunkZ = MathHelper.floor(event.player.posZ / 16.0D);
        scheduleChunksAroundPlayer(world, chunkX, chunkZ, (int) (System.currentTimeMillis() / 60000L));
    }

    @SubscribeEvent
    public void playerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        trackedPlayerChunks.remove(event.player.getName());
    }

    private boolean shouldMelt(World world, BlockPos pos, AnnualSeason.SubPhase subPhase) {
        Biome biome = world.getBiome(pos);
        return SeasonalityASMHelper.getFloatTemperature(subPhase, biome, pos) >= 0.15F;
    }
}
