package me.branchpanic.mods.rtfm.mixin;

import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface EnhancedContainerScreen {

    @Accessor("focusedSlot")
    Slot getFocusedSlot();
}
