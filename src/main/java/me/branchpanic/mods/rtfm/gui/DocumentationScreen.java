package me.branchpanic.mods.rtfm.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.gui.text.StatefulTextRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class DocumentationScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("rtfm", "textures/gui/info.png");

    public static final int CORE_BACKGROUND_WIDTH = 177;
    public static final int CORE_BACKGROUND_HEIGHT = 50;

    public static final int BOX_LEFT = 54;
    public static final int BOX_TOP = 0;
    public static final int CONTENT_PADDING = 8;

    public static final float HORIZONTAL_MARGIN_FACTOR = 0.2f;
    public static final float VERTICAL_MARGIN_FACTOR = 0.1f;

    public static final int SCROLL_NOTCH_VELOCITY = 4;

    private final DocEntry entry;
    private final ItemStack icon;

    private final NinePatchRectangle textRectangle;
    private final TextStyle textStyle;

    private MarkdownView markdownView;

    private int guiLeft;
    private int guiRight;
    private int guiTop;
    private int guiBottom;

    private int textLeft;
    private int textRight;
    private int textTop;
    private int textBottom;

    private int scrollMin;

    private float scrollVelocity;
    private float scrollAmount;

    public DocumentationScreen(DocEntry entry, ItemStack icon) {
        super(new TextComponent("RTFM Documentation"));
        this.entry = entry;
        this.icon = icon;

        textStyle = ImmutableTextStyle.builder()
                .lineSpacingPx(1.8f)
                .blockSpacingPx(8f)
                .putHeadingScales(1, 1.75f)
                .putHeadingScales(2, 1.5f)
                .putHeadingScales(3, 1.25f)
                .build();

        textRectangle = ImmutableNinePatchRectangle.builder()
                .originU(0)
                .originV(50)
                .borderSize(5)
                .fillColor(0xFF212121)
                .sprite(BACKGROUND_TEXTURE)
                .build();
    }

    private int maxTextColumnWidth() {
        return 600;
    }

    @Override
    protected void init() {
        guiLeft = (int) (HORIZONTAL_MARGIN_FACTOR * width);
        guiRight = width - guiLeft;
        guiTop = (int) (VERTICAL_MARGIN_FACTOR * height);
        guiBottom = height - guiTop;

        textLeft = guiLeft + BOX_LEFT + CONTENT_PADDING + textRectangle.borderSize();
        textRight = Math.min(guiRight - CONTENT_PADDING - textRectangle.borderSize(), maxTextColumnWidth());
        textTop = guiTop + BOX_TOP + CONTENT_PADDING + textRectangle.borderSize();
        textBottom = guiBottom - CONTENT_PADDING - textRectangle.borderSize();

        StatefulTextRenderer textRenderer = new StatefulTextRenderer(textStyle, font, textLeft, textTop, textRight);
        markdownView = new MarkdownView(textStyle, entry.node(), textRenderer);

        scrollMin = Math.min(-(markdownView.calculateHeight() - textBottom / 2), 0);
        scrollAmount = Math.max(scrollAmount, scrollMin);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);

        if (width < CORE_BACKGROUND_WIDTH || height < CORE_BACKGROUND_HEIGHT) {
            String sizeMessage = I18n.translate("ui.rtfm.too_small");
            int size = font.getStringWidth(sizeMessage);
            font.draw(sizeMessage, (width - size) / 2f, (height - font.fontHeight) / 2f, 0xFFFFFFFF);
            return;
        }

        scrollAmount = MathHelper.clamp(scrollAmount + 6 * scrollVelocity, scrollMin, 0);
        scrollVelocity = 0.55f * scrollVelocity;

        // To prevent smooth scrolling from lasting too long, we just cut it off after the velocity is negligible.
        if (Math.abs(scrollVelocity) <= 0.005) {
            scrollVelocity = 0;
        }

        textRectangle.draw(this, guiLeft + BOX_LEFT, guiTop, (guiRight - (guiLeft + BOX_LEFT)), (guiBottom - guiTop));

        MinecraftClient.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        blit(guiLeft, guiTop, 0, 0, CORE_BACKGROUND_WIDTH, CORE_BACKGROUND_HEIGHT);
        drawScrollBar();

        GuiLighting.enable();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(guiLeft + 8f, guiTop + 8f, 0f);
        GlStateManager.scalef(2f, 2f, 2f);
        itemRenderer.renderGuiItem(icon, 0, 0);
        GlStateManager.popMatrix();

        GuiLighting.disable();

        Window window = MinecraftClient.getInstance().window;

        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        float scaledWidth = window.getScaledWidth();
        float scaledHeight = window.getScaledHeight();

        int scissorX = screenToWindowX(textLeft);
        int scissorY = screenToWindowY(textTop);

        int scissorWidth = (int) (windowWidth * ((textRight - textLeft) / scaledWidth));
        int scissorHeight = (int) (windowHeight * ((textBottom - textTop) / scaledHeight));

        GL11.glScissor(scissorX, windowHeight - scissorHeight - scissorY, scissorWidth, scissorHeight);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, scrollAmount, 0);
        markdownView.render(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (scrollAmount != 0) {
            drawDashedHorizontalLine(
                    textLeft,
                    textTop - CONTENT_PADDING / 4,
                    guiRight - textLeft - CONTENT_PADDING - textRectangle.borderSize(),
                    0xFF313131
            );
        }

        if (scrollAmount > scrollMin + textBottom) {
            drawDashedHorizontalLine(
                    textLeft,
                    textBottom + CONTENT_PADDING / 4,
                    guiRight - textLeft - CONTENT_PADDING - textRectangle.borderSize(),
                    0xFF313131
            );
        }
    }

    private void drawScrollBar() {
        int contentHeight = -scrollMin;
        int contentWindowHeight = (guiBottom - guiTop) - 2 * CONTENT_PADDING;

        int barSize = (int) ((contentWindowHeight / (float) contentHeight) * contentWindowHeight);

        int barTop = (guiTop + CONTENT_PADDING) - (int) ((scrollAmount / (float) contentHeight) * (contentWindowHeight - 2 * barSize));
        int barBottom = barTop + barSize;

        fill(
                guiRight - textRectangle.borderSize() - 2,
                barTop + textRectangle.borderSize(),
                guiRight - textRectangle.borderSize(),
                barBottom,
                0xFFFF0000
        );
    }

    private int screenToWindowX(int x) {
        Window window = MinecraftClient.getInstance().window;
        return (int) (window.getWidth() * (x / (float) window.getScaledWidth()));
    }

    private int screenToWindowY(int y) {
        Window window = MinecraftClient.getInstance().window;
        return (int) (window.getHeight() * (y / (float) window.getScaledHeight()));
    }

    private void drawDashedHorizontalLine(int x, int y, int length, int color) {
        for (int i = x; i + 4 < x + length; i += 6) {
            fill(i, y, i + 4, y + 1, color);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (scrollMin >= 0) {
            return false;
        }

        scrollVelocity = (float) (SCROLL_NOTCH_VELOCITY * amount);

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollMin >= 0) {
            return false;
        }

        scrollVelocity = 0;
        scrollAmount = (float) MathHelper.clamp(scrollAmount + deltaY, scrollMin, 0);

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
