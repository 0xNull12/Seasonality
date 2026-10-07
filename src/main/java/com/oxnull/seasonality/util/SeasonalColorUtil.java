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
package com.oxnull.seasonality.util;

import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.api.season.ISeasonalPalette;
import com.oxnull.seasonality.config.BiomeClimateRegistry;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.init.ConfigLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.Biome;

import java.awt.Color;

/**
 * It is used to mix and apply seasonal colors to foliage and grass.
 */
public final class SeasonalColorUtil {
    private static final ThreadLocal<float[]> HSB_VALUES = ThreadLocal.withInitial(() -> new float[3]);

    private SeasonalColorUtil() {}

    public static int multiplyColours(int colour1, int colour2) {
        return (colour1 * colour2) / 255;
    }

    public static int overlayBlendChannel(int underColour, int overColour) {
        if (underColour < 128) {
            return multiplyColours(2 * underColour, overColour);
        } else {
            int retVal = multiplyColours(2 * (255 - underColour), 255 - overColour);
            return 255 - retVal;
        }
    }

    public static int overlayBlend(int underColour, int overColour) {
        int r = overlayBlendChannel((underColour >> 16) & 255, (overColour >> 16) & 255);
        int g = overlayBlendChannel((underColour >> 8) & 255, (overColour >> 8) & 255);
        int b = overlayBlendChannel(underColour & 255, overColour & 255);
        return (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    public static int saturateColour(int colour, float saturationMultiplier) {
        if (saturationMultiplier == 1.0F) return colour;

        int r = (colour >> 16) & 255;
        int g = (colour >> 8) & 255;
        int b = colour & 255;

        float[] hsb = HSB_VALUES.get();
        Color.RGBtoHSB(r, g, b, hsb);

        hsb[1] *= saturationMultiplier;
        if (hsb[1] > 1.0F) hsb[1] = 1.0F;
        if (hsb[1] < 0.0F) hsb[1] = 0.0F;

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    public static int applySeasonalGrassColouring(ISeasonalPalette colorProvider, Biome biome, int originalColour) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !BiomeClimateRegistry.isAffectedBySeasons(biome) || !TemporalConfig.isDimensionWhitelisted(mc.player.dimension))
            return originalColour;

        int overlay = colorProvider.getGrassOverlay();
        float saturationMultiplier = colorProvider.getGrassSaturationMultiplier();
        
        if (!ConfigLoader.temporalConfig.changeGrassColour) {
            overlay = AnnualSeason.SubPhase.MID_SUMMER.getGrassOverlay();
            saturationMultiplier = AnnualSeason.SubPhase.MID_SUMMER.getGrassSaturationMultiplier();
        }

        int newColour = ((overlay & 0xFFFFFF) == 0xFFFFFF) ? originalColour : overlayBlend(originalColour, overlay);
        return (saturationMultiplier != 1.0F && saturationMultiplier != -1.0F) ? saturateColour(newColour, saturationMultiplier) : newColour;
    }

    public static int applySeasonalFoliageColouring(ISeasonalPalette colorProvider, Biome biome, int originalColour) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !BiomeClimateRegistry.isAffectedBySeasons(biome) || !TemporalConfig.isDimensionWhitelisted(mc.player.dimension))
            return originalColour;

        int overlay = colorProvider.getFoliageOverlay();
        float saturationMultiplier = colorProvider.getFoliageSaturationMultiplier();
        
        if (!ConfigLoader.temporalConfig.changeFoliageColour) {
            overlay = AnnualSeason.SubPhase.MID_SUMMER.getFoliageOverlay();
            saturationMultiplier = AnnualSeason.SubPhase.MID_SUMMER.getFoliageSaturationMultiplier();
        }

        int newColour = ((overlay & 0xFFFFFF) == 0xFFFFFF) ? originalColour : overlayBlend(originalColour, overlay);
        return (saturationMultiplier != 1.0F && saturationMultiplier != -1.0F) ? saturateColour(newColour, saturationMultiplier) : newColour;
    }
}
