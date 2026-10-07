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
package com.oxnull.seasonality.handler.season;

import com.oxnull.seasonality.api.SeasonalityBlocks;
import com.oxnull.seasonality.config.AgriculturalConfig;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.init.CropFertilityCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockReed;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Intercept crop growth and bone meal events for seasonal fertility rules
 */
public class CropGrowthInterceptor {
    private static final ResourceLocation GREENHOUSE_GLASS_ID =
            new ResourceLocation(Seasonality.MOD_ID, "greenhouse_glass");

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemTooltipAdded(ItemTooltipEvent event) {
        CropFertilityCache.setupTooltips(event);
    }

    @SubscribeEvent
    public void onCropGrowth(BlockEvent.CropGrowEvent.Pre event) {
        Block plant = event.getState().getBlock();

        if (AgriculturalConfig.general.seasonalCrops
                && !CropFertilityCache.isCropFertile(plant.getRegistryName().toString(), event.getWorld(), event.getPos())
                && !isUnderGreenhouseGlass(event.getWorld(), event.getPos())) {

            if (AgriculturalConfig.general.cropsBreakOutOfSeason
                    && !(plant instanceof BlockGrass) && !(plant instanceof BlockReed)) {
                event.getWorld().destroyBlock(event.getPos(), true);
            } else {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void onApplyBonemeal(BonemealEvent event) {
        Block plant = event.getBlock().getBlock();

        if (AgriculturalConfig.general.seasonalCrops
                && !CropFertilityCache.isCropFertile(plant.getRegistryName().toString(), event.getWorld(), event.getPos())
                && !isUnderGreenhouseGlass(event.getWorld(), event.getPos())) {

            if (AgriculturalConfig.general.cropsBreakOutOfSeason
                    && !(plant instanceof BlockGrass) && !(plant instanceof BlockReed)) {
                event.getWorld().destroyBlock(event.getPos(), true);
            }
            event.setCanceled(true);
        }
    }

    private boolean isUnderGreenhouseGlass(World world, BlockPos cropPos) {
        int maxHeight = AgriculturalConfig.general.greenhouseGlassMaxHeight;
        int maxY = cropPos.getY() + maxHeight;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(cropPos);

        for (int y = cropPos.getY() + 1; y <= maxY; y++) {
            mutablePos.setY(y);
            if (isGreenhouseGlass(world.getBlockState(mutablePos).getBlock())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isGreenhouseGlass(Block block) {
        if (block == null) return false;
        if (SeasonalityBlocks.GREENHOUSE_GLASS != null && block == SeasonalityBlocks.GREENHOUSE_GLASS) return true;
        ResourceLocation id = block.getRegistryName();
        return id != null && GREENHOUSE_GLASS_ID.equals(id);
    }
}