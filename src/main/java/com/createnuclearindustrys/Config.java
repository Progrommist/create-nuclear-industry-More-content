package com.createnuclearindustrys;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue CONSOLE_DEBUG;

    public static final ModConfigSpec.IntValue AUTO_DISCOVER_TICKS;
    public static final ModConfigSpec.BooleanValue AUTO_DISCOVER_PRIORITY;

    public static final ModConfigSpec.IntValue MELTDOWN_CHECK_TICKS;
    public static final ModConfigSpec.BooleanValue MELTDOWN_CHECK_PRIORITY;

    public static final ModConfigSpec.IntValue HEAT_SYNC_TICKS;
    public static final ModConfigSpec.BooleanValue HEAT_SYNC_PRIORITY;

    public static final ModConfigSpec.IntValue ADVANCEMENT_TRIGGER_TICKS;
    public static final ModConfigSpec.BooleanValue ADVANCEMENT_TRIGGER_PRIORITY;


    static {
        BUILDER.push("Scheduler manager");
        CONSOLE_DEBUG = BUILDER
                .comment("if enabled, debugging will take place in the console.")
                .define("consoleDebug", false);

        AUTO_DISCOVER_TICKS = BUILDER
                .comment("Delay auto-discover block placed using non-standard methods. (placed using /fill, commands, pistons)")
                .defineInRange("auto_discover_ticks", 100, 0, Integer.MAX_VALUE);
        AUTO_DISCOVER_PRIORITY = BUILDER
                .comment("If enabled, auto-discover will work with high priority")
                .define("auto_discover_priority", false);

        MELTDOWN_CHECK_TICKS = BUILDER
                .comment("Delay checks for meltdown")
                .defineInRange("meltdown_check_ticks", 100, 0, Integer.MAX_VALUE);
        MELTDOWN_CHECK_PRIORITY = BUILDER
                .comment("If enabled, meltdown checks will work with high priority")
                .define("meltdown_check_priority", false);

        HEAT_SYNC_TICKS = BUILDER
                .comment("Delay in synchronizing blocks with their temperature")
                .defineInRange("heat_sync_ticks", 10, 0, Integer.MAX_VALUE);
        HEAT_SYNC_PRIORITY = BUILDER
                .comment("If enabled, synchronizing blocks with their temperature will work with high priority")
                .define("heat_sync_priority", false);

        ADVANCEMENT_TRIGGER_TICKS = BUILDER
                .comment("Delay in checks for advancements")
                .defineInRange("advancements_trigger_ticks", 200, 0, Integer.MAX_VALUE);
        ADVANCEMENT_TRIGGER_PRIORITY = BUILDER
                .comment("If enabled, checks for advancements will work with high priority")
                .define("advancements_trigger_priority", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
