package com.createnuclearindustrys.Blocks.ReactimeterBlock;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ReactimeterDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity be = context.getSourceBlockEntity();
        if (!(be instanceof ReactimeterBlockEntity rbe))
            return Component.literal("---");
        return Component.literal(String.format("%.3f", rbe.reactivity));
    }

    /** Allow the user to add a custom label prefix via the Display Link GUI. */
    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
