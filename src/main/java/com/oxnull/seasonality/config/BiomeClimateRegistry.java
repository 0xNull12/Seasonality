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

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.oxnull.seasonality.config.json.BiomeProfile;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.util.config.JsonUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Settings that manage the seasonal properties of biomes, loaded from a JSON file
 */
public class BiomeClimateRegistry {

    private static Map<ResourceLocation, BiomeProfile> registry = ImmutableMap.of();

    // Default Biome Lists
    private static final String[] DEFAULT_BLACKLISTED = {
        "minecraft:mushroom_island", "minecraft:mushroom_island_shore", "minecraft:ocean",
        "minecraft:deep_ocean", "minecraft:river", "biomesoplenty:mystic_grove", 
        "biomesoplenty:ominous_woods", "biomesoplenty:wasteland", "biomesoplenty:flower_island",
        "biomesoplenty:coral_reef", "biomesoplenty:kelp_forest", "thaumcraft:magical_forest", 
        "integrateddynamics:biome_meneglin", "abyssalcraft:darklands", "abyssalcraft:darklands_forest",
        "abyssalcraft:darklands_plains", "abyssalcraft:darklands_hills", "abyssalcraft:darklands_mountains",
        "abyssalcraft:coralium_infested_swamp"
    };

    private static final String[] DEFAULT_TROPICAL = {
        "minecraft:desert", "minecraft:desert_hills", "minecraft:mutated_desert", "minecraft:jungle", 
        "minecraft:jungle_hills", "minecraft:jungle_edge", "minecraft:mutated_jungle", 
        "minecraft:mutated_jungle_edge", "minecraft:mesa", "minecraft:mesa_rock", 
        "minecraft:mesa_clear_rock", "minecraft:mutated_mesa", "minecraft:mutated_mesa_rock",
        "minecraft:mutated_mesa_clear_rock", "minecraft:savanna", "minecraft:savanna_rock",
        "minecraft:mutated_savanna", "minecraft:mutated_savanna_rock", "minecraft:mushroom_island", 
        "minecraft:mushroom_island_shore", "biomesoplenty:bamboo_forest", "biomesoplenty:bayou", 
        "biomesoplenty:brushland", "biomesoplenty:eucalyptus_forest", "biomesoplenty:floodplains", 
        "biomesoplenty:lush_desert", "biomesoplenty:mangrove", "biomesoplenty:outback",
        "biomesoplenty:overgrown_cliffs", "biomesoplenty:rainforest", "biomesoplenty:sacred_springs", 
        "biomesoplenty:scrubland", "biomesoplenty:tropical_rainforest", "biomesoplenty:wasteland", 
        "biomesoplenty:xeric_shrubland", "biomesoplenty:flower_island", "biomesoplenty:tropical_island", 
        "biomesoplenty:volcanic_island", "biomesoplenty:oasis", "biomesoplenty:white_beach",
        "traverse:arid_highland", "traverse:badlands", "traverse:canyon", "traverse:desert_shrubland", 
        "traverse:mini_jungle", "traverse:mountainous_desert", "traverse:red_desert",
        "conquest:bamboo_forest", "conquest:desert_mod", "conquest:jungle_mod", "conquest:mesa_extreme_mod", 
        "conquest:red_desert", "climaticbiomesjbg:subtropical_forest", "climaticbiomesjbg:subtropical_forest_hills", 
        "climaticbiomesjbg:tropical_forest", "climaticbiomesjbg:tropical_forest_hills", "climaticbiomesjbg:pine_swamp", 
        "climaticbiomesjbg:dense_scrub", "climaticbiomesjbg:dense_scrub_hills", "climaticbiomesjbg:dry_scrub",
        "climaticbiomesjbg:dry_scrub_hills", "climaticbiomesjbg:hot_mountain", "climaticbiomesjbg:hot_mountain_trees"
    };

    private static final String[] DEFAULT_CROP_DISABLED = {
        "biomesoplenty:crag", "biomesoplenty:wasteland", "biomesoplenty:volcanic_island"
    };

    public static void init(File configDir) {
        Map<String, BiomeProfile> defaultData = new HashMap<>();
        
        for (String id : DEFAULT_BLACKLISTED) defaultData.put(id, new BiomeProfile(false, false, false));
        for (String id : DEFAULT_TROPICAL) defaultData.put(id, new BiomeProfile(true, true, false));
        for (String id : DEFAULT_CROP_DISABLED) defaultData.put(id, new BiomeProfile(false, false, true));

        Map<String, BiomeProfile> loadedData = JsonUtil.getOrCreateConfigFile(
            configDir, "biome_profiles.json", defaultData, 
            new TypeToken<Map<String, BiomeProfile>>(){}.getType()
        );

        Map<ResourceLocation, BiomeProfile> tempRegistry = new HashMap<>();
        if (loadedData != null) {
            for (Map.Entry<String, BiomeProfile> entry : loadedData.entrySet()) {
                tempRegistry.put(new ResourceLocation(entry.getKey()), entry.getValue());
            }
        }
        
        // Atomic swap for thread safety during reloads
        registry = ImmutableMap.copyOf(tempRegistry);
        Seasonality.LOGGER.info("Loaded {} biome profiles.", registry.size());
    }

    public static boolean isAffectedBySeasons(Biome biome) {
        ResourceLocation name = biome.getRegistryName();
        if (name == null) return true;
        BiomeProfile profile = registry.get(name);
        return profile == null || profile.affectedBySeasons;
    }

    public static boolean usesTropicalCycle(Biome biome) {
        ResourceLocation name = biome.getRegistryName();
        if (name == null) return false;
        BiomeProfile profile = registry.get(name);
        return profile != null && profile.usesTropicalCycle;
    }

    public static boolean isCropGrowthDisabled(Biome biome) {
        ResourceLocation name = biome.getRegistryName();
        if (name == null) return false;
        BiomeProfile profile = registry.get(name);
        return profile != null && profile.disablesCropGrowth;
    }
}
