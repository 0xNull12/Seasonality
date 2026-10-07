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
package com.oxnull.seasonality.proxy;

import com.oxnull.seasonality.handler.ClimateRecalculationHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;

/**
 * Server-side proxy
 */
public class CommonProxy {

    public void registerRenderers() {}
    
    public void registerItemVariantModel(Item item, String name, int metadata) {}
    
    public void registerBlockSided(Block block) {}
    
    public void registerItemSided(Item item) {}

    public void registerEventListeners() {
        MinecraftForge.EVENT_BUS.register(new ClimateRecalculationHandler());
    }

    public void registerPostEventListeners() {}
}
