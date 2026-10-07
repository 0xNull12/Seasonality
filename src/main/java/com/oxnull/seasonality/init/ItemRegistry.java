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
package com.oxnull.seasonality.init;

import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.item.ItemTemporalChronometer;
import com.oxnull.seasonality.util.inventory.SeasonalityCreativeTab;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemRegistry {
    public static void registerItems() {
        registerItem(new ItemTemporalChronometer(), "season_clock");
        
        // Mod Icon (TODO: Crear el icono del mod -- Create the mod icon)
        Item icon = new Item().setRegistryName(Seasonality.MOD_ID, "seasonality_icon").setTranslationKey("seasonality_icon");

        ForgeRegistries.ITEMS.register(icon);
    }

    private static void registerItem(Item item, String name) {
        item.setRegistryName(Seasonality.MOD_ID, name);
        item.setTranslationKey(name);
        item.setCreativeTab(SeasonalityCreativeTab.INSTANCE);
        ForgeRegistries.ITEMS.register(item);
        Seasonality.proxy.registerItemSided(item);
    }
}