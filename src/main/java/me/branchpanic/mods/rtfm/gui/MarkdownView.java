package me.branchpanic.mods.rtfm.gui;

import me.branchpanic.mods.rtfm.gui.text.StatefulTextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import org.commonmark.node.*;

public class MarkdownView extends AbstractVisitor implements Drawable, Element {
    private final TextStyle style;
    private final StatefulTextRenderer renderer;
    private final Node node;

    private int cachedHeight = -1;

    public MarkdownView(TextStyle style, Node node, StatefulTextRenderer renderer) {
        this.style = style;
        this.node = node;
        this.renderer = renderer;
    }

    public int calculateHeight() {
        renderer.reset();
        renderer.setSimulate(true);
        visitChildren(node);
        int result = (int) Math.ceil(renderer.getCaretY());
        renderer.setSimulate(false);
        return result;
    }

    private void beginBlock(Node node) {
        Node previous = node.getPrevious();

        if (previous == null) {
            return;
        }

        if (previous instanceof Heading) {
            renderer.setScale(style.headingScales().getOrDefault(((Heading) previous).getLevel(), 1f));
        }

        renderer.newBlock();
        renderer.setScale(1f);
    }

    private void beginInlineBlock(Node node) {
        Node previous = node.getPrevious();

        if (previous == null) {
            return;
        }

        if (previous instanceof Text) {
            renderer.write(" ");
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        renderer.write(" ");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderer.reset();
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
        renderer.writeBlock(text.getLiteral());
        visitChildren(text);
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        beginInlineBlock(strongEmphasis);
        renderer.setBold(true);
        visitChildren(strongEmphasis);
        renderer.setBold(false);
    }

    @Override
    public void visit(BulletList bulletList) {
        renderer.increaseIndent(2);
        renderer.newBlock();
        visitChildren(bulletList);
        renderer.decreaseIndent(2);
    }

    @Override
    public void visit(ListItem listItem) {
        renderer.newLine();
        visitChildren(listItem);
    }

    @Override
    public void visit(Emphasis emphasis) {
        beginInlineBlock(emphasis);
        renderer.setItalic(true);
        visitChildren(emphasis);
        renderer.setItalic(false);
    }

    @Override
    public void visit(Heading heading) {
        beginBlock(heading);
        renderer.setScale(style.headingScales().getOrDefault(heading.getLevel(), 1f));
        visitChildren(heading);
        renderer.setScale(1f);
    }

    @Override
    public void visit(Link link) {
        beginInlineBlock(link);
        renderer.setUnderline(true);
        renderer.setColor(0xFF4450FF);
        visitChildren(link);
        renderer.setColor(0xFFFFFFFF);
        renderer.setUnderline(false);
    }
}
