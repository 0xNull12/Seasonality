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

import com.oxnull.seasonality.handler.season.TemporalManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class CycleSyncPacket implements IMessage, IMessageHandler<CycleSyncPacket, IMessage> {
    public int dimension;
    public int seasonCycleTicks;

    public CycleSyncPacket() {}

    public CycleSyncPacket(int dimension, int seasonCycleTicks) {
        this.dimension = dimension;
        this.seasonCycleTicks = seasonCycleTicks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dimension = buf.readInt();
        this.seasonCycleTicks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dimension);
        buf.writeInt(this.seasonCycleTicks);
    }

    @Override
    public IMessage onMessage(CycleSyncPacket message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            if (Minecraft.getMinecraft().player == null) return null;
            TemporalManager.clientCycleTicks.put(message.dimension, message.seasonCycleTicks);
        }
        return null;
    }
}
