package fr.atesab.customcursormod.fabric.utils;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.function.Supplier;

public class FabricRenderPipelines {
    public static Supplier<RenderPipeline> CURSOR = () -> RenderPipelines.GUI_TEXTURED;
}
