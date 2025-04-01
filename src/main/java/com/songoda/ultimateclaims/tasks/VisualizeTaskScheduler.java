package com.songoda.ultimateclaims.tasks;

import com.songoda.core.SongodaPlugin;
import com.songoda.core.thread.TaskScheduler;

public class VisualizeTaskScheduler extends TaskScheduler {

    public VisualizeTaskScheduler(SongodaPlugin plugin) {
        super(plugin, 1L, 1L);
    }
}
