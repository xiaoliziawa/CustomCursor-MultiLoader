package fr.atesab.customcursormod.fabric.mixin;

import fr.atesab.customcursormod.fabric.FabricCursorMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenRenderMixin {
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("TAIL"))
    private void customcursor$afterExtractRenderStateWithTooltipAndSubtitles(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float tickDelta,
            CallbackInfo ci
    ) {
        FabricCursorMod.handleScreenRender((Screen) (Object) this, graphics, mouseX, mouseY, tickDelta);
    }
}
