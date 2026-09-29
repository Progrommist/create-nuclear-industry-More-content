package com.createnuclearindustrys.Ponder;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class UraniumFuelRodScene {
    public static void basic(SceneBuilder scene, SceneBuildingUtil util) {
        {
            scene.title("uranium_fuel_rod", "Uranium Fuel Rods");
            scene.configureBasePlate(0, 0, 5);

            BlockPos rodRight = util.grid().at(3, 1, 2);
            BlockPos rodLeft  = util.grid().at(1, 1, 2);
            BlockPos mid      = util.grid().at(2, 1, 2);

            // show_structure (attachKeyFrame) + idle 15
            scene.showBasePlate();
            scene.addKeyframe();
            scene.idle(15);

            // set_block правого стержня: direction=down, анимация 20 тиков, keyframe
            scene.world().showSection(util.select().position(rodRight), Direction.DOWN);
            scene.addKeyframe();

            text(scene, util, 60, 3.5, 1.5, 2.0, "Fuel rods emit particles", false);
            scene.idle(70);

            text(scene, util, 60, 3.5, 1.0, 2.5, "The higher the temperature, the more particles", false);
            scene.idle(70);

            // set_block левого стержня
            scene.world().showSection(util.select().position(rodLeft), Direction.DOWN);

            text(scene, util, 60, 1.5, 1.0, 2.5, "When a particle hits another rod...", false);
            scene.idle(60);

            text(scene, util, 40, 3.5, 1.0, 2.5, "Temp: 0", true);
            text(scene, util, 40, 1.5, 1.5, 2.0, "Temp: 20", false);
            scene.idle(50);

            scene.effects().indicateSuccess(mid);
            scene.idle(20);

            text(scene, util, 60, 1.5, 1.0, 2.5, "This rod's temperature increases!", false);
            scene.idle(70);

            text(scene, util, 40, 3.5, 2.5, 2.0, "Temp: 20", false);
            text(scene, util, 40, 1.5, 2.0, 2.5, "Temp: 22.5", false);
            scene.idle(40);

            text(scene, util, 60, 1.5, 1.0, 2.5, "Also rod's durability decrease", false);
            scene.idle(70);

            // Вода между стержнями (immediateDisplay=true, без анимации) + keyframe
            scene.world().showSection(util.select().position(mid), Direction.UP);
            scene.addKeyframe();

            // Стержни становятся waterlogged
            for (BlockPos rod : new BlockPos[]{rodRight, rodLeft}) {
                scene.world().modifyBlock(rod,
                        s -> s.setValue(BlockStateProperties.WATERLOGGED, true), false);
            }
            scene.idle(20);

            text(scene, util, 40, 2.5, 1.0, 2.5, "A particle that enters water...", false);
            scene.idle(50);

            text(scene, util, 40, 3.5, 1.0, 2.5, "Temp: 20", true);
            text(scene, util, 40, 1.5, 1.5, 2.0, "Temp: 20", false);
            scene.idle(50);

            text(scene, util, 60, 1.5, 1.0, 2.5, "Once it hits the rod, it heats it 4 times more!", false);
            scene.idle(70);

            scene.effects().indicateSuccess(mid);
            scene.idle(20);

            text(scene, util, 40, 3.5, 1.0, 2.5, "Temp: 20", true);
            text(scene, util, 40, 1.5, 1.5, 2.0, "Temp: 25", false);
            scene.idle(50);

            text(scene, util, 60, 2.5, 1.0, 2.5, "However, rods underwater cool down 3 times faster", false);
            scene.idle(60);

            scene.markAsFinished();
        }
    }
    private static void text(SceneBuilder scene, SceneBuildingUtil util, int ticks,
                             double x, double y, double z, String text, boolean blue) {
        var builder = scene.overlay().showText(ticks)
                .text(text)
                .pointAt(new Vec3(x, y, z))
                .placeNearTarget();
        if (blue) builder.colored(PonderPalette.BLUE);
    }
}
