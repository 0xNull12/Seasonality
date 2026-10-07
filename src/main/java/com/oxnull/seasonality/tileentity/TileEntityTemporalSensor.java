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
package com.oxnull.seasonality.tileentity;

import com.oxnull.seasonality.block.BlockTemporalSensor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

/**
 * Periodically refresh the sensor block's Redstone power
 */
public class TileEntityTemporalSensor extends TileEntity implements ITickable {
    private int tickCounter = 0;

    @Override
    public void update() {
        if (this.world != null && !this.world.isRemote) {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                if (this.getBlockType() instanceof BlockTemporalSensor) {
                    ((BlockTemporalSensor) this.getBlockType()).updatePower(this.world, this.pos);
                }
            }
        }
    }
}
