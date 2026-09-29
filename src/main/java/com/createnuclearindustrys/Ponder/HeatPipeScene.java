package com.createnuclearindustrys.Ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class HeatPipeScene {

    public static void basic(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("heat_pipe", "Heat Pipes");
        scene.configureBasePlate(0, 0, 5);

        // show_structure (0,0,0)-(4,0,4): только слой платформы, keyframe
        scene.showBasePlate();
        scene.addKeyframe();

        // Первые два блока, показываются сверху вниз
        scene.world().showSection(util.select().fromTo(3, 2, 2, 1, 2, 1), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(3, 4, 1, 3, 4, 0), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("Heat pipes transfer heat to their neighbors")
                .pointAt(util.vector().centerOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(70);

        // Температуры: три подписи одновременно
        temp(scene, 3.5, 4.5, 0.5, "Temp: 20", 60);
        temp(scene, 3.5, 2.5, 2.0, "Temp: 100", 60);
        temp(scene, 1.5, 2.5, 1.5, "Temp: 20", 60);
        scene.idle(70);

        // Остальные трубы появляются с севера
        scene.world().showSection(util.select().fromTo(3, 1, 2, 1, 1, 2), Direction.NORTH);
        scene.world().showSection(util.select().fromTo(3, 3, 2, 3, 4, 2), Direction.NORTH);

        scene.overlay().showText(80)
                .text("Unlike fuel rods, they transfer heat not only vertically, but also horizontally")
                .pointAt(util.vector().centerOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(90);

        temp(scene, 3.5, 2.5, 2.0, "Temp: 100", 60);
        temp(scene, 3.5, 4.5, 0.5, "Temp: 100", 60);
        temp(scene, 1.5, 2.5, 1.5, "Temp: 100", 60);
        scene.idle(70);

        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("Keep in mind that heat is not transferred instantly")
                .pointAt(util.vector().centerOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Heat also gradually dissipates if no more heat is coming in")
                .pointAt(util.vector().centerOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }

    private static void temp(SceneBuilder scene, double x, double y, double z,
                             String text, int ticks) {
        scene.overlay().showText(ticks)
                .text(text)
                .pointAt(new Vec3(x, y, z))
                .placeNearTarget();
    }
}