package com.createnuclearindustrys.Ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class SteamTurbineScene {

    public static void basic(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("steam_turbine", "Steam Turbine");
        // show_structure (5,0,1)-(1,0,5): платформа 5x5 со смещением 1,1
        scene.configureBasePlate(1, 1, 5);

        scene.showBasePlate();
        scene.addKeyframe();

        scene.world().showSection(util.select().fromTo(5, 2, 4, 4, 1, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(0, 0, 6, 6, 1, 6), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        text(scene, util, 60, "To turn heat into kinetic energy you need to:");
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(3, 1, 5, 1, 4, 5), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(3, 1, 4, 3, 4, 4), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(1, 4, 4, 2, 2, 4), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        text(scene, util, 80, "1. Turn water into steam in a boiler connected to a heat source");
        scene.idle(90);

        scene.world().showSection(util.select().fromTo(2, 1, 4, 1, 4, 1), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        text(scene, util, 80, "2. Connect the steam turbine to the boiler");
        scene.idle(90);

        textAt(scene, 60, 1.5, 3.5, 3.0,
                "Don't forget to vent steam from the turbine, or it will stop working");
        scene.idle(70);

        textAt(scene, 60, 1.0, 1.5, 2.5, "Speed is always 16 RPM");
        scene.idle(70);

        textAt(scene, 60, 1.0, 1.5, 1.5,
                "The amount of stress generated depends on the steam supply rate");
        scene.idle(70);

        textAt(scene, 60, 3.5, 1.5, 4.0,
                "The rate of water-to-steam conversion depends on temperature");
        scene.idle(70);

        textAt(scene, 60, 4.5, 1.5, 3.5,
                "Keep in mind that the boiler cools the reactor while operating");
        scene.idle(70);

        scene.markAsFinished();
    }

    // Тексты без point в оригинале: указываем на центр структуры
    private static void text(SceneBuilder scene, SceneBuildingUtil util, int ticks, String text) {
        scene.overlay().showText(ticks)
                .text(text)
                .pointAt(util.vector().centerOf(3, 2, 3))
                .placeNearTarget();
    }

    private static void textAt(SceneBuilder scene, int ticks,
                               double x, double y, double z, String text) {
        scene.overlay().showText(ticks)
                .text(text)
                .pointAt(new Vec3(x, y, z))
                .placeNearTarget();
    }
}