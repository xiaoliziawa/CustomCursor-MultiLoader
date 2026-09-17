package fr.atesab.customcursormod.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.atesab.customcursormod.fabric.FabricCursorMod;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;

public class CustomCursorCommand {
    private static int scheduledTicks = 0;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                LiteralArgumentBuilder.<FabricClientCommandSource>literal("customcursor")
                        .executes(CustomCursorCommand::openConfigGui)
        );
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        scheduledTicks = 2;
        return 1;
    }

    public static void tick() {
        if (scheduledTicks > 0) {
            scheduledTicks--;

            if (scheduledTicks == 0) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.gui.setScreen(FabricCursorMod.createConfigScreen(minecraft.gui.screen()));
            }
        }
    }
}
