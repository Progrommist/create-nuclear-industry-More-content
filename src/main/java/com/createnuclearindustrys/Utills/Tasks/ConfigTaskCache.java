package com.createnuclearindustrys.Utills.Tasks;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ConfigTaskCache {
    private Supplier<Integer> configDelay;
    private Supplier<Boolean> configPriority;

    public int currentDelay;
    public boolean currentPriority;

    public ConfigTaskCache(@Nullable TaskCreator taskCreator) {
        if (taskCreator == null) return;

        this.configDelay = taskCreator.ticksToUpdate;
        this.configPriority = taskCreator.taskPriority;

        refresh();
    }

    public void refresh() {
        currentDelay = configDelay.get();
        currentPriority = configPriority.get();
    }
}