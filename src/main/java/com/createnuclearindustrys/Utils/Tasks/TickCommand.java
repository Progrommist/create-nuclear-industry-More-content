package com.createnuclearindustrys.Utils.Tasks;

import net.minecraft.server.level.ServerLevel;

public interface TickCommand {
    void execute(ServerLevel level);
}