package com.createnuclearindustrys.Utils.Interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

public interface Heat_syncer {
    void heat_sync(Map.Entry<BlockPos, Float> entry, ServerLevel level, float MAX_TEMP);
}
