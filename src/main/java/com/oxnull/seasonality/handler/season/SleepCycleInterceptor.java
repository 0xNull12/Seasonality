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

import com.oxnull.seasonality.api.config.SeasonalityOption;
import com.oxnull.seasonality.api.config.SyncedConfig;
import com.oxnull.seasonality.season.TemporalSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SleepCycleInterceptor {
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side != net.minecraftforge.fml.relauncher.Side.SERVER) return;
        if (!SyncedConfig.getBooleanValue(SeasonalityOption.ADVANCE_SEASON_WHILE_SLEEPING)) return;

        WorldServer world = (WorldServer) event.world;
        if (world.areAllPlayersAsleep()) {
            TemporalSavedData seasonData = TemporalManager.getTemporalSavedData(world);
            long timeDiff = 24000L - ((world.getWorldInfo().getWorldTime() + 24000L) % 24000L);
            seasonData.seasonCycleTicks += timeDiff;
            seasonData.markDirty();
            TemporalManager.sendSeasonUpdate(world);
        }
    }
}