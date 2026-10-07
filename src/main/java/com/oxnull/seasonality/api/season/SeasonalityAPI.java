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
package com.oxnull.seasonality.api.season;

import net.minecraft.world.World;

/**
 * Main API entry point for retrieving temporal and seasonal data
 */
public final class SeasonalityAPI {

    private static ITemporalDataProvider dataProvider;

    /**
     * Registers the internal data provider. Should only be called by the core mod
     */
    public static void registerDataProvider(ITemporalDataProvider provider) {
        if (dataProvider != null) {
            throw new IllegalStateException("Seasonality data provider is already registered");
        }
        dataProvider = provider;
    }

    /**
     * Obtains data about the state of the seasonal cycle in the world
     * Works seamlessly on both client and server sides
     */
    public static ITemporalState getTemporalState(World world) {
        if (dataProvider == null) {
            throw new IllegalStateException("Attempted to access Seasonality API before initialization");
        }
        return world.isRemote ? dataProvider.getClientTemporalState() : dataProvider.getServerTemporalState(world);
    }

    /**
     * Internal interface implemented by the core mod to supply temporal data
     */
    public interface ITemporalDataProvider {
        ITemporalState getServerTemporalState(World world);
        ITemporalState getClientTemporalState();
    }
    
    private SeasonalityAPI() {}
}
