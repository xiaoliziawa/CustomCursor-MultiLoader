package fr.atesab.customcursormod.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import fr.atesab.customcursormod.common.CursorMod;
import fr.atesab.customcursormod.common.client.CursorScreenHelper;
import fr.atesab.customcursormod.common.config.Configuration;
import fr.atesab.customcursormod.common.cursor.CursorClick;
import fr.atesab.customcursormod.common.cursor.CursorType;
import fr.atesab.customcursormod.common.cursor.SelectZone;
import fr.atesab.customcursormod.common.gui.GuiConfig;
import fr.atesab.customcursormod.common.gui.GuiCursorConfig;
import fr.atesab.customcursormod.common.gui.GuiUtils;
import fr.atesab.customcursormod.common.gui.screen.CommonScreen;
import fr.atesab.customcursormod.common.gui.screen.CommonScreenHandler;
import fr.atesab.customcursormod.common.gui.text.StringCommonText;
import fr.atesab.customcursormod.common.gui.text.TranslationCommonText;
import fr.atesab.customcursormod.common.gui.widget.CommonButton;
import fr.atesab.customcursormod.common.gui.widget.CommonTextField;
import fr.atesab.customcursormod.common.handler.CommonResourceLocation;
import fr.atesab.customcursormod.common.handler.GameType;
import fr.atesab.customcursormod.common.utils.I18nHelper;
import fr.atesab.customcursormod.fabric.command.CustomCursorCommand;
import fr.atesab.customcursormod.fabric.gui.FabricBasicCommonScreen;
import fr.atesab.customcursormod.fabric.gui.FabricGuiSelectZone;
import fr.atesab.customcursormod.fabric.mixin.AbstractContainerScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

public class FabricCursorMod implements ClientModInitializer {
    private static FabricCursorMod instance;
    private final CursorMod mod = new CursorMod(GameType.FABRIC);
    private final CursorScreenHelper cursorScreens = new CursorScreenHelper(FabricBasicCommonScreen::new);

    static {
        SelectZone.SUPPLIER.forType(GameType.FABRIC, o -> new FabricGuiSelectZone(o.xPosition, o.yPosition, o.width, o.height));
        GuiUtils.SUPPLIER.forType(GameType.FABRIC, GuiUtils::new);
        TranslationCommonText.SUPPLIER.forType(GameType.FABRIC, obj -> new TranslationCommonText(obj.format, obj.args));
        StringCommonText.SUPPLIER.forType(GameType.FABRIC, StringCommonText::new);
        CommonResourceLocation.SUPPLIER.forType(GameType.FABRIC, CommonResourceLocation::new);
        CommonButton.SUPPLIER.forType(GameType.FABRIC, CommonButton::new);
        CommonTextField.SUPPLIER.forType(GameType.FABRIC, CommonTextField::new);
        CommonScreen.SUPPLIER.forType(GameType.FABRIC, CommonScreen::new);
        CommonScreen.SUPPLIER_CURRENT.forType(GameType.FABRIC, v -> {
            Screen screen = Minecraft.getInstance().gui.screen();
            if (screen instanceof CommonScreenHandler handler) {
                return handler.getCommonScreen();
            }
            return CommonScreen.createNull();
        });
        I18nHelper.SUPPLIER.forType(GameType.FABRIC, obj -> I18n.get(obj.format, obj.args));
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        ResourceLoader.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(GuiCursorConfig.IMAGE_RELOAD_ID, GuiCursorConfig.IMAGE_RELOAD_LISTENER);
        ClientCommandRegistrationCallback.EVENT.register(CustomCursorCommand::register);

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            mod.waiter.tick();
            CustomCursorCommand.tick();
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> this.setup());

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            mod.forceNextCursor();

            ScreenEvents.afterTick(screen).register(screen1 -> {
                if (!mod.getCursorClicks().isEmpty() && client.gui == null) {
                    mod.getCursorClicks().clear();
                }
            });

            ScreenMouseEvents.afterMouseClick(screen).register((screen1, mouseButtonEvent, consumed) -> {
                if (mouseButtonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT && mod.getConfig().clickAnimation) {
                    mod.getCursorClicks().add(new CursorClick(mouseButtonEvent.x(), mouseButtonEvent.y()));
                    return true;
                }
                return false;
            });
        });
    }

    public static void handleScreenRender(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        if (instance != null) {
            instance.onDrawScreen(Minecraft.getInstance(), screen, guiGraphics, mouseX, mouseY, tickDelta);
        }
    }

    public static Screen createConfigScreen(Screen parent) {
        return GuiConfig.create(new FabricBasicCommonScreen(parent)).getHandle();
    }

    public void onDrawScreen(Minecraft minecraft, Screen gui, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
        Configuration config = mod.getConfig();
        CursorType newCursorType = CursorType.POINTER;
        if (config.dynamicCursor) {
            newCursorType = CursorScreenHelper.getHoveredCursor(gui, mouseX, mouseY);

            if (gui instanceof AbstractContainerScreen<?> container) {
                LocalPlayer player = minecraft.player;
                if (player != null && !player.containerMenu.getCarried().getItem().equals(Items.AIR)) {
                    newCursorType = CursorType.HAND_GRAB;
                } else if (((AbstractContainerScreenAccessor) container).getHoveredSlot() != null
                        && ((AbstractContainerScreenAccessor) container).getHoveredSlot().hasItem()) {
                    newCursorType = CursorType.HAND;
                }
            } else if (gui instanceof ChatScreen) {
                Minecraft mc = minecraft;
                int mx = (int) (mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth()
                        / (double) mc.getWindow().getScreenWidth());
                int my = (int) (mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight()
                        / (double) mc.getWindow().getScreenHeight());
                ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(mc.font, mx, my);
                mc.gui.hud.getChat().captureClickableText(
                        finder,
                        mc.getWindow().getGuiScaledHeight(),
                        mc.gui.hud.getGuiTicks(),
                        ChatComponent.DisplayMode.FOREGROUND
                );
                Style style = finder.result();
                if (style != null && style.getClickEvent() != null) {
                    newCursorType = CursorType.HAND;
                }
            }

            newCursorType = cursorScreens.testCursor(mod, newCursorType, gui, mouseX, mouseY, tickDelta);
        }

        mod.changeCursor(newCursorType);

        if (config.clickAnimation) {
            Iterator<CursorClick> iterator = mod.getCursorClicks().iterator();
            while (iterator.hasNext()) {
                CursorClick cursorClick = iterator.next();
                int posX = (int) cursorClick.getPosX();
                int posY = (int) cursorClick.getPosY();

                try {
                    Identifier texture = Identifier.withDefaultNamespace("textures/gui/click_" + cursorClick.getImage() + ".png");
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, posX - 8, posY - 8, 0, 0, 16, 16, 16, 16);
                } catch (Exception ignored) {
                }

                cursorClick.descreaseTime(tickDelta);
                if (cursorClick.getTime() <= 0) {
                    iterator.remove();
                }
            }
        }
    }

    private void setup() {
        File saveDir = new File(Minecraft.getInstance().gameDirectory, "config");
        try {
            Files.createDirectories(saveDir.toPath());
        } catch (IOException e) {
            throw new RuntimeException("can't create directories: " + saveDir, e);
        }

        File save = new File(saveDir, CursorMod.MOD_ID + ".json");
        mod.getConfig().sync(save);
        mod.markReady();
    }
}
