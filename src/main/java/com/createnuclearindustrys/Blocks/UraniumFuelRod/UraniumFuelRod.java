package com.createnuclearindustrys.Blocks.UraniumFuelRod;

import com.createnuclearindustrys.Manament.RadiationManager;
import com.createnuclearindustrys.Utills.Interfaces.HeatNodeBlock;
import com.createnuclearindustrys.Utills.Interfaces.Heat_syncer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Map;

public class UraniumFuelRod extends Block implements EntityBlock, HeatNodeBlock, Heat_syncer, SimpleWaterloggedBlock {
    public static final IntegerProperty HEAT_LEVEL = IntegerProperty.create("heat_level", 0, 15);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public UraniumFuelRod(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HEAT_LEVEL, 0).setValue(WATERLOGGED, false));
    }

    /** Glows with heat; a submerged hot rod lights the pool more, backing up the Cherenkov glow. */
    public static int lightLevel(BlockState state) {
        int heat = state.getValue(HEAT_LEVEL);
        return state.getValue(WATERLOGGED) && heat > 0 ? Math.min(15, heat + 5) : heat;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof UraniumFuelRodEntity ufre) {
                int maxDamage = stack.getMaxDamage();
                int damage = maxDamage - stack.getDamageValue();
                ufre.setDurability(Math.max(0, Math.min(damage, maxDamage)));
            }
        }
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UraniumFuelRodEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEAT_LEVEL, WATERLOGGED);
    }
    /**
     * Waterlogging. Create's contraptions leave the water behind when they pick this block up,
     * and recompute waterlogging from wherever the block lands, so a rod hauled out of a
     * reactor pool by a pulley does not carry water up with it.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean inWater = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return defaultBlockState().setValue(WATERLOGGED, inWater);
    }
    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Heat-level updates also re-set this block; only react when a rod newly arrives
        if (oldState.is(this) || !(level instanceof ServerLevel serverLevel)) return;
        RadiationManager.get(serverLevel).registerMovedRod(serverLevel, pos, state.getValue(HEAT_LEVEL));
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof UraniumFuelRodEntity entity) {
            int durability = entity.getDurability();
            player.sendSystemMessage(Component.literal(String.format("[Uranium fuel] Durability: %d", durability)));
        } else {
            player.sendSystemMessage(Component.literal("[Uranium fuel] No BlockEntity found!"));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void heat_sync(Map.Entry<BlockPos, Float> entry, ServerLevel level, float MAX_TEMP) {
        BlockPos pos = entry.getKey();
        float heat = entry.getValue();
        BlockState current = level.getBlockState(pos);
        int newLevel = Math.min(15, (int)(heat / MAX_TEMP * 15));
        if (current.getValue(UraniumFuelRod.HEAT_LEVEL) != newLevel)
            level.setBlock(pos, current.setValue(UraniumFuelRod.HEAT_LEVEL, newLevel), 2);
    }
}
