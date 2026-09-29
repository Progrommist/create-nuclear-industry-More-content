package com.createnuclearindustrys.Ponder;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BoronControlRodScene {

    public static void basic(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("boron_control_rod", "Boron Control Rods");
        scene.configureBasePlate(0, 0, 5);

        // show_structure (attachKeyFrame) + idle 20
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.addKeyframe();
        scene.idle(20);

        // Тексты без точки в оригинале: указываем на центр структуры
        scene.addKeyframe();
        scene.overlay().showText(80)
                .text("Different blocks have different absorption levels")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("The higher the absorption, the more likely a particle is to vanish when it hits the block")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(80)
                .text("The lower the absorption, the more likely a particle is to be reflected when it hits the block")
                .pointAt(util.vector().centerOf(2, 1, 2))
                .placeNearTarget();
        scene.idle(90);

        // Боровый стержень - 90%
        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("Boron control rod absorption: 90%")
                .pointAt(new Vec3(2.5, 1.0, 0.5))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "boron",
                util.select().position(2, 1, 0), 60);
        scene.idle(70);

        // Обсидиан - 40%
        scene.overlay().showText(40)
                .text("Obsidian: 40%")
                .pointAt(new Vec3(2.0, 1.5, 1.5))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "obsidian",
                util.select().position(2, 1, 1), 40);
        scene.idle(50);

        // Ряд z=2 - 25%
        scene.overlay().showText(40)
                .text("These blocks: 25%")
                .pointAt(new Vec3(2.5, 1.0, 2.5))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "row25",
                util.select().fromTo(4, 1, 2, 0, 1, 2), 40);
        scene.idle(50);

        // Ряд z=3 - 15%
        scene.overlay().showText(40)
                .text("And these: 15%")
                .pointAt(new Vec3(2.5, 1.0, 3.5))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "row15",
                util.select().fromTo(4, 1, 3, 0, 1, 3), 40);
        scene.idle(50);

        // Железный блок - 0%
        scene.overlay().showText(40)
                .text("Iron block: 0%")
                .pointAt(new Vec3(2.5, 1.0, 4.5))
                .placeNearTarget();
        scene.overlay().showOutline(PonderPalette.BLUE, "iron",
                util.select().position(2, 1, 4), 40);
        scene.idle(50);

        scene.overlay().showText(60)
                .text("This is why reactor walls should be made of iron")
                .pointAt(new Vec3(2.5, 1.0, 4.5))
                .placeNearTarget();
        scene.idle(70);

        scene.markAsFinished();
    }
}