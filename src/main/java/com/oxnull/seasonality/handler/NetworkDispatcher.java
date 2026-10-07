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

import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.network.message.ConfigSyncPacket;
import com.oxnull.seasonality.network.message.CycleSyncPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public class NetworkDispatcher {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Seasonality.MOD_ID);

    public static void init() {
        CHANNEL.registerMessage(CycleSyncPacket.class, CycleSyncPacket.class, 0, Side.CLIENT);
		CHANNEL.registerMessage(ConfigSyncPacket.class, ConfigSyncPacket.class, 1, Side.CLIENT);
    }

    public static void dispatchSyncedConfigs(EntityPlayerMP player) {
        NBTTagCompound options = new NBTTagCompound();
        for (Map.Entry<String, SyncedConfig.SyncedConfigEntry> entry : SyncedConfig.getOptions().entrySet()) {
            options.setString(entry.getKey(), entry.getValue().value);
        }
        CHANNEL.sendTo(new ConfigSyncPacket(options), player);
    }
}