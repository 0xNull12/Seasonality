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
package com.oxnull.seasonality.mixin.client;

import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.season.SeasonalityASMHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererClimateMixin {

    @Shadow @Final private Minecraft mc;

    private boolean isSeasonsActive() {
        return this.mc.world != null
            && TemporalConfig.isDimensionWhitelisted(this.mc.world.provider.getDimension());
    }

    @Redirect(
        method = "renderRainSnow",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;canRain()Z")
    )
    private boolean onRenderRainSnowCanRain(Biome biome) {
        if (!isSeasonsActive()) return biome.canRain();
        return SeasonalityASMHelper.shouldRenderRainSnow(this.mc.world, biome);
    }

    @Redirect(
        method = "renderRainSnow",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getEnableSnow()Z")
    )
    private boolean onRenderRainSnowGetEnableSnow(Biome biome) {
        if (!isSeasonsActive()) return biome.getEnableSnow();
        return false;
    }

    @Redirect(
        method = "renderRainSnow",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F")
    )
    private float onRenderRainSnowGetTemperature(Biome biome, BlockPos pos) {
        if (!isSeasonsActive()) return biome.getTemperature(pos);
        return SeasonalityASMHelper.getFloatTemperature(this.mc.world, biome, pos);
    }

    // addRainParticles redirects

    @Redirect(
        method = "addRainParticles",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;canRain()Z")
    )
    private boolean onAddRainParticlesCanRain(Biome biome) {
        if (!isSeasonsActive()) return biome.canRain();
        return SeasonalityASMHelper.shouldAddRainParticles(this.mc.world, biome);
    }

    @Redirect(
        method = "addRainParticles",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getTemperature(Lnet/minecraft/util/math/BlockPos;)F")
    )
    private float onAddRainParticlesGetTemperature(Biome biome, BlockPos pos) {
        if (!isSeasonsActive()) return biome.getTemperature(pos);
        return SeasonalityASMHelper.getFloatTemperature(this.mc.world, biome, pos);
    }
}
