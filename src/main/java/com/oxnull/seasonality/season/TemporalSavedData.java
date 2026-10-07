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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldSavedData;

public class TemporalSavedData extends WorldSavedData {
    public static final String DATA_IDENTIFIER = "seasonality_temporal_data";
    public int seasonCycleTicks;
    
    public TemporalSavedData() {
        this(DATA_IDENTIFIER);
    }
    
    public TemporalSavedData(String identifier) {
        super(identifier);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.seasonCycleTicks = nbt.getInteger("CycleTicks");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("CycleTicks", this.seasonCycleTicks);
        return nbt;
    }
}
