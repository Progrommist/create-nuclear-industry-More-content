package com.createnuclearindustrys.UraniumFluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

public class UraniumFluidType extends FluidType {
    public UraniumFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            // Reuse water textures; the tint makes it look like steam
            private static final ResourceLocation STILL =
                    ResourceLocation.withDefaultNamespace("block/water_still");
            private static final ResourceLocation FLOW =
                    ResourceLocation.withDefaultNamespace("block/water_still");
            private static final ResourceLocation OVERLAY =
                    ResourceLocation.withDefaultNamespace("misc/underwater");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return OVERLAY;
            }
            @Override
            public int getTintColor() {
                return 0xFF60FF0A;
            }
        });
    }
}
