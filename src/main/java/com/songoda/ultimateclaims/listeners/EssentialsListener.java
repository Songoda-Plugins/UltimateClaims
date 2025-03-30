package com.songoda.ultimateclaims.listeners;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.tasks.TrackerTask;
import net.ess3.api.events.FlyStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EssentialsListener implements Listener {

    @EventHandler
    public void onEssentialsFly(FlyStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        TrackerTask.TrackedPlayer trackedPlayer = UltimateClaims.getInstance().getTrackerTask().getTrackedPlayer(player.getUniqueId());
        trackedPlayer.setEssentialsFly(event.getValue());
    }
}
