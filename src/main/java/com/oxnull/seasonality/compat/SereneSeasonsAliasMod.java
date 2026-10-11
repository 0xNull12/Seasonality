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
package com.oxnull.seasonality.compat;

import net.minecraftforge.fml.common.Mod;

/**
 * Empty mod container that registers modid "sereneseasons" so that mods
 * that check Loader.isModLoaded("sereneseasons") trigger their integration
 * I'm too lazy to create a mixin for each mod like I did with SimpleDifficulty, okay?
 */
@Mod(
    modid = "sereneseasons",
    name = "Serene Seasons (Seasonality Compatibility)",
    version = "1.12.2-4.3.0.1",
    dependencies = "required-after:seasonality",
    acceptedMinecraftVersions = "[1.12.2]"
)
public class SereneSeasonsAliasMod {
}