package com.createnuclearindustrys.Blocks.ThermalGeneratorBlock;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ThermalGeneratorSteamDisplaySource extends SingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        BlockEntity be = context.getSourceBlockEntity();
        if (!(be instanceof ThermalGeneratorBlockEntity tgbe))
            return Component.literal("---");
        return Component.literal(String.format("%d / %d", tgbe.steamAmount(), tgbe.steamCapacity()));
    }

    /** Allow the user to add a custom label prefix via the Display Link GUI. */
    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

}
