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

import com.oxnull.seasonality.api.config.SeasonalityOption;
import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.ITemporalState;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.init.ConfigLoader;
import net.minecraft.world.World;

/**
 * Calculates the seasonal sun/moon angle
 */
public final class SeasonalOrbitalCalculator {
    private static final int TICKS_PER_DAY = 24000;
    private static final float SUNRISE_ANGLE = 0.757F;
    private static final float MIDDAY_ANGLE = 1.0F;
    private static final float SUNSET_ANGLE = 0.2425F;
    private static final float MIDNIGHT_ANGLE = 0.5F;

    private SeasonalOrbitalCalculator() {}

    public static float calculate(World world, long worldTime, float partialTicks) {
        AnnualSeason season = getSeason(world);
        return season == null ? calculateVanilla(worldTime, partialTicks) : calculate(season, worldTime, partialTicks);
    }

    public static float calculate(AnnualSeason season, long worldTime, float partialTicks) {
        if (season == null) return calculateVanilla(worldTime, partialTicks);

        int sunrise;
        int sunset;
        switch (season) {
            case SPRING: sunrise = 6000; sunset = 20500; break;
            case SUMMER: sunrise = 5000; sunset = 21500; break;
            case AUTUMN: sunrise = 7000; sunset = 19000; break;
            case WINTER: sunrise = 8000; sunset = 16500; break;
            default: return calculateVanilla(worldTime, partialTicks);
        }
        return calculateSeasonal(worldTime, partialTicks, sunrise, sunset);
    }

    public static boolean isEnabled(World world) {
        return world != null 
            && ConfigLoader.temporalConfig != null 
            && SyncedConfig.getOptions().containsKey(SeasonalityOption.CHANGE_DAYLIGHT_WITH_SEASONS.getOptionName())
            && SyncedConfig.getBooleanValue(SeasonalityOption.CHANGE_DAYLIGHT_WITH_SEASONS)
            && TemporalConfig.isDimensionWhitelisted(world.provider.getDimension());
    }

    public static float applySunBrightness(World world, float brightness) {
        return getSeason(world) == AnnualSeason.SUMMER ? brightness * 1.25F : brightness;
    }

    public static float applyStarBrightness(World world, float brightness) {
        return getSeason(world) == AnnualSeason.WINTER ? brightness * 1.25F : brightness;
    }

    private static AnnualSeason getSeason(World world) {
        if (!isEnabled(world)) return null;
        try {
            ITemporalState state = SeasonalityAPI.getTemporalState(world);
            return state == null ? null : state.getAnnualSeason();
        } catch (Exception e) {
            return null;
        }
    }

    private static float calculateSeasonal(long worldTime, float partialTicks, int sunrise, int sunset) {
        int midday = (sunrise + sunset) / 2;
        int midnight = (sunrise + sunset + TICKS_PER_DAY) / 2;

        float time = positiveModulo(worldTime + 6000L - sunrise, TICKS_PER_DAY) + sunrise + partialTicks;

        if (time < midday) return convertRange(sunrise, midday - 1, SUNRISE_ANGLE, MIDDAY_ANGLE, time);
        else if (time < sunset) return convertRange(midday, sunset - 1, 0.0F, SUNSET_ANGLE, time);
        else if (time < midnight) return convertRange(sunset, midnight - 1, SUNSET_ANGLE, MIDNIGHT_ANGLE, time);

        return convertRange(midnight, sunrise + TICKS_PER_DAY, MIDNIGHT_ANGLE, SUNRISE_ANGLE, time);
    }

    private static float calculateVanilla(long worldTime, float partialTicks) {
        int timeOfDay = (int) positiveModulo(worldTime, TICKS_PER_DAY);
        float angle = ((float) timeOfDay + partialTicks) / TICKS_PER_DAY - 0.25F;

        if (angle < 0.0F) ++angle;
        if (angle > 1.0F) --angle;

        float smoothedAngle = 1.0F - (float) ((Math.cos((double) angle * Math.PI) + 1.0D) / 2.0D);
        return angle + (smoothedAngle - angle) / 3.0F;
    }

    private static float convertRange(float oldMin, float oldMax, float newMin, float newMax, float value) {
        float range = oldMax - oldMin;
        if (range == 0.0F) return newMin;
        return ((value - oldMin) * (newMax - newMin)) / range + newMin;
    }

    private static long positiveModulo(long value, long modulus) {
        long result = value % modulus;
        return result < 0L ? result + modulus : result;
    }
}
