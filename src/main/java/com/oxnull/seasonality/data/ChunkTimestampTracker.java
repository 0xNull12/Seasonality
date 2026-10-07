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
package com.oxnull.seasonality.data;

import com.oxnull.seasonality.core.Seasonality;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class ChunkTimestampTracker extends WorldSavedData {
    private final Map<Long, Integer> timeStampMap = new HashMap<>();
    private static final String DATA_NAME = Seasonality.MOD_ID + "_ChunkTimestamps";

    public ChunkTimestampTracker() {
        super(DATA_NAME);
    }

    public ChunkTimestampTracker(String dataName) {
        super(dataName);
    }

    public static void setChunkTimeStamp(Chunk chunk, int timeStamp) {
        ChunkTimestampTracker data = get(chunk.getWorld());
        long key = ChunkPos.asLong(chunk.x, chunk.z);
        data.timeStampMap.put(key, timeStamp);
        data.markDirty();
    }

    public static int getChunkTimeStamp(Chunk chunk) {
        ChunkTimestampTracker data = get(chunk.getWorld());
        long key = ChunkPos.asLong(chunk.x, chunk.z);
        return data.timeStampMap.getOrDefault(key, 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        timeStampMap.clear();
        boolean migratedLegacyKey = false;
        for (String key : nbt.getKeySet()) {
            if (nbt.hasKey(key, 99)) {
                Long chunkKey = parseChunkKey(key);
                if (chunkKey != null) {
                    if (!isLongKey(key)) migratedLegacyKey = true;
                    timeStampMap.put(chunkKey, nbt.getInteger(key));
                }
            }
        }
        if (migratedLegacyKey) markDirty();
    }

    private static Long parseChunkKey(String key) {
        try {
            return Long.parseLong(key);
        } catch (NumberFormatException ignored) {}

        if (key.length() < 5 || key.charAt(0) != '[' || key.charAt(key.length() - 1) != ']') return null;

        String[] coordinates = key.substring(1, key.length() - 1).split(",");
        if (coordinates.length != 2) return null;

        try {
            int x = Integer.parseInt(coordinates[0].trim());
            int z = Integer.parseInt(coordinates[1].trim());
            return ChunkPos.asLong(x, z);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean isLongKey(String key) {
        try {
            Long.parseLong(key);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        for (Map.Entry<Long, Integer> entry : timeStampMap.entrySet()) {
            nbt.setInteger(entry.getKey().toString(), entry.getValue());
        }
        return nbt;
    }

    public static ChunkTimestampTracker get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        ChunkTimestampTracker instance = (ChunkTimestampTracker) storage.getOrLoadData(ChunkTimestampTracker.class, DATA_NAME);

        if (instance == null) {
            instance = new ChunkTimestampTracker();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }

    public static void clearChunkTimeStamp(Chunk chunk) {
        ChunkTimestampTracker data = get(chunk.getWorld());
        long key = ChunkPos.asLong(chunk.x, chunk.z);
        data.timeStampMap.remove(key);
        data.markDirty();
    }
}
