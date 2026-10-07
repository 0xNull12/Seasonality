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
package com.oxnull.seasonality.config;

import com.oxnull.seasonality.core.Seasonality;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Seasonality.MOD_ID, name = "seasonality_agriculture", category = "")
@Mod.EventBusSubscriber(modid = Seasonality.MOD_ID)
public class AgriculturalConfig {
    public static GeneralSettings general = new GeneralSettings();
    public static SeasonalFertility fertility = new SeasonalFertility();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Seasonality.MOD_ID)) {
            ConfigManager.sync(Seasonality.MOD_ID, Config.Type.INSTANCE);
        }
    }

    public static class GeneralSettings {
        @Config.Comment("Whether crops are affected by seasons")
        public boolean seasonalCrops = true;

        @Config.Comment("Whether crops break if out of season. If false, they simply stop growing")
        public boolean cropsBreakOutOfSeason = false;

        @Config.Comment("Whether unlisted seeds are fertile every season. False means they're fertile every season except Winter")
        public boolean ignoreUnlistedCrops = false;

        @Config.Comment("Whether to include tooltips on crops listing which seasons they're fertile in")
        public boolean enableCropTooltips = true;

        @Config.Comment("Maximum height greenhouse glass can be above a crop for it to be fertile out of season")
        @Config.RangeInt(min = 1, max = 64)
        public int greenhouseGlassMaxHeight = 7;
    }

    // TODO: Extender esta lista a más mods - Expand this list to include more mods

    public static class SeasonalFertility {
        @Config.Comment("Crops growable in Spring (List either the seed item or the crop block itself)")
        public String[] springCrops = {
            "minecraft:potato", "minecraft:carrot", "minecraft:sapling", "minecraft:nether_wart", 
            "minecraft:tallgrass", "minecraft:grass", "minecraft:red_mushroom", "minecraft:brown_mushroom",
            "harvestcraft:caulifloweritem", "harvestcraft:coffeebeanitem", "harvestcraft:garlicitem", 
            "harvestcraft:beanitem", "harvestcraft:rhubarbitem", "harvestcraft:strawberryitem", 
            "harvestcraft:oatsitem", "harvestcraft:celeryitem", "harvestcraft:peasitem", 
            "harvestcraft:broccoliitem", "harvestcraft:cabbageitem", "harvestcraft:spinachitem", 
            "harvestcraft:zucchiniitem", "harvestcraft:tealeafitem", "harvestcraft:sweetpotatoitem",
            "harvestcraft:turnipitem", "harvestcraft:leekitem", "harvestcraft:brusselsproutitem", 
            "harvestcraft:asparagusitem", "harvestcraft:barleyitem", "harvestcraft:onionitem", 
            "harvestcraft:parsnipitem", "growthcraft_rice:rice", "growthcraft_rice:riceCrop"
        };

        @Config.Comment("Crops growable in Summer")
        public String[] summerCrops = {
            "minecraft:melon_seeds", "minecraft:wheat_seeds", "minecraft:reeds", "minecraft:cocoa", 
            "minecraft:cactus", "minecraft:sapling", "minecraft:nether_wart", "minecraft:tallgrass", 
            "minecraft:grass", "minecraft:red_mushroom", "minecraft:brown_mushroom", "simplecorn:kernels",
            "harvestcraft:coffeebeanitem", "harvestcraft:beanitem", "harvestcraft:blueberryitem", 
            "harvestcraft:cornitem", "harvestcraft:chilipepperitem", "harvestcraft:radishitem", 
            "harvestcraft:tomatoitem", "harvestcraft:grapeitem", "harvestcraft:raspberryitem", 
            "harvestcraft:peasitem", "harvestcraft:cottonitem", "harvestcraft:tealeafitem", 
            "harvestcraft:sweetpotatoitem", "harvestcraft:spiceleafitem", "harvestcraft:riceitem",
            "growthcraft_apples:apple_crop", "growthcraft_apples:apple_sapling", "growthcraft_hops:hops"
        };

        @Config.Comment("Crops growable in Autumn")
        public String[] autumnCrops = {
            "minecraft:carrot", "minecraft:pumpkin_seeds", "minecraft:wheat_seeds", "minecraft:beetroot_seeds", 
            "minecraft:sapling", "minecraft:nether_wart", "minecraft:grass", "minecraft:red_mushroom", 
            "minecraft:brown_mushroom", "simplecorn:kernels", "harvestcraft:cornitem", "harvestcraft:artichokeitem", 
            "harvestcraft:beetitem", "harvestcraft:cranberryitem", "harvestcraft:eggplantitem", 
            "harvestcraft:grapeitem", "harvestcraft:whitemushroomitem", "harvestcraft:blackberryitem", 
            "harvestcraft:oatsitem", "harvestcraft:ryeitem", "harvestcraft:peasitem", "harvestcraft:spinachitem",
            "growthcraft_grapes:native_grape_vine0", "growthcraft_hops:hops"
        };

        @Config.Comment("Crops growable in Winter")
        public String[] winterCrops = {
            "minecraft:sapling", "minecraft:nether_wart", "minecraft:red_mushroom", "minecraft:brown_mushroom"
        };
    }
}
