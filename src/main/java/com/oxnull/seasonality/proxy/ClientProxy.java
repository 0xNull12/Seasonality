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

import com.google.common.base.Preconditions;
import com.oxnull.seasonality.api.ISeasonalityBlock;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.util.inventory.SeasonalityCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;


/**
 * Client-side proxy
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void registerItemVariantModel(Item item, String name, int metadata) {
        Preconditions.checkNotNull(item, "Cannot register models for null item " + name);
        Preconditions.checkArgument(item != Items.AIR, "Cannot register models for air (" + name + ")");

        ResourceLocation loc = new ResourceLocation(Seasonality.MOD_ID, name);
        ModelLoader.registerItemVariants(item, loc);
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(loc, "inventory"));
    }

    @Override
    public void registerBlockSided(Block block) {
        if (block instanceof ISeasonalityBlock) {
            ISeasonalityBlock seasonBlock = (ISeasonalityBlock) block;
            IProperty<?>[] nonRenderingProperties = seasonBlock.getNonRenderingProperties();

            if (nonRenderingProperties != null && nonRenderingProperties.length > 0) {
                IStateMapper customMapper = new StateMap.Builder().ignore(nonRenderingProperties).build();
                ModelLoader.setCustomStateMapper(block, customMapper);
            }
        }
    }

    @Override
    public void registerItemSided(Item item) {
        if (item.getHasSubtypes()) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            item.getSubItems(SeasonalityCreativeTab.INSTANCE, subItems);
            
            for (ItemStack subItem : subItems) {
                String subItemName = item.getTranslationKey(subItem);
                subItemName = subItemName.substring(subItemName.indexOf(".") + 1);

                ResourceLocation loc = new ResourceLocation(Seasonality.MOD_ID, subItemName);
                ModelLoader.registerItemVariants(item, loc);
                ModelLoader.setCustomModelResourceLocation(item, subItem.getMetadata(), new ModelResourceLocation(loc, "inventory"));
            }
        } else {
            ResourceLocation registryName = item.getRegistryName();
            if (registryName != null) {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
            } else {
                Seasonality.LOGGER.warn("Attempted to register model for an unregistered item: {}", item.getClass().getName());
            }
        }
    }
}