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
package com.oxnull.seasonality.network.message;

import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.core.Seasonality;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ConfigSyncPacket implements IMessage, IMessageHandler<ConfigSyncPacket, IMessage> {
    public NBTTagCompound nbtOptions;

    public ConfigSyncPacket() {}

    public ConfigSyncPacket(NBTTagCompound nbtOptions) {
        this.nbtOptions = nbtOptions;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.nbtOptions = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, nbtOptions);
    }

    @Override
    public IMessage onMessage(ConfigSyncPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            for (String key : message.nbtOptions.getKeySet()) {
                SyncedConfig.SyncedConfigEntry entry = SyncedConfig.getOptions().get(key);
                if (entry == null) {
                    Seasonality.LOGGER.error("Synced option {} does not exist locally", key);
                    continue;
                }
                entry.value = message.nbtOptions.getString(key);
            }
            Seasonality.LOGGER.info("Seasonality configuration synchronized with server");
        }
        return null;
    }
}
