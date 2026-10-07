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
import com.oxnull.seasonality.season.SeasonalOrbitalCalculator;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adjust the brightness of the sun and the stars
 * depending on the season (for example, brighter suns in summer, brighter stars in winter)
 */
@Mixin(World.class)
public abstract class ClientWorldLuminanceMixin {
    @Inject(method = "getSunBrightness", at = @At("HEAD"), cancellable = true)
    public void onGetSunBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        World world = (World) (Object) this;
        if (!TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) return;

        float bodyBrightness = world.getSunBrightnessBody(partialTicks);
        cir.setReturnValue(SeasonalOrbitalCalculator.applySunBrightness(world, bodyBrightness));
    }

    @Inject(method = "getStarBrightness", at = @At("HEAD"), cancellable = true)
    public void onGetStarBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        World world = (World) (Object) this;
        if (!TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) return;

        float bodyBrightness = world.getStarBrightnessBody(partialTicks);
        cir.setReturnValue(SeasonalOrbitalCalculator.applyStarBrightness(world, bodyBrightness));
    }
}
