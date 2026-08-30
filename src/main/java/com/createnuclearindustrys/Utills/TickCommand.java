package com.createnuclearindustrys.Utills;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Set;

public interface TickCommand {
    void execute(ServerLevel level);
}
