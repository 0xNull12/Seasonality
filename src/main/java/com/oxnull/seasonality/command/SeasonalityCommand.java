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
package com.oxnull.seasonality.command;

import com.oxnull.seasonality.api.season.AnnualSeason;
import com.oxnull.seasonality.handler.season.TemporalManager;
import com.oxnull.seasonality.season.TemporalCalendar;
import com.oxnull.seasonality.season.TemporalSavedData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SeasonalityCommand extends CommandBase {
    private static final String[] SUB_PHASE_NAMES = Arrays.stream(AnnualSeason.SubPhase.VALUES)
            .map(e -> e.toString().toLowerCase())
            .toArray(String[]::new);

    @Override
    public String getName() {
        return "seasonality";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("season");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.seasonality.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        } else if ("setseason".equals(args[0])) {
            setSeason(sender, args);
        } else if ("getseason".equals(args[0])) {
            getSeason(sender, args);
        } else {
            sender.sendMessage(new TextComponentTranslation(getUsage(sender)));
        }
    }

    private void getSeason(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        TemporalSavedData data = TemporalManager.getTemporalSavedData(player.world);

        int seasonCycleTicks = data.seasonCycleTicks;
        TemporalCalendar time = new TemporalCalendar(seasonCycleTicks);
        AnnualSeason.SubPhase phase = time.getSubPhase();

        int subPhaseDuration = time.getSubPhaseDuration();
        int index = Arrays.asList(AnnualSeason.SubPhase.VALUES).indexOf(phase);

        int ticksTillNext;
        if (index == 11) {
            int cycleDuration = subPhaseDuration * 12;
            ticksTillNext = cycleDuration - seasonCycleTicks;
        } else {
            ticksTillNext = subPhaseDuration * (index + 1) - seasonCycleTicks;
        }

        int totalSeconds = ticksTillNext / 20;
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        
        sender.sendMessage(new TextComponentTranslation("commands.seasonality.getseason", getPhaseName(phase), days, hours));
    }

    private void setSeason(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        AnnualSeason.SubPhase newPhase = null;
        String inputPhase = args[1].toLowerCase();

        for (AnnualSeason.SubPhase phase : AnnualSeason.SubPhase.VALUES) {
            if (phase.toString().toLowerCase().equals(inputPhase)) {
                newPhase = phase;
                break;
            }
        }

        if (newPhase != null) {
            TemporalSavedData seasonData = TemporalManager.getTemporalSavedData(player.world);
            seasonData.seasonCycleTicks = TemporalCalendar.ZERO.getSubPhaseDuration() * newPhase.ordinal();
            seasonData.markDirty();
            TemporalManager.sendSeasonUpdate(player.world);
            sender.sendMessage(new TextComponentTranslation("commands.seasonality.setseason.success", getPhaseName(newPhase)));
        } else {
            sender.sendMessage(new TextComponentTranslation("commands.seasonality.setseason.fail", args[1]));
        }
    }

    private ITextComponent getPhaseName(AnnualSeason.SubPhase phase) {
        return new TextComponentTranslation(phase.getTranslationKey());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "setseason", "getseason");
        } else if (args.length == 2 && "setseason".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, SUB_PHASE_NAMES);
        }
        return super.getTabCompletions(server, sender, args, pos);
    }
}
