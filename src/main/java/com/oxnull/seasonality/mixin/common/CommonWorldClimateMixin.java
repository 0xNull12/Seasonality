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
package com.oxnull.seasonality.mixin.common;

import com.oxnull.seasonality.api.season.ITemporalState;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.season.SeasonalityASMHelper;
import com.oxnull.seasonality.season.SeasonalOrbitalCalculator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Common mixin (server + client) that intercepts World's main methods
 *  to inject seasonal logic involving the sun's angle, snow, ice, and rain
 */
@Mixin(World.class)
public abstract class CommonWorldClimateMixin {

    /**
     * Returns the current world instance from the mixin target
     */
    private World self() {
        return (World) (Object) this;
    }

    /**
     * Checks if seasons are active in the current dimension
     */
    private boolean isSeasonsActive() {
        return TemporalConfig.isDimensionWhitelisted(self().provider.getDimension());
    }

    // Celestial Angle (sun/moon position)

    @Inject(method = "getCelestialAngle", at = @At("HEAD"), cancellable = true)
    public void onGetCelestialAngle(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (!isSeasonsActive()) return;

        World world = self();
        cir.setReturnValue(
            SeasonalOrbitalCalculator.calculate(world, world.getWorldTime(), partialTicks)
        );
    }

    // Snow formation

    @Inject(method = "canSnowAt", at = @At("HEAD"), cancellable = true)
    public void onCanSnowAt(BlockPos pos, boolean checkLight, CallbackInfoReturnable<Boolean> cir) {
        if (!isSeasonsActive()) return;

        World world = self();
        ITemporalState state = SeasonalityAPI.getTemporalState(world);
        cir.setReturnValue(
            SeasonalityASMHelper.canSnowAtInSeason(world, pos, checkLight, state)
        );
    }

    // Water freezing

    @Inject(method = "canBlockFreeze", at = @At("HEAD"), cancellable = true)
    public void onCanBlockFreeze(BlockPos pos, boolean noWaterAdj, CallbackInfoReturnable<Boolean> cir) {
        if (!isSeasonsActive()) return;

        World world = self();
        ITemporalState state = SeasonalityAPI.getTemporalState(world);
        cir.setReturnValue(
            SeasonalityASMHelper.canBlockFreezeInSeason(world, pos, noWaterAdj, state)
        );
    }

    // Rainfall check

    @Inject(
        method = "isRainingAt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome;"
        ),
        cancellable = true
    )
    public void onIsRainingAt(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!isSeasonsActive()) return;

        World world = self();
        ITemporalState state = SeasonalityAPI.getTemporalState(world);
        cir.setReturnValue(
            SeasonalityASMHelper.isRainingAtInSeason(world, pos, state)
        );
    }
}
