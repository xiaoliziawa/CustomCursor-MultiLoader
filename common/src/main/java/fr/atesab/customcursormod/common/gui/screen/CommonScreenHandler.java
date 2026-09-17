package fr.atesab.customcursormod.common.gui.screen;

import fr.atesab.customcursormod.common.handler.CommonMatrixStack;
import fr.atesab.customcursormod.common.gui.GuiUtils;
import fr.atesab.customcursormod.common.gui.widget.CommonButton;
import fr.atesab.customcursormod.common.gui.widget.CommonButtonValue;
import fr.atesab.customcursormod.common.gui.widget.CommonElement;
import fr.atesab.customcursormod.common.gui.widget.CommonTextField;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CommonScreenHandler extends Screen {
    private final CommonScreen.ScreenListener listener;
    private GuiGraphicsExtractor currentGuiGraphics;

    public CommonScreenHandler(Component title, CommonScreen.ScreenListener listener) {
        super(title);
        this.listener = listener;
    }

    /**
     * Get the CommonScreen instance associated with this handler
     * @return the CommonScreen instance
     */
    public CommonScreen getCommonScreen() {
        return listener.getScreen();
    }

    /**
     * Get the current GuiGraphics instance for rendering
     * @return current GuiGraphics
     */
    public GuiGraphicsExtractor getCurrentGuiGraphics() {
        return currentGuiGraphics;
    }

    public void setCurrentGuiGraphics(GuiGraphicsExtractor guiGraphics) {
        this.currentGuiGraphics = guiGraphics;
    }

    @Override
    protected void init() {
        super.init();

        this.getCommonScreen().resize(width, height);

        this.getCommonScreen().init();

        CommonScreen commonScreen = this.getCommonScreen();

        for (CommonElement element : commonScreen.childrens) {
            if (element instanceof CommonButtonValue<?> buttonValue) {
                this.addRenderableWidget(buttonValue.getHandle().handle);
            } else if (element instanceof CommonButton button) {
                this.addRenderableWidget(button.handle);
            } else if (element instanceof CommonTextField textField) {
                this.addRenderableWidget(textField.handle);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.setCurrentGuiGraphics(guiGraphics);

        GuiUtils.setCurrentGuiGraphics(guiGraphics);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        if (this.minecraft != null) {
            CommonMatrixStack stack = new CommonMatrixStack(guiGraphics.pose());
            this.getCommonScreen().listener.render(stack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.getCommonScreen().listener.resize(width, height);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        boolean listenerHandled = this.getCommonScreen().listener.charTyped((char) event.codepoint(), 0);
        boolean superHandled = super.charTyped(event);
        return listenerHandled || superHandled;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean listenerHandled = this.getCommonScreen().listener.keyPressed(event.key(), event.keycode(), event.modifiers());
        boolean superHandled = super.keyPressed(event);
        return listenerHandled || superHandled;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        boolean superHandled = super.mouseClicked(event, isDoubleClick);
        boolean listenerHandled = this.getCommonScreen().listener.mouseClicked(event.x(), event.y(), event.button());
        return superHandled || listenerHandled;
    }

    @Override
    public void tick() {
        super.tick();
        this.getCommonScreen().listener.tick();
    }

    public Font getTextRenderer() {
        return font;
    }
}
