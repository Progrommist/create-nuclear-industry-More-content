package com.createnuclearindustrys;

import com.createnuclearindustrys.Ponder.*;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CNIPonderPlugin implements PonderPlugin {
    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ResourceLocation.fromNamespaceAndPath("createnuclearindustrys", "uranium_fuel_rod"))
                .addStoryBoard("uranium_fuel_rod/basic", UraniumFuelRodScene::basic)
                .addStoryBoard("boron_control_rod/basic", BoronControlRodScene::basic)
                .addStoryBoard("reactor/basic", ReactorScene::basic);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(
                        CreateNuclearIndustrys.MODID, "boron_control_rod"))
                .addStoryBoard("boron_control_rod/basic", BoronControlRodScene::basic);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(
                        CreateNuclearIndustrys.MODID, "heat_pipe"))
                .addStoryBoard("heat_pipe/basic", HeatPipeScene::basic);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(
                        CreateNuclearIndustrys.MODID, "steam_turbine"))
                .addStoryBoard("steam_turbine/basic", SteamTurbineScene::basic);

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(
                        CreateNuclearIndustrys.MODID, "boiler"))
                .addStoryBoard("steam_turbine/basic", SteamTurbineScene::basic);
    }

    @Override
    public String getModId() {
        return CreateNuclearIndustrys.MODID;
    }

}