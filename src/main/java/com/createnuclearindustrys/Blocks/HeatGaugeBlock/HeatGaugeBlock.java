package com.createnuclearindustrys.Blocks.HeatGaugeBlock;

import com.createnuclearindustrys.Manament.RadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class HeatGaugeBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape BASE_SHAPE = Shapes.box(
            4.0/16.0, 0.0/16.0, 4.0/16.0,
            12.0/16.0, 2.0/16.0, 12.0/16.0
    );

    // Хитбоксы для разных направлений
    private static final VoxelShape SHAPE_DOWN = Shapes.box(
            4.0/16.0, 14.0/16.0, 4.0/16.0,
            12.0/16.0, 16.0/16.0, 12.0/16.0
    );

    private static final VoxelShape SHAPE_UP = BASE_SHAPE;

    private static final VoxelShape SHAPE_NORTH = Shapes.box(
            4.0/16.0, 4.0/16.0, 14.0/16.0,
            12.0/16.0, 12.0/16.0, 16.0/16.0
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.box(
            4.0/16.0, 4.0/16.0, 0.0/16.0,
            12.0/16.0, 12.0/16.0, 2.0/16.0
    );

    private static final VoxelShape SHAPE_WEST = Shapes.box(
            14.0/16.0, 4.0/16.0, 4.0/16.0,
            16.0/16.0, 12.0/16.0, 12.0/16.0
    );

    private static final VoxelShape SHAPE_EAST = Shapes.box(
            0.0/16.0, 4.0/16.0, 4.0/16.0,
            2.0/16.0, 12.0/16.0, 12.0/16.0
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }
    public HeatGaugeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatGaugeBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        float heat = RadiationManager.get((ServerLevel) level).getHeat(pos);
        player.sendSystemMessage(Component.literal(String.format("[Heat Gauge] %.1f°C", heat)));

        return InteractionResult.SUCCESS;
    }

    // ── Analog redstone output ────────────────────────────────────────────────

    /** The Heat Gauge emits a comparator/redstone signal proportional to its heat. */
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /**
     * Returns 0–15 linearly mapped to 0°C – 1000°C (meltdown temperature).
     * A comparator placed next to the gauge will read this value directly.
     */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof HeatGaugeBlockEntity be)
            return Math.min(15, (int)(be.heat / 1000f * 15));
        return 0;
    }

    /** Needed alongside isSignalSource so the game knows to call getAnalogOutputSignal. */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
}
