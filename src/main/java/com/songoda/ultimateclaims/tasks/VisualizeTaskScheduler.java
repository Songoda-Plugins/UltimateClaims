package com.songoda.ultimateclaims.tasks;

import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.thread.TaskScheduler;

public class VisualizeTaskScheduler extends TaskScheduler {

    public VisualizeTaskScheduler(SongodaPlugin plugin) {
        super(plugin, 1L, 1L);
    }
}
