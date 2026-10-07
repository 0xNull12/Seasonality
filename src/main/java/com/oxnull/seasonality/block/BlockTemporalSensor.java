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
package com.oxnull.seasonality.block;

import com.oxnull.seasonality.api.ISeasonalityBlock;
import com.oxnull.seasonality.api.SeasonalityBlocks;
import com.oxnull.seasonality.api.season.SeasonalityAPI;
import com.oxnull.seasonality.config.TemporalConfig;
import com.oxnull.seasonality.core.Seasonality;
import com.oxnull.seasonality.item.ItemSeasonalityBlock;
import com.oxnull.seasonality.season.TemporalCalendar;
import com.oxnull.seasonality.tileentity.TileEntityTemporalSensor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockTemporalSensor extends BlockContainer implements ISeasonalityBlock {
    public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
    public static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.375D, 1.0D);

    private final SeasonalDetectorType detectorType;

    public BlockTemporalSensor(SeasonalDetectorType type) {
        super(Material.WOOD);
        this.detectorType = type;
        this.setHardness(0.2F);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, 0));
    }

    @Override public Class<? extends ItemBlock> getItemClass() {
        return ItemSeasonalityBlock.class;
    }

    @Override public IProperty<?>[] getPresetProperties() {
        return new IProperty<?>[] {};
    }

    @Override public IProperty<?>[] getNonRenderingProperties() {
        return new IProperty<?>[] { POWER };
    }

    @Override public String getStateName(IBlockState state) {
        return detectorType.getName();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(POWER);
    }

    public void updatePower(World world, BlockPos pos) {
        IBlockState currentState = world.getBlockState(pos);

        if (!TemporalConfig.isDimensionWhitelisted(world.provider.getDimension())) {
            if (currentState.getValue(POWER) != 0) {
                world.setBlockState(pos, currentState.withProperty(POWER, 0), 3);
            }
            return;
        }

        int power = 0;
        int seasonDuration = TemporalCalendar.ZERO.getSeasonDuration();
        int startTicks = this.detectorType.ordinal() * seasonDuration;
        int endTicks = startTicks + seasonDuration;
        int currentTicks = SeasonalityAPI.getTemporalState(world).getCycleTicks();

        if (currentTicks >= startTicks && currentTicks <= endTicks) {
            float delta = (float)(currentTicks - startTicks) / (float)seasonDuration;
            power = (int)Math.min(delta * 15.0F + 1.0F, 15.0F);
        }

        if (currentState.getValue(POWER) != power) {
            world.setBlockState(pos, currentState.withProperty(POWER, power), 3);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (!player.isAllowEdit()) return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);

        SeasonalDetectorType currentType = getTypeOfBlockAt(world, pos);
        SeasonalDetectorType[] types = SeasonalDetectorType.values();
        SeasonalDetectorType nextType = types[(currentType.ordinal() + 1) % types.length];

        Block nextBlock = getBlockForType(nextType);
        if (nextBlock == null) {
            Seasonality.LOGGER.error("Cannot switch sensor at {}: block for {} is null", pos, nextType);
            return false;
        }

        int power = state.getValue(POWER);

        world.removeTileEntity(pos);

        world.setBlockState(pos, nextBlock.getDefaultState().withProperty(POWER, power), 2);

        if (nextBlock instanceof BlockTemporalSensor) {
            ((BlockTemporalSensor) nextBlock).updatePower(world, pos);
        }

        Seasonality.LOGGER.info("Sensor at {} switched: {} -> {}", pos, currentType.getName(), nextType.getName());
        return true;
    }

    private static SeasonalDetectorType getTypeOfBlockAt(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == SeasonalityBlocks.SEASON_SENSOR_SUMMER) return SeasonalDetectorType.SUMMER;
        if (block == SeasonalityBlocks.SEASON_SENSOR_AUTUMN) return SeasonalDetectorType.AUTUMN;
        if (block == SeasonalityBlocks.SEASON_SENSOR_WINTER) return SeasonalDetectorType.WINTER;
        return SeasonalDetectorType.SPRING;
    }

    private Block getBlockForType(SeasonalDetectorType type) {
        Block result;
        switch(type) {
            case SPRING:
                result = SeasonalityBlocks.SEASON_SENSOR_SPRING;
                break;
            case SUMMER:
                result = SeasonalityBlocks.SEASON_SENSOR_SUMMER;
                break;
            case AUTUMN:
                result = SeasonalityBlocks.SEASON_SENSOR_AUTUMN;
                break;
            case WINTER:
                result = SeasonalityBlocks.SEASON_SENSOR_WINTER;
                break;
            default:
                result = null;
        }

        if (result == null) {
            Seasonality.LOGGER.warn(
                    "The block for detector type ‘{}’ is null",
                    type.getName()
            );
        }

        return result;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(SeasonalityBlocks.SEASON_SENSOR_SPRING);
    }

    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean canProvidePower(IBlockState state) { return true; }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTemporalSensor();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(POWER, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(POWER);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POWER);
    }

    public enum SeasonalDetectorType implements IStringSerializable {
        SPRING, SUMMER, AUTUMN, WINTER;

        @Override public String getName() { return this.name().toLowerCase(); }
        @Override public String toString() { return this.getName(); }
    }
}