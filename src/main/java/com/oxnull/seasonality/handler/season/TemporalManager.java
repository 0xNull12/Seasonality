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

import com.oxnull.seasonality.api.config.SeasonalityOption;
import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.api.season.ITemporalState;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.handler.NetworkDispatcher;
import com.oxnull.seasonality.network.message.CycleSyncPacket;
import com.oxnull.seasonality.season.SeasonalityASMHelper;
import com.oxnull.seasonality.season.TemporalCalendar;
import com.oxnull.seasonality.season.TemporalSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class TemporalManager implements SeasonalityAPI.ITemporalDataProvider {
    public static final Map<Integer, Integer> clientCycleTicks = new HashMap<>();
    
    private static int cachedClientTick = -1;
    private static int cachedClientDim = Integer.MIN_VALUE;
    private static TemporalCalendar cachedClientCalendar;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote) return;

        if (!SyncedConfig.getBooleanValue(SeasonalityOption.PROGRESS_SEASON_WHILE_OFFLINE)) {
            MinecraftServer server = world.getMinecraftServer();
            if (server != null && server.getPlayerList().getCurrentPlayerCount() == 0) return;
        }

        TemporalSavedData savedData = getTemporalSavedData(world);
        int cycleDuration = TemporalCalendar.ZERO.getCycleDuration();
        
        if (cycleDuration <= 0) {
            Seasonality.LOGGER.warn("Invalid cycle duration: {} skipping tick", cycleDuration);
            return;
        }

        savedData.seasonCycleTicks = (savedData.seasonCycleTicks + 1) % cycleDuration;

        if (savedData.seasonCycleTicks % 20 == 0) {
            sendSeasonUpdate(world);
            savedData.markDirty();
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (!player.world.isRemote && player instanceof EntityPlayerMP) {
            NetworkDispatcher.dispatchSyncedConfigs((EntityPlayerMP) player);
            sendSeasonUpdate(player.world);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().player == null || event.phase != TickEvent.Phase.END) return;
        
        int dimension = Minecraft.getMinecraft().player.dimension;
        if (!TemporalConfig.isDimensionWhitelisted(dimension)) return;

        int currentTicks = clientCycleTicks.merge(dimension, 1, Integer::sum);
        int cycleDuration = TemporalCalendar.ZERO.getCycleDuration();
        
        if (cycleDuration > 0 && currentTicks >= cycleDuration) {
            clientCycleTicks.put(dimension, 0);
        }
        
        TemporalCalendar calendar = getClientCalendar();
    }

    @SubscribeEvent
    public void onPopulateChunk(PopulateChunkEvent.Populate event) {
        World world = event.getWorld();
        if (world.isRemote || event.getType() != PopulateChunkEvent.Populate.EventType.ICE || !TemporalConfig.isDimensionWhitelisted(world.provider.getDimension()))
            return;

        event.setResult(Event.Result.DENY);
        ITemporalState state = SeasonalityAPI.getTemporalState(world);

        int baseX = event.getChunkX() * 16 + 8;
        int baseZ = event.getChunkZ() * 16 + 8;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                mutablePos.setPos(baseX + x, 0, baseZ + z);
                BlockPos precipPos = world.getPrecipitationHeight(mutablePos);
                BlockPos downPos = precipPos.down();

                if (SeasonalityASMHelper.canBlockFreezeInSeason(world, downPos, false, state, true)) {
                    world.setBlockState(downPos, Blocks.ICE.getDefaultState(), 2);
                }
                if (SeasonalityASMHelper.canSnowAtInSeason(world, precipPos, true, state, true)) {
                    world.setBlockState(precipPos, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
    }

    public static void sendSeasonUpdate(World world) {
        if (!world.isRemote) {
            TemporalSavedData savedData = getTemporalSavedData(world);
            NetworkDispatcher.CHANNEL.sendToAll(new CycleSyncPacket(world.provider.getDimension(), savedData.seasonCycleTicks));
        }
    }

    public static TemporalSavedData getTemporalSavedData(World world) {
        MapStorage storage = world.getPerWorldStorage();
        TemporalSavedData data = (TemporalSavedData) storage.getOrLoadData(TemporalSavedData.class, TemporalSavedData.DATA_IDENTIFIER);

        if (data == null) {
            data = new TemporalSavedData(TemporalSavedData.DATA_IDENTIFIER);
            int startingPhase = SyncedConfig.getIntValue(SeasonalityOption.STARTING_SUB_SEASON);
            
            if (startingPhase == 0) {
                data.seasonCycleTicks = (world.rand.nextInt(12)) * TemporalCalendar.ZERO.getSubPhaseDuration();
            } else if (startingPhase > 0) {
                data.seasonCycleTicks = (startingPhase - 1) * TemporalCalendar.ZERO.getSubPhaseDuration();
            }
            storage.setData(TemporalSavedData.DATA_IDENTIFIER, data);
            data.markDirty();
        }
        return data;
    }

    public static TemporalCalendar getClientCalendar() {
        int dim = Minecraft.getMinecraft().player != null ? Minecraft.getMinecraft().player.dimension : 0;
        int tick = clientCycleTicks.getOrDefault(dim, 0);
        if (tick != cachedClientTick || dim != cachedClientDim) {
            cachedClientTick = tick;
            cachedClientDim = dim;
            cachedClientCalendar = new TemporalCalendar(tick);
        }
        return cachedClientCalendar;
    }

    @Override
    public ITemporalState getServerTemporalState(World world) {
        return new TemporalCalendar(getTemporalSavedData(world).seasonCycleTicks);
    }

    @Override
    public ITemporalState getClientTemporalState() {
        return getClientCalendar();
    }
}
