package com.createnuclearindustrys.Utills;

public class DelayedTask {
    public final boolean currentPriority;
    public final int taskIndex;

    public DelayedTask(boolean currentPriority, int taskIndex) {
        this.currentPriority = currentPriority;
        this.taskIndex = taskIndex;
    }
}
