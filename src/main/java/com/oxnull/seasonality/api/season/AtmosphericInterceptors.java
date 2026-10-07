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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Interceptors for weather events (snow, ice, rain)
 */
public final class AtmosphericInterceptors {

    private static final String ASM_HELPER_CLASS = "com.oxnull.seasonality.season.SeasonASMHelper";
    
    private static final MethodHandle CAN_SNOW_AT;
    private static final MethodHandle CAN_FREEZE;
    private static final MethodHandle IS_RAINING_AT;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Class<?> clazz = Class.forName(ASM_HELPER_CLASS);
            
            CAN_SNOW_AT = lookup.findStatic(clazz, "canSnowAtInSeason", 
                MethodType.methodType(boolean.class, World.class, BlockPos.class, boolean.class, ITemporalState.class));
                
            CAN_FREEZE = lookup.findStatic(clazz, "canBlockFreezeInSeason", 
                MethodType.methodType(boolean.class, World.class, BlockPos.class, boolean.class, ITemporalState.class));
                
            IS_RAINING_AT = lookup.findStatic(clazz, "isRainingAtInSeason", 
                MethodType.methodType(boolean.class, World.class, BlockPos.class, ITemporalState.class));
                
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Intercepts {@link World#canSnowAt(BlockPos, boolean)}
     */
    public static boolean canSnowAtInSeason(World world, BlockPos pos, boolean checkLight, ITemporalState state) {
        try {
            return (boolean) CAN_SNOW_AT.invokeExact(world, pos, checkLight, state);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to intercept snow", t);
        }
    }

    /**
     * Intercepts {@link World#canBlockFreeze(BlockPos, boolean)}
     */
    public static boolean canBlockFreezeInSeason(World world, BlockPos pos, boolean noWaterAdj, ITemporalState state) {
        try {
            return (boolean) CAN_FREEZE.invokeExact(world, pos, noWaterAdj, state);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to intercept ice", t);
        }
    }

    /**
     * Intercepts {@link World#isRainingAt(BlockPos)}
     */
    public static boolean isRainingAtInSeason(World world, BlockPos pos, ITemporalState state) {
        try {
            return (boolean) IS_RAINING_AT.invokeExact(world, pos, state);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to intercept rainfall", t);
        }
    }
    
    private AtmosphericInterceptors() {}
}
