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
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class DocumentationScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("rtfm", "textures/gui/info.png");

    public static final int CORE_BACKGROUND_WIDTH = 177;
    public static final int CORE_BACKGROUND_HEIGHT = 50;

    public static final int BOX_LEFT = 54;
    public static final int BOX_TOP = 0;
    public static final int CONTENT_PADDING = 5;

    public static final float HORIZONTAL_MARGIN_FACTOR = 0.2f;
    public static final float VERTICAL_MARGIN_FACTOR = 0.1f;

    public static final int SCROLL_NOTCH_VELOCITY = 4;

    private final DocEntry entry;
    private final ItemStack representation;

    private final NinePatchRectangle textRectangle;
    private final TextStyle textStyle;

    private StatefulTextRenderer textRenderer;
    private MarkdownView document;

    private int left;
    private int right;
    private int top;
    private int bottom;

    private int textLeft;
    private int textRight;
    private int textTop;
    private int textBottom;

    private int scrollMin;

    private float scrollVelocity;
    private float offsetY;

    public DocumentationScreen(DocEntry entry, ItemStack representation) {
        super(new TextComponent("RTFM Documentation"));
        this.entry = entry;
        this.representation = representation;

        textStyle = ImmutableTextStyle.builder()
                .lineSpacingPx(1.5f)
                .blockSpacingPx(6f)
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
        return font.getStringWidth(StringUtils.repeat("@", 75));
    }

    @Override
    protected void init() {
        super.init();

        left = (int) (HORIZONTAL_MARGIN_FACTOR * width);
        right = width - left;
        top = (int) (VERTICAL_MARGIN_FACTOR * height);
        bottom = height - top;

        textLeft = left + BOX_LEFT + CONTENT_PADDING + textRectangle.borderSize();
        textRight = Math.min(right - CONTENT_PADDING - textRectangle.borderSize(), maxTextColumnWidth());
        textTop = top + BOX_TOP + CONTENT_PADDING + textRectangle.borderSize();
        textBottom = bottom - CONTENT_PADDING - textRectangle.borderSize();

        textRenderer = new StatefulTextRenderer(textStyle, font, textLeft, textTop, textRight);
        document = new MarkdownView(textStyle, entry.node(), textRenderer);

        scrollMin = Math.min(-(document.calculateHeight() - textBottom / 2), 0);
        offsetY = Math.max(offsetY, scrollMin);
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

        offsetY = MathHelper.clamp(offsetY + 6 * scrollVelocity, scrollMin, 0);
        scrollVelocity = 0.55f * scrollVelocity;

        textRectangle.draw(this, left + BOX_LEFT, top, (right - (left + BOX_LEFT)), (bottom - top));

        MinecraftClient.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        blit(left, top, 0, 0, CORE_BACKGROUND_WIDTH, CORE_BACKGROUND_HEIGHT);

        GuiLighting.enable();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(left + 8f, top + 8f, 0f);
        GlStateManager.scalef(2f, 2f, 2f);
        itemRenderer.renderGuiItem(representation, 0, 0);
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
        GlStateManager.translatef(0, offsetY, 0);
        document.render(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (offsetY != 0) {
            drawDashedHorizontalLine(textLeft, textTop - 2, textRight - textLeft, 0xFF313131);
        }

        if (offsetY > scrollMin + textBottom) {
            drawDashedHorizontalLine(textLeft, textBottom + 1, textRight - textLeft, 0xFF313131);
        }
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
        offsetY = (float) MathHelper.clamp(offsetY + deltaY, scrollMin, 0);

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
