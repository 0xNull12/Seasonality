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
package com.oxnull.seasonality.item;

import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.season.TemporalCalendar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

/**
 * A clock or calendar that shows what season it is
 */
public class ItemTemporalChronometer extends Item {
    private final Map<Integer, ChronometerAnimationState> animationStates = new HashMap<>();

    public ItemTemporalChronometer() {
        this.addPropertyOverride(new ResourceLocation("time"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World world, EntityLivingBase entity) {
                Entity holder = entity != null ? entity : stack.getItemFrame();
                if (world == null && holder != null) {
                    world = holder.world;
                }

                if (world == null) return 0.0F;

                double frame;
                int dimension = world.provider.getDimension();

                if (TemporalConfig.isDimensionWhitelisted(dimension)) {
                    int cycleTicks = SeasonalityAPI.getTemporalState(world).getCycleTicks();
                    frame = (double) cycleTicks / TemporalCalendar.ZERO.getCycleDuration();
                } else {
                    frame = Math.random();
                }

                ChronometerAnimationState state = animationStates.computeIfAbsent(dimension, k -> new ChronometerAnimationState());
                frame = state.calculateSmoothedFrame(world, frame);
                return MathHelper.positiveModulo((float) frame, 1.0F);
            }
        });
    }

    @SideOnly(Side.CLIENT)
    private static class ChronometerAnimationState {
        private double currentFrame;
        private double velocity;
        private long lastWorldTime;

        private double calculateSmoothedFrame(World world, double targetFrame) {
            long currentTime = world.getTotalWorldTime();
            if (currentTime != this.lastWorldTime) {
                this.lastWorldTime = currentTime;
                double delta = targetFrame - this.currentFrame;

                if (delta < -0.5D) delta += 1.0D;
                if (delta > 0.5D) delta -= 1.0D;

                this.velocity = (this.velocity + delta * 0.1D) * 0.9D;
                this.currentFrame += this.velocity;
            }
            return this.currentFrame;
        }
    }
}
