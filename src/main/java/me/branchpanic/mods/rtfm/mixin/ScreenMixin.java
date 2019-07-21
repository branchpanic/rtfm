package me.branchpanic.mods.rtfm.mixin;

import me.branchpanic.mods.rtfm.RtfmMod;
import net.minecraft.ChatFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "getTooltipFromItem(Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"))
    public void onTooltipBuilt(ItemStack stack, CallbackInfoReturnable<List<String>> cir) {
        RtfmMod.INSTANCE.getDocRetriever()
                .retrieve(
                        "item",
                        MinecraftClient.getInstance().getLanguageManager().getLanguage().getCode(),
                        Registry.ITEM.getId(stack.getItem())
                ).ifPresent(e -> cir.getReturnValue().add(ChatFormat.DARK_GRAY + "Press " + RtfmMod.HELP_KEY_BINDING.getLocalizedName() + " for more info"));
    }
}
