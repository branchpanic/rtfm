package me.branchpanic.mods.rtfm.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.immutables.value.Value;

@Value.Immutable
public interface NinePatchRectangle {
    Identifier sprite();

    int borderSize();

    int fillColor();

    int originU();

    int originV();

    default void draw(Screen screen, int x, int y, int width, int height) {
        int borderSize = borderSize();

        MinecraftClient.getInstance().getTextureManager().bindTexture(sprite());

        int leftColumnU = originU();
        int topRowV = originV();

        screen.blit(x, y, leftColumnU, topRowV, borderSize, borderSize);
        screen.blit(x + width - borderSize, y, leftColumnU + borderSize + 1, topRowV, borderSize, borderSize);
        screen.blit(x, y + height - borderSize, leftColumnU, topRowV + borderSize + 1, borderSize, borderSize);
        screen.blit(x + width - borderSize, y + height - borderSize, leftColumnU + borderSize + 1, topRowV + borderSize + 1, borderSize, borderSize);

        for (int i = x + borderSize; i < x + width - borderSize; i++) {
            screen.blit(i, y, leftColumnU + borderSize, topRowV, 1, borderSize);
            screen.blit(i, y + height - borderSize, leftColumnU + borderSize, topRowV + borderSize + 1, 1, borderSize);
        }

        for (int i = y + borderSize; i < y + height - borderSize; i++) {
            screen.blit(x, i, leftColumnU, topRowV + borderSize, borderSize, 1);
            screen.blit(x + width - borderSize, i, leftColumnU + borderSize + 1, topRowV + borderSize, borderSize, 1);
        }

        Screen.fill(x + borderSize, y + borderSize, x + width - borderSize, y + height - borderSize, fillColor());
        GlStateManager.color4f(1f, 1f, 1f, 1f);
    }
}
