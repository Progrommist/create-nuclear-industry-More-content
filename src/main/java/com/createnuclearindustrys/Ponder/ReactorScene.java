package com.createnuclearindustrys.Ponder;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class ReactorScene {
    private static final double ROD_TRAVEL = 3.0;

    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("nuclear_reactor", "Building a Nuclear Reactor");
        scene.configureBasePlate(0, 0, 8);
        scene.scaleSceneView(0.6f);

        Vec3 toReal = new Vec3(3, 0, 0);

        Selection rodsInside = util.select().fromTo(1, 2, 3, 1, 4, 3)
                .add(util.select().fromTo(0, 2, 4, 0, 4, 4));
        Selection rodsAbove = util.select().fromTo(1, 5, 3, 1, 7, 3)
                .add(util.select().fromTo(0, 5, 4, 0, 7, 4));

        scene.showBasePlate();
        scene.addKeyframe();

        scene.world().showSection(util.select().position(7, 1, 3), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        text(scene, util, 60, "So, how do you build a nuclear reactor?");
        scene.idle(70);

        // 1. пол из тепловых труб
        scene.world().showSection(util.select().fromTo(6, 1, 1, 1, 1, 6), Direction.DOWN);
        text(scene, util, 60, "1. Build a floor of heat pipes");
        scene.idle(70);

        // 2. стены
        scene.world().showSection(util.select().fromTo(2, 2, 2, 5, 5, 2), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(5, 2, 3, 5, 5, 4), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(5, 2, 5, 2, 5, 5), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(2, 2, 4, 2, 5, 3), Direction.DOWN);
        scene.addKeyframe();
        text(scene, util, 60, "2. Build the walls");
        scene.idle(70);

        // 3. стержни

        scene.world().showSection(util.select().fromTo(3, 2, 3, 4, 4, 4), Direction.DOWN);
        ElementLink<WorldSectionElement> rods =
                scene.world().showIndependentSection(rodsInside, Direction.DOWN);
        scene.world().moveSection(rods, toReal, 0);
        scene.addKeyframe();
        text(scene, util, 60, "3. Insert the control rods and fuel rods");
        scene.idle(70);

        // 4. механизм управления
        scene.world().showSection(util.select().fromTo(7, 2, 3, 7, 8, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(6, 5, 2, 2, 10, 5), Direction.DOWN);
        scene.world().showSectionAndMerge(rodsAbove, Direction.DOWN, rods);

        scene.addKeyframe();
        text(scene, util, 60, "4. Add a mechanism to operate the control rods");
        scene.idle(70);

        text(scene, util, 60, "Fasten the rods together with super glue so they can be lifted");
        scene.overlay().showControls(new Vec3(4.5, 4.0, 3.5), Pointing.DOWN, 60)
                .withItem(AllItems.SUPER_GLUE.asStack());
        scene.overlay().showControls(new Vec3(3.5, 4.0, 4.5), Pointing.DOWN, 60)
                .withItem(AllItems.SUPER_GLUE.asStack());
        scene.idle(70);

        // Поднимаем стержни
        scene.addKeyframe();
        text(scene, util, 80, "Now the fuel rods will emit particles and raise the temperature");
        toggleMechanism(scene, util, rods, false, ROD_TRAVEL);
        scene.effects().indicateSuccess(util.grid().at(3, 3, 3));
        scene.effects().indicateSuccess(util.grid().at(4, 2, 4));
        scene.idle(90);

        // Опускаем стержни
        text(scene, util, 60, "To keep the temperature from getting too high, you can lower the rods");
        toggleMechanism(scene, util, rods, true, -ROD_TRAVEL);
        //FINALLY IT WORKS
        scene.idle(70);

        scene.addKeyframe();
        text(scene, util, 100,
                "This example reactor is very unstable: without cooling, even with the rods fully lowered, it will overheat and explode");
        scene.idle(110);

        text(scene, util, 60, "Better build your own reactor with more control rods");
        scene.idle(70);

        scene.markAsFinished();
    }

    private static void toggleMechanism(CreateSceneBuilder scene, SceneBuildingUtil util,
                                        ElementLink<WorldSectionElement> rods,
                                        boolean powered, double dy) {
        scene.effects().indicateRedstone(util.grid().at(5, 9, 3));
        scene.world().toggleRedstonePower(util.select().fromTo(5, 9, 3, 5, 9, 4));
        scene.world().modifyBlocks(util.select().fromTo(5, 8, 3, 3, 8, 4),
                s -> s.hasProperty(BlockStateProperties.POWERED)
                        ? s.setValue(BlockStateProperties.POWERED, powered) : s,
                false);

        scene.world().modifyKineticSpeed(util.select().fromTo(7, 1, 3, 7, 8, 3), f -> -f);
        scene.effects().rotationDirectionIndicator(util.grid().at(7, 3, 3));
        scene.world().moveSection(rods, util.vector().of(0, dy, 0), 40);
    }
    private static void text(CreateSceneBuilder scene, SceneBuildingUtil util, int ticks, String text) {
        scene.overlay().showText(ticks)
                .text(text)
                .pointAt(util.vector().centerOf(4, 3, 4))
                .placeNearTarget();
    }
}
