package me.branchpanic.mods.rtfm.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.ChatFormat;
import net.minecraft.client.font.TextRenderer;
import org.apache.commons.lang3.StringUtils;

/**
 * A StatefulTextRenderer wraps a TextRenderer for use at a high level.
 */
public class StatefulTextRenderer {
    private final Theme theme;
    private final TextRenderer textRenderer;

    private final float minX;
    private final float minY;
    private final float maxX;

    private float lastX;
    private float lastY;
    private float scale;

    private boolean bold;
    private boolean italic;
    private boolean underline;
    private int color;

    private int indent;

    public StatefulTextRenderer(Theme theme, TextRenderer textRenderer, float minX, float minY, float maxX) {
        this.theme = theme;
        this.textRenderer = textRenderer;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;

        color = 0xFFFFFFFF;
    }

    public void newLine() {
        if (lastX == minX) {
            return;
        }

        lastX = minX;
        lastY += textRenderer.fontHeight * scale + theme.lineSpacingPx();
    }

    public void newBlock() {
        if (lastX == minX) {
            return;
        }

        lastX = minX;
        lastY += textRenderer.fontHeight * scale + theme.blockSpacingPx();
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void increaseIndent(int amount) {
        this.indent += amount;
    }

    public void decreaseIndent(int amount) {
        this.indent -= amount;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void writeBlock(String text) {
        for (int i = 0; i < indent; i++) {
            write(" ");
        }

        if (indent > 0) {
            text = "* " + text;
        }

        String[] words = text.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i + 1 < words.length) {
                word += " ";
            }

            write(word);
        }
    }

    private String applyCurrentFormatting(String text) {
        StringBuilder drawnTextBuilder = new StringBuilder(text);

        if (bold) {
            drawnTextBuilder.insert(0, ChatFormat.BOLD);
        }

        if (italic) {
            drawnTextBuilder.insert(0, ChatFormat.ITALIC);
        }

        if (underline) {
            drawnTextBuilder.insert(0, ChatFormat.UNDERLINE);
        }

        return drawnTextBuilder.toString();
    }

    public void write(String text) {
        String drawnText = applyCurrentFormatting(text);
        float textWidth = textRenderer.getStringWidth(drawnText) * scale;
        boolean needsWrap = lastX + textWidth > maxX;

        if (StringUtils.isWhitespace(text)) {
            if (!needsWrap) {
                lastX += scale * textWidth;
            }
            return;
        }

        if (needsWrap) {
            newLine();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef(lastX, lastY, 0);
        GlStateManager.scalef(scale, scale, 1);

        textRenderer.draw(drawnText, 0, 0, color);

        GlStateManager.popMatrix();

        lastX += textWidth;
    }

    public void reset() {
        lastX = minX;
        lastY = minY;
        scale = 1f;
    }
}
