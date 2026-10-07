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
package com.oxnull.seasonality.util;

import com.google.common.collect.ImmutableSet;
import com.oxnull.seasonality.api.ISeasonalityBlock;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;

/**
 * It is used to manipulate and view the properties of BlockState
 */
public final class BlockStateUtils {
    private BlockStateUtils() {}

    public static String getStateInfoAsString(IBlockState state) {
        StringBuilder desc = new StringBuilder(state.getBlock().getClass().getName()).append("[");
        boolean first = true;

        for (Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
            if (!first) desc.append(",");
            IProperty<?> property = entry.getKey();
            Comparable<?> value = entry.getValue();
            desc.append(property.getName()).append("=").append(getPropertyName(property, value));
            first = false;
        }
        desc.append("]");
        return desc.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

    public static ImmutableSet<IBlockState> getStatesSet(IBlockState baseState, IProperty<?>... properties) {
        Deque<IProperty<?>> propStack = new ArrayDeque<>();
        List<IBlockState> states = new ArrayList<>();

        for (IProperty<?> prop : properties) {
            propStack.push(prop);
        }

        if (!propStack.isEmpty()) {
            addStatesToList(baseState, states, propStack);
        }
        return ImmutableSet.copyOf(states);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> void addStatesToList(IBlockState state, List<IBlockState> list, Deque<IProperty<?>> stack) {
        if (stack.isEmpty()) {
            list.add(state);
            return;
        }

        IProperty<T> prop = (IProperty<T>) stack.pop();
        for (T value : prop.getAllowedValues()) {
            addStatesToList(state.withProperty(prop, value), list, stack);
        }
        stack.push(prop);
    }

    public static ImmutableSet<IBlockState> getBlockPresets(Block block) {
        if (!(block instanceof ISeasonalityBlock)) return ImmutableSet.of();

        IBlockState defaultState = block.getDefaultState();
        if (defaultState == null) defaultState = block.getBlockState().getBaseState();

        return getStatesSet(defaultState, ((ISeasonalityBlock) block).getPresetProperties());
    }

    public static IBlockState getPresetState(IBlockState state) {
        IBlockState outState = state.getBlock().getDefaultState();
        if (state.getBlock() instanceof ISeasonalityBlock) {
            ISeasonalityBlock seasonBlock = (ISeasonalityBlock) state.getBlock();
            for (IProperty<?> property : seasonBlock.getPresetProperties()) {
                outState = setPropertyUnchecked(outState, property, state.getValue(property));
            }
        }
        return outState;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> IBlockState setPropertyUnchecked(IBlockState state, IProperty<T> property, Comparable<?> value) {
        return state.withProperty(property, (T) value);
    }

    public static IProperty<?> getPropertyByName(IBlockState blockState, String propertyName) {
        for (IProperty<?> property : blockState.getProperties().keySet()) {
            if (property.getName().equals(propertyName)) return property;
        }
        return null;
    }

    public static boolean isValidPropertyName(IBlockState blockState, String propertyName) {
        return getPropertyByName(blockState, propertyName) != null;
    }

    public static Comparable<?> getPropertyValueByName(IBlockState blockState, IProperty<?> property, String valueName) {
        for (Comparable<?> value : property.getAllowedValues()) {
            if (value.toString().equals(valueName)) return value;
        }
        return null;
    }
}
