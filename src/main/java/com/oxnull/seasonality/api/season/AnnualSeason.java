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

import java.util.Locale;

/**
 * Defines the primary annual seasons and their respective sub-phases and tropical cycles
 */
public enum AnnualSeason {
    SPRING, SUMMER, AUTUMN, WINTER;

    /**
     * Represents the micro-seasons (SubPhases) that occur within a primary season
     */
    public enum SubPhase implements ISeasonalPalette {
        EARLY_SPRING(SPRING, 0x778087, 0.85F, 0x6F818F, 0.85F, 0x869A68),
        MID_SPRING(SPRING, 0x6F818F, 0x5F849F, 0x7EA271),
        LATE_SPRING(SPRING, 0x678297, 0x3F89BF, 0x6EB283),
        
        EARLY_SUMMER(SUMMER, 0x73808B, 0x5F849F, 0x76AC6C),
        MID_SUMMER(SUMMER, 0xFFFFFF, 0xFFFFFF, 0x80A755),
        LATE_SUMMER(SUMMER, 0x877777, 0x9F5F5F, 0x98A54B),
        
        EARLY_AUTUMN(AUTUMN, 0x8F6F6F, 0xC44040, 0xB1A442),
        MID_AUTUMN(AUTUMN, 0x9F5F5F, 0xEF2121, 0xE2A231),
        LATE_AUTUMN(AUTUMN, 0xAF4F4F, 0.85F, 0xDB3030, 0.85F, 0xC98A35),
        
        EARLY_WINTER(WINTER, 0xAF4F4F, 0.60F, 0xDB3030, 0.60F, 0xB1723B),
        MID_WINTER(WINTER, 0xAF4F4F, 0.45F, 0xDB3030, 0.45F, 0xA0824D),
        LATE_WINTER(WINTER, 0x8E8181, 0.60F, 0xA57070, 0.60F, 0x8F925F);

        public static final SubPhase[] VALUES = SubPhase.values();

        private final AnnualSeason parentSeason;
        private final int grassOverlay;
        private final float grassSaturation;
        private final int foliageOverlay;
        private final float foliageSaturation;
        private final int birchColor;

        SubPhase(AnnualSeason parent, int grassColor, float grassSat, int foliageColor, float foliageSat, int birchColor) {
            this.parentSeason = parent;
            this.grassOverlay = grassColor;
            this.grassSaturation = grassSat;
            this.foliageOverlay = foliageColor;
            this.foliageSaturation = foliageSat;
            this.birchColor = birchColor;
        }

        SubPhase(AnnualSeason parent, int grassColor, int foliageColor, int birchColor) {
            this(parent, grassColor, -1F, foliageColor, -1F, birchColor);
        }

        public AnnualSeason getParentSeason() { return this.parentSeason; }

        public String getTranslationKey() {
            return "commands.seasonality.phase." + this.name().toLowerCase(Locale.ROOT);
        }

        @Override public int getGrassOverlay() { return this.grassOverlay; }
        @Override public float getGrassSaturationMultiplier() { return this.grassSaturation; }
        @Override public int getFoliageOverlay() { return this.foliageOverlay; }
        @Override public float getFoliageSaturationMultiplier() { return this.foliageSaturation; }
        @Override public int getBirchColor() { return this.birchColor; }
    }

    /**
     * Represents the seasonal cycles for tropical biomes (Wet/Dry)
     */
    public enum TropicalCycle implements ISeasonalPalette {
        EARLY_DRY(0xFFFFFF, 0xFFFFFF, 0x80A755),
        MID_DRY(0xA58668, 0.8F, 0xB7867C, 0.95F, 0x98A54B),
        LATE_DRY(0x8E7B6D, 0.9F, 0xA08B86, 0.975F, 0x80A755),
        
        EARLY_WET(0x758C8A, 0x728C91, 0x80A755),
        MID_WET(0x548384, 0x2498AE, 0x76AC6C),
        LATE_WET(0x658989, 0x4E8893, 0x80A755);

        public static final TropicalCycle[] VALUES = TropicalCycle.values();

        private final int grassOverlay;
        private final float grassSaturation;
        private final int foliageOverlay;
        private final float foliageSaturation;
        private final int birchColor;

        TropicalCycle(int grassColor, float grassSat, int foliageColor, float foliageSat, int birchColor) {
            this.grassOverlay = grassColor;
            this.grassSaturation = grassSat;
            this.foliageOverlay = foliageColor;
            this.foliageSaturation = foliageSat;
            this.birchColor = birchColor;
        }

        TropicalCycle(int grassColor, int foliageColor, int birchColor) {
            this(grassColor, -1F, foliageColor, -1F, birchColor);
        }

        @Override public int getGrassOverlay() { return this.grassOverlay; }
        @Override public float getGrassSaturationMultiplier() { return this.grassSaturation; }
        @Override public int getFoliageOverlay() { return this.foliageOverlay; }
        @Override public float getFoliageSaturationMultiplier() { return this.foliageSaturation; }
        @Override public int getBirchColor() { return this.birchColor; }
    }
}
