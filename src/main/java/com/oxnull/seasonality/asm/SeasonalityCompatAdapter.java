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
package com.oxnull.seasonality.asm;

import com.oxnull.seasonality.api.config.SeasonalityOption;
import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.ITemporalState;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Join Serene Seasons API calls to Seasonality API
 */
public class SeasonalityCompatAdapter {
	private static Object cachedProxy;
    private static World cachedWorld;
    private static int cachedTicks = -1;
    
    // Ordinal mapping cache -> generated enum
    private static final Map<String, Object> enumCache = new HashMap<>();

    /**
     * Gets a constant from a dynamically generated enum
     */
    private static Object enumInstance(String className, String constName) {
        String key = className + "." + constName;
        Object cached = enumCache.get(key);
        if (cached != null) return cached;
        try {
            Class<?> clazz = Class.forName(className);
            Object value = clazz.getField(constName).get(null);
            enumCache.put(key, value);
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Called by SeasonHelper.getSeasonState()
     * Returns a proxy that implements the ISeasonState interface
     */
    public static Object getSeasonState(World world) {
        ITemporalState state = SeasonalityAPI.getTemporalState(world);
        int ticks = state.getCycleTicks();
        if (cachedProxy != null && cachedWorld == world && cachedTicks == ticks) {
            return cachedProxy;
        }
        try {
            Class<?> iface = Class.forName("sereneseasons.api.season.ISeasonState");
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{ iface },
                new SeasonStateHandler(state)
            );
            cachedProxy = proxy;
            cachedWorld = world;
            cachedTicks = ticks;
            return proxy;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    private static class SeasonStateHandler implements java.lang.reflect.InvocationHandler {
        private final ITemporalState state;
        SeasonStateHandler(ITemporalState state) { this.state = state; }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            switch (method.getName()) {
                case "getDayDuration":        return state.getDayDuration();
                case "getSubSeasonDuration":  return state.getSubPhaseDuration();
                case "getSeasonDuration":     return state.getSeasonDuration();
                case "getCycleDuration":      return state.getCycleDuration();
                case "getSeasonCycleTicks":   return state.getCycleTicks();
                case "getDay":                return state.getDay();
                case "getSubSeason":          return enumInstance("sereneseasons.api.season.Season$SubSeason", state.getSubPhase().name());
                case "getSeason":             return enumInstance("sereneseasons.api.season.Season", state.getAnnualSeason().name());
                case "getTropicalSeason":     return enumInstance("sereneseasons.api.season.Season$TropicalSeason", state.getTropicalCycle().name());
                case "toString":              return "SeasonalityCompatState";
                case "hashCode":              return System.identityHashCode(proxy);
                case "equals":                return proxy == args[0];
                default:                      return null;
            }
        }
    }

    /**
     * Called by SyncedConfig methods
     */
    public static boolean getBooleanValue(Object option) {
        String optionName = getEnumName(option);
        try {
            SeasonalityOption real = SeasonalityOption.valueOf(optionName);
            return SyncedConfig.getBooleanValue(real);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static int getIntValue(Object option) {
        String optionName = getEnumName(option);
        try {
            SeasonalityOption real = SeasonalityOption.valueOf(optionName);
            return SyncedConfig.getIntValue(real);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Called by SubSeason/TropicalSeason color methods
     */
    public static int colorInt(Object e, String m) {
        String n = ((Enum<?>) e).name();
        AnnualSeason.SubPhase p = phaseOrNull(n);
        if (p != null) {
            switch (m) {
                case "getGrassOverlay":   return p.getGrassOverlay();
                case "getFoliageOverlay": return p.getFoliageOverlay();
                case "getBirchColor":     return p.getBirchColor();
            }
        } else {
            AnnualSeason.TropicalCycle c = cycleOrNull(n);
            if (c != null) {
                switch (m) {
                    case "getGrassOverlay":   return c.getGrassOverlay();
                    case "getFoliageOverlay": return c.getFoliageOverlay();
                    case "getBirchColor":     return c.getBirchColor();
                }
            }
        }
        return 0xFFFFFF;
    }

    public static float colorFloat(Object e, String m) {
        String n = ((Enum<?>) e).name();
        AnnualSeason.SubPhase p = phaseOrNull(n);
        if (p != null) {
            switch (m) {
                case "getGrassSaturationMultiplier":   return p.getGrassSaturationMultiplier();
                case "getFoliageSaturationMultiplier": return p.getFoliageSaturationMultiplier();
            }
        } else {
            AnnualSeason.TropicalCycle c = cycleOrNull(n);
            if (c != null) {
                switch (m) {
                    case "getGrassSaturationMultiplier":   return c.getGrassSaturationMultiplier();
                    case "getFoliageSaturationMultiplier": return c.getFoliageSaturationMultiplier();
                }
            }
        }
        return 1.0f;
    }

    private static AnnualSeason.SubPhase phaseOrNull(String n) {
        try { return AnnualSeason.SubPhase.valueOf(n); } catch (IllegalArgumentException e) { return null; }
    }
    private static AnnualSeason.TropicalCycle cycleOrNull(String n) {
        try { return AnnualSeason.TropicalCycle.valueOf(n); } catch (IllegalArgumentException e) { return null; }
    }

    /**
     * Called by SubSeason.getSeason()
     */
    public static Object getSeasonFromSubSeason(Object subSeasonInstance) {
        String subSeasonName = getEnumName(subSeasonInstance);
        try {
            AnnualSeason.SubPhase phase = AnnualSeason.SubPhase.valueOf(subSeasonName);
            AnnualSeason parentSeason = phase.getParentSeason();
            return getGeneratedEnumInstance("sereneseasons.api.season.Season", parentSeason.name());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the name of an enum
     */
    private static String getEnumName(Object enumInstance) {
        try {
            return ((Enum<?>) enumInstance).name();
        } catch (Exception e) {
            return enumInstance.toString();
        }
    }

    /**
     * Gets an instance of enum dynamically generated by reflection
     */
    private static Object getGeneratedEnumInstance(String className, String fieldName) {
        String key = className + "." + fieldName;
        if (enumCache.containsKey(key)) {
            return enumCache.get(key);
        }
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getField(fieldName);
            Object value = field.get(null);
            enumCache.put(key, value);
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Adapter that implements ISeasonState by delegating to ITemporalState
     */
    private static class SeasonStateAdapter {
        private final ITemporalState delegate;
        SeasonStateAdapter(ITemporalState delegate) {
            this.delegate = delegate;
        }

        public int getDayDuration() { return delegate.getDayDuration(); }
        public int getSubSeasonDuration() { return delegate.getSubPhaseDuration(); }
        public int getSeasonDuration() { return delegate.getSeasonDuration(); }
        public int getCycleDuration() { return delegate.getCycleDuration(); }
        public int getSeasonCycleTicks() { return delegate.getCycleTicks(); }
        public int getDay() { return delegate.getDay(); }

        public Object getSubSeason() {
            return getGeneratedEnumInstance(
                "sereneseasons.api.season.Season$SubSeason",
                delegate.getSubPhase().name()
            );
        }

        public Object getSeason() {
            return getGeneratedEnumInstance(
                "sereneseasons.api.season.Season",
                delegate.getAnnualSeason().name()
            );
        }

        public Object getTropicalSeason() {
            return getGeneratedEnumInstance(
                "sereneseasons.api.season.Season$TropicalSeason",
                delegate.getTropicalCycle().name()
            );
        }
    }
    
    /**
     * Called by SeasonASMHelper.getFloatTemperature(World, Biome, BlockPos)
     */
    public static float getFloatTemperature(World world, Object biome, Object pos) {
        float temp = com.oxnull.seasonality.season.SeasonalityASMHelper.getFloatTemperature(
            world,
            (net.minecraft.world.biome.Biome) biome,
            (net.minecraft.util.math.BlockPos) pos
        );
        // TODO: Eliminar
        com.oxnull.seasonality.core.Seasonality.LOGGER.info(
            "[compat] getFloatTemperature(World) -> {}", temp);
        return temp;
    }

    public static float getFloatTemperatureFromSubSeason(Object subSeason, Object biome, Object pos) {
        String subSeasonName = ((Enum<?>) subSeason).name();
        // TODO: Eliminar
        com.oxnull.seasonality.core.Seasonality.LOGGER.info(
            "[compat] getFloatTemperature(SubSeason={}) ", subSeasonName);
        try {
            com.oxnull.seasonality.api.season.AnnualSeason.SubPhase phase =
                com.oxnull.seasonality.api.season.AnnualSeason.SubPhase.valueOf(subSeasonName);
            return com.oxnull.seasonality.season.SeasonalityASMHelper.getFloatTemperature(
                phase,
                (net.minecraft.world.biome.Biome) biome,
                (net.minecraft.util.math.BlockPos) pos
            );
        } catch (IllegalArgumentException e) {
            net.minecraft.world.biome.Biome b = (net.minecraft.world.biome.Biome) biome;
            return b.getDefaultTemperature();
        }
    }
}
