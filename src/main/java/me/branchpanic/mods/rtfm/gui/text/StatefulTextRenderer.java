package me.branchpanic.mods.rtfm.gui.text;

import com.mojang.blaze3d.platform.GlStateManager;
import me.branchpanic.mods.rtfm.gui.TextStyle;
import net.minecraft.ChatFormat;
import net.minecraft.client.font.TextRenderer;
import org.apache.commons.lang3.StringUtils;

public class StatefulTextRenderer {
    private final TextStyle style;
    private final TextRenderer font;

    private final float minX;
    private final float minY;
    private final float maxX;

    private float caretX;
    private float caretY;
    private float scale;

    private boolean bold;
    private boolean italic;
    private boolean underline;
    private int color;

    private int indent;

    private boolean simulate;

    public StatefulTextRenderer(TextStyle style, TextRenderer font, float minX, float minY, float maxX) {
        this.style = style;
        this.font = font;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;

        color = 0xFFFFFFFF;
    }

    public float getCaretX() {
        return caretX;
    }

    public float getCaretY() {
        return caretY;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void newLine() {
        if (caretX == minX) {
            return;
        }

        caretX = minX;
        caretY += font.fontHeight * scale + style.lineSpacingPx();
    }

    public void newBlock() {
        if (caretX == minX) {
            return;
        }

        caretX = minX;
        caretY += font.fontHeight * scale + style.blockSpacingPx();
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
        float textWidth = font.getStringWidth(drawnText) * scale;
        boolean needsWrap = caretX + textWidth > maxX;

        if (StringUtils.isWhitespace(text)) {
            if (!needsWrap) {
                caretX += scale * textWidth;
            }
            return;
        }

        if (needsWrap) {
            newLine();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef(caretX, caretY, 0);
        GlStateManager.scalef(scale, scale, 1);

        if (!simulate) {
            font.draw(drawnText, 0, 0, color);
        }

        GlStateManager.popMatrix();

        caretX += textWidth;
    }

    public void reset() {
        caretX = minX;
        caretY = minY;
        scale = 1f;
    }
}
