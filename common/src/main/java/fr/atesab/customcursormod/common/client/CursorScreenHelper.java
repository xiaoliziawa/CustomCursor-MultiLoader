package fr.atesab.customcursormod.common.client;

import fr.atesab.customcursormod.common.CursorMod;
import fr.atesab.customcursormod.common.cursor.CursorType;
import fr.atesab.customcursormod.common.cursor.SelectZone;
import fr.atesab.customcursormod.common.gui.screen.CommonScreen;
import fr.atesab.customcursormod.common.gui.screen.CommonScreenHandler;
import fr.atesab.customcursormod.common.gui.widget.CommonButton;
import fr.atesab.customcursormod.common.gui.widget.CommonElement;
import fr.atesab.customcursormod.common.gui.widget.CommonTextField;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.function.Function;

public final class CursorScreenHelper {
    private final Function<Screen, CommonScreen> screenFactory;
    private Screen cachedScreen;
    private CommonScreen cachedCommonScreen;

    public CursorScreenHelper(Function<Screen, CommonScreen> screenFactory) {
        this.screenFactory = screenFactory;
    }

    public static CursorType getHoveredCursor(Screen screen, int mouseX, int mouseY) {
        if (screen instanceof CommonScreenHandler handler) {
            List<CommonElement> children = handler.getCommonScreen().childrens;
            for (int index = children.size() - 1; index >= 0; index--) {
                CommonElement child = children.get(index);
                if (!child.isEnable() || !child.isHover(mouseX, mouseY)) {
                    continue;
                }
                if (child instanceof CommonTextField) {
                    return CursorType.BEAM;
                }
                if (child instanceof CommonButton) {
                    return CursorType.HAND;
                }
                if (child instanceof SelectZone) {
                    return CursorType.CROSS;
                }
            }
            return CursorType.POINTER;
        }
        return getHoveredCursor(screen.children(), mouseX, mouseY);
    }

    private static CursorType getHoveredCursor(List<? extends GuiEventListener> children, int mouseX, int mouseY) {
        for (int index = children.size() - 1; index >= 0; index--) {
            GuiEventListener child = children.get(index);
            if (child instanceof AbstractButton button) {
                if (button.visible && button.active && button.isMouseOver(mouseX, mouseY)) {
                    return CursorType.HAND;
                }
            } else if (child instanceof EditBox textField) {
                if (textField.isVisible() && textField.isMouseOver(mouseX, mouseY)) {
                    return CursorType.BEAM;
                }
            } else if (child instanceof ContainerEventHandler container && child.isMouseOver(mouseX, mouseY)) {
                CursorType cursor = getHoveredCursor(container.children(), mouseX, mouseY);
                if (cursor != CursorType.POINTER) {
                    return cursor;
                }
            }
        }
        return CursorType.POINTER;
    }

    public CursorType testCursor(CursorMod mod, CursorType cursor, Screen screen, int mouseX, int mouseY, float partialTick) {
        List<CursorType> cursorTypes = mod.getCursorTesterTypes();
        if (cursorTypes.isEmpty()) {
            return cursor;
        }
        if (screen != cachedScreen) {
            cachedScreen = screen;
            cachedCommonScreen = screen instanceof CommonScreenHandler handler
                    ? handler.getCommonScreen() : screenFactory.apply(screen);
        }
        for (int index = 0, count = cursorTypes.size(); index < count; index++) {
            CursorType cursorType = cursorTypes.get(index);
            if (cursorType.getCursorTester().testCursor(cursor, cachedCommonScreen, mouseX, mouseY, partialTick)) {
                return cursorType;
            }
        }
        return cursor;
    }
}
