package com.createnuclearindustrys.Blocks.UraniumFuelRod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class UraniumFuelRod extends Block implements EntityBlock{
    public static final IntegerProperty HEAT_LEVEL = IntegerProperty.create("heat_level", 0, 15);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public UraniumFuelRod(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HEAT_LEVEL, 0));
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
        builder.add(HEAT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
}
