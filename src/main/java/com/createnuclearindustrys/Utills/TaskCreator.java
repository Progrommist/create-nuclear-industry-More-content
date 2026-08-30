package com.createnuclearindustrys.Utills;


import java.util.function.Supplier;

public final class TaskCreator {
    public TickCommand method;
    public final Supplier<Integer> ticksToUpdate;
    public final Supplier<Boolean> taskPriority;

    public TaskCreator(TickCommand method, Supplier<Integer> ticksToUpdate, Supplier<Boolean> taskPriority) {
        this.method = method;
        this.ticksToUpdate = ticksToUpdate;
        this.taskPriority = taskPriority;
    }
}