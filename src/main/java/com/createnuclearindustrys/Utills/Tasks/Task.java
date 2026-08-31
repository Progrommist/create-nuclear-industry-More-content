package com.createnuclearindustrys.Utills.Tasks;

public final class Task {
    public TickCommand method;
    public int ticks;
    public final ConfigTaskCache configTaskCache;

    public Task(TickCommand method, int ticks, ConfigTaskCache configTaskCache) {
        this.method = method;
        this.ticks = ticks;
        this.configTaskCache = configTaskCache;
    }
}