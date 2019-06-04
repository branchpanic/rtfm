package me.branchpanic.mods.rtfm;

import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.api.DocLoader;
import me.branchpanic.mods.rtfm.gui.DocumentationScreen;
import me.branchpanic.mods.rtfm.impl.ImmutableDirectoryDocLoader;
import me.branchpanic.mods.rtfm.mixin.EnhancedContainerScreen;
import me.shedaniel.cloth.hooks.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Paths;

/**
 * The RtfmMod singleton is the active instance of RTFM.
 */
public enum RtfmMod implements ClientModInitializer {
    @SuppressWarnings("unused") INSTANCE;

    private static final String KEY_BIND_SECTION = "RTFM";
    private static final FabricKeyBinding HELP_KEY_BINDING = FabricKeyBinding.Builder.create(
            new Identifier("rtfm", "help"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            KEY_BIND_SECTION
    ).build();

    private Logger logger = LogManager.getLogger("rtfm");
    private DocLoader docLoader;

    public void showEntry(DocEntry entry, ItemStack representation) {
        MinecraftClient.getInstance().openScreen(new DocumentationScreen(entry, representation));
    }

    @Override
    public void onInitializeClient() {
        docLoader = ImmutableDirectoryDocLoader.builder()
                .root(Paths.get("."))
                .build();

        logger.info("rtfm: using documentation loader: {}", docLoader.toString());

        ClothClientHooks.SCREEN_KEY_PRESSED.register((client, screen, keyCode, scanCode, modifiers) -> {
            if (!(screen instanceof AbstractContainerScreen) || screen.getFocused() != null) {
                return ActionResult.PASS;
            }

            Slot selectedSlot = ((EnhancedContainerScreen) screen).getFocusedSlot();

            if (selectedSlot == null || !HELP_KEY_BINDING.matchesKey(keyCode, scanCode)) {
                return ActionResult.PASS;
            }

            ItemStack selectedStack = selectedSlot.getStack();

            if (selectedStack.isEmpty()) {
                return ActionResult.PASS;
            }

            docLoader.retrieve("item", Registry.ITEM.getId(selectedStack.getItem())).ifPresent(e -> showEntry(e, selectedStack));

            return ActionResult.SUCCESS;
        });

        logger.info("rtfm: ready on client");
    }
}
