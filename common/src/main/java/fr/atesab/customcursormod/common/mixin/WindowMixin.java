package fr.atesab.customcursormod.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import fr.atesab.customcursormod.common.CursorMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Window.class)
public class WindowMixin {
    @WrapOperation(method = "selectCursor", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/platform/cursor/CursorType;select()V"))
    private void onSelectCursor(CursorType cursor, Operation<Void> original) {
        original.call(cursor);
        CursorMod mod = CursorMod.getInstance();
        if (mod != null) {
            mod.applyCursor();
        }
    }
}

