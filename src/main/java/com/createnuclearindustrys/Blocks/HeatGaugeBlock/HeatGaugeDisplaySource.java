package com.createnuclearindustrys.Blocks.HeatGaugeBlock;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Display source that exposes the Heat Gauge's current temperature
 * to Create's Display Link system (display boards, nixie tubes, signs, etc.).
 *
 * Outputs a single line formatted as "XXX.X°C".
 */
public class HeatGaugeDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity be = context.getSourceBlockEntity();
        if (!(be instanceof HeatGaugeBlockEntity hgbe))
            return Component.literal("---");
        return Component.literal(String.format("%.1f°C", hgbe.heat));
    }

    /** Allow the user to add a custom label prefix via the Display Link GUI. */
    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
