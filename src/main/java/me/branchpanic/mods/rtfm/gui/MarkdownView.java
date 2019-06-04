package me.branchpanic.mods.rtfm.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import org.commonmark.node.*;

public class MarkdownView extends AbstractVisitor implements Drawable, Element {
    private final Theme theme;
    private final StatefulTextRenderer textRenderer;
    private final Node node;

    public MarkdownView(Theme theme, Node node, StatefulTextRenderer textRenderer) {
        this.theme = theme;
        this.node = node;
        this.textRenderer = textRenderer;
    }

    private void beginBlock(Node node) {
        Node previous = node.getPrevious();

        if (previous == null) {
            return;
        }

        if (previous instanceof Heading) {
            textRenderer.setScale(theme.headingScales().getOrDefault(((Heading) previous).getLevel(), 1f));
        }

        textRenderer.newBlock();
        textRenderer.setScale(1f);
    }

    private void beginInlineBlock(Node node) {
        Node previous = node.getPrevious();

        if (previous == null) {
            return;
        }

        if (previous instanceof Text) {
            textRenderer.write(" ");
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        textRenderer.write(" ");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        textRenderer.reset();
        visitChildren(node);
    }

    @Override
    public void visit(Paragraph paragraph) {
        beginBlock(paragraph);
        visitChildren(paragraph);
    }

    @Override
    public void visit(Text text) {
        beginInlineBlock(text);
        textRenderer.writeBlock(text.getLiteral());
        visitChildren(text);
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        beginInlineBlock(strongEmphasis);
        textRenderer.setBold(true);
        visitChildren(strongEmphasis);
        textRenderer.setBold(false);
    }

    @Override
    public void visit(BulletList bulletList) {
        textRenderer.increaseIndent(2);
        textRenderer.newBlock();
        visitChildren(bulletList);
        textRenderer.decreaseIndent(2);
    }

    @Override
    public void visit(ListItem listItem) {
        textRenderer.newLine();
        visitChildren(listItem);
    }

    @Override
    public void visit(Emphasis emphasis) {
        beginInlineBlock(emphasis);
        textRenderer.setItalic(true);
        visitChildren(emphasis);
        textRenderer.setItalic(false);
    }

    @Override
    public void visit(Heading heading) {
        beginBlock(heading);
        textRenderer.setScale(theme.headingScales().getOrDefault(heading.getLevel(), 1f));
        visitChildren(heading);
        textRenderer.setScale(1f);
    }

    @Override
    public void visit(Link link) {
        beginInlineBlock(link);
        textRenderer.setUnderline(true);
        textRenderer.setColor(0xFF4450FF);
        visitChildren(link);
        textRenderer.setColor(0xFFFFFFFF);
        textRenderer.setUnderline(false);
    }
}
