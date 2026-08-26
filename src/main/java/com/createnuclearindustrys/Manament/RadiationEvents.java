package com.createnuclearindustrys.Manament;

import com.createnuclearindustrys.CreateNuclearIndustrys;
import com.createnuclearindustrys.Blocks.HeatGaugeBlock.HeatGaugeBlock;
import com.createnuclearindustrys.Blocks.HeatPipeBlock.HeatPipeBlock;
import com.createnuclearindustrys.Blocks.ThermalGeneratorBlock.ThermalGeneratorBlock;
import com.createnuclearindustrys.Blocks.UraniumFuelRod.UraniumFuelRod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = CreateNuclearIndustrys.MODID)
public class RadiationEvents {

    @SubscribeEvent
    public static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        RadiationManager manager = RadiationManager.get(serverLevel);
        manager.tick(serverLevel);

        List<RadiationParticle> born = manager.drainPendingBroadcast();
        if (born.isEmpty()) return;

        List<ServerPlayer> playersWithGoggles = serverLevel.getPlayers(
                player -> hasEngineersGoggles(player)
        );

        if (playersWithGoggles.isEmpty()) return;

        for (RadiationParticle p : born) {
            RadiationBirthPacket particlePacket = new RadiationBirthPacket(
                    p.id,
                    p.pos.x, p.pos.y, p.pos.z,
                    p.vel.x, p.vel.y, p.vel.z,
                    p.r, p.g, p.b,
                    p.energy,
                    p.ticksLeft,
                    p.source.asLong()
            );
            for (ServerPlayer player : playersWithGoggles) {
                PacketDistributor.sendToPlayer(player, particlePacket);
            }
        }
        //for (RadiationParticle p : born) {
        //PacketDistributor.sendToPlayersInDimension(serverLevel, new RadiationBirthPacket(
        //p.id,
        //p.pos.x, p.pos.y, p.pos.z,
        //p.vel.x, p.vel.y, p.vel.z,
        //p.r, p.g, p.b,
        //p.energy,
        //p.ticksLeft,
        //p.source.asLong()
        //));
    }


    private static boolean hasEngineersGoggles(ServerPlayer player) {
        ItemStack helmet = player.getInventory().getArmor(3); // 3 = helmet slot

        // Проверяем, является ли шлем инженерными очками Create
        return helmet.getItem() == com.simibubi.create.AllItems.GOGGLES.get();
    }


    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        Block placed = event.getPlacedBlock().getBlock();
        if (placed instanceof UraniumFuelRod || placed instanceof HeatGaugeBlock
                || placed instanceof HeatPipeBlock || placed instanceof ThermalGeneratorBlock) {
            RadiationManager.get(serverLevel).registerRod(event.getPos(), serverLevel);
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        Block broken = event.getState().getBlock();
        if (broken instanceof UraniumFuelRod || broken instanceof HeatGaugeBlock
                || broken instanceof HeatPipeBlock || broken instanceof ThermalGeneratorBlock) {
            RadiationManager.get(serverLevel).removeRod(event.getPos(), serverLevel);
        }
    }
}
