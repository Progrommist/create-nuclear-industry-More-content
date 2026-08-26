package com.createnuclearindustrys.Blocks.ThermalGeneratorBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ThermalGeneratorRenderer implements BlockEntityRenderer<ThermalGeneratorBlockEntity> {

    public ThermalGeneratorRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ThermalGeneratorBlockEntity be, float partialTick, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {

        Direction axis = be.getBlockState().getValue(BlockStateProperties.FACING);
        //BlockState shaftState = KineticBlockEntityRenderer.shaft(axis.getAxis());
        BlockState blockState = be.getBlockState();

        SuperByteBuffer buf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState);

        // FINALY IT WORK

        KineticBlockEntityRenderer.standardKineticRotationTransform(buf, be, light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}