package me.branchpanic.mods.rtfm.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.branchpanic.mods.rtfm.api.DocEntry;
import me.branchpanic.mods.rtfm.gui.ImmutableTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Identifier;

public class DocumentationScreen extends Screen {
    public static final Identifier BACKGROUND_TEXTURE = new Identifier("rtfm", "textures/gui/info.png");

    public static final int BACKGROUND_WIDTH = 182;
    public static final int BACKGROUND_HEIGHT = 144;

    public static final int CORE_BACKGROUND_WIDTH = 177;
    public static final int CORE_BACKGROUND_HEIGHT = 50;

    public static final int BOX_TOP_LEFT_X = 59;
    public static final int BOX_TOP_LEFT_Y = 5;

    public static final int NINEPATCH_TOP_EDGE_START_X = CORE_BACKGROUND_WIDTH;
    public static final int NINEPATCH_TOP_EDGE_START_Y = 0;
    public static final int NINEPATCH_LEFT_EDGE_START_X = 54;
    public static final int NINEPATCH_LEFT_EDGE_START_Y = 50;

    public static final int NINEPATCH_DECORATION_SIZE = 5;
    public static final int NINEPATCH_BOTTOM_LEFT_U = 0;
    public static final int NINEPATCH_BOTTOM_LEFT_V = 56;
    public static final int NINEPATCH_BOTTOM_RIGHT_U = 6;
    public static final int NINEPATCH_BOTTOM_RIGHT_V = 56;
    public static final int NINEPATCH_TOP_RIGHT_U = 6;
    public static final int NINEPATCH_TOP_RIGHT_V = 50;
    public static final int NINEPATCH_LEFT_EDGE_U = 0;
    public static final int NINEPATCH_LEFT_EDGE_V = 55;
    public static final int NINEPATCH_RIGHT_EDGE_U = 6;
    public static final int NINEPATCH_RIGHT_EDGE_V = 55;
    public static final int NINEPATCH_BOTTOM_EDGE_U = 5;
    public static final int NINEPATCH_BOTTOM_EDGE_V = 56;
    public static final int NINEPATCH_TOP_EDGE_U = 5;
    public static final int NINEPATCH_TOP_EDGE_V = 50;

    public static final int NINEPATCH_FILL_COLOR = 0xFF212121;

    public static final float HORIZONTAL_MARGIN_FACTOR = 0.2f;
    public static final float VERTICAL_MARGIN_FACTOR = 0.1f;

    private final DocEntry entry;
    private final ItemStack representation;

    private MarkdownView view;

    private int left;
    private int top;

    private float offsetY;

    public DocumentationScreen(DocEntry entry, ItemStack representation) {
        super(new TextComponent("RTFM Documentation"));
        this.entry = entry;
        this.representation = representation;
    }

    @Override
    protected void init() {
        super.init();

        Theme t = ImmutableTheme.builder()
                .lineSpacingPx(1.5f)
                .blockSpacingPx(6f)
                .putHeadingScales(1, 1.75f)
                .putHeadingScales(2, 1.5f)
                .putHeadingScales(3, 1.25f)
                .build();

        left = (int) (HORIZONTAL_MARGIN_FACTOR * width);
        top = (int) (VERTICAL_MARGIN_FACTOR * height);

        view = new MarkdownView(t, entry.node(), new StatefulTextRenderer(t, font, left + BOX_TOP_LEFT_X + 8, top + BOX_TOP_LEFT_Y + 8, 300));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);

        int right = width - left;
        int bottom = height - top;

        MinecraftClient.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        blit(left, top, 0, 0, CORE_BACKGROUND_WIDTH, CORE_BACKGROUND_HEIGHT);

        // Top edge

        for (int i = left + NINEPATCH_TOP_EDGE_START_X; i < right - NINEPATCH_DECORATION_SIZE; i++) {
            blit(i, top + NINEPATCH_TOP_EDGE_START_Y, NINEPATCH_TOP_EDGE_U, NINEPATCH_TOP_EDGE_V, 1, NINEPATCH_DECORATION_SIZE);
        }

        // Bottom edge

        for (int i = left + NINEPATCH_LEFT_EDGE_START_X + NINEPATCH_DECORATION_SIZE; i < right - NINEPATCH_DECORATION_SIZE; i++) {
            blit(i, bottom - NINEPATCH_DECORATION_SIZE, NINEPATCH_BOTTOM_EDGE_U, NINEPATCH_BOTTOM_EDGE_V, 1, NINEPATCH_DECORATION_SIZE);
        }

        // Left edge

        for (int i = top + NINEPATCH_LEFT_EDGE_START_Y; i < bottom - NINEPATCH_DECORATION_SIZE; i++) {
            blit(left + NINEPATCH_LEFT_EDGE_START_X, i, NINEPATCH_LEFT_EDGE_U, NINEPATCH_LEFT_EDGE_V, NINEPATCH_DECORATION_SIZE, 1);
        }

        // Right edge

        for (int i = top + NINEPATCH_DECORATION_SIZE; i < bottom - NINEPATCH_DECORATION_SIZE; i++) {
            blit(right - NINEPATCH_DECORATION_SIZE, i, NINEPATCH_RIGHT_EDGE_U, NINEPATCH_RIGHT_EDGE_V, NINEPATCH_DECORATION_SIZE, 1);
        }

        // Corners

        blit(right - NINEPATCH_DECORATION_SIZE, top, NINEPATCH_TOP_RIGHT_U, NINEPATCH_TOP_RIGHT_V, NINEPATCH_DECORATION_SIZE, NINEPATCH_DECORATION_SIZE);
        blit(right - NINEPATCH_DECORATION_SIZE, bottom - NINEPATCH_DECORATION_SIZE, NINEPATCH_BOTTOM_RIGHT_U, NINEPATCH_BOTTOM_RIGHT_V, NINEPATCH_DECORATION_SIZE, NINEPATCH_DECORATION_SIZE);
        blit(left + NINEPATCH_LEFT_EDGE_START_X, bottom - NINEPATCH_DECORATION_SIZE, NINEPATCH_BOTTOM_LEFT_U, NINEPATCH_BOTTOM_LEFT_V, NINEPATCH_DECORATION_SIZE, NINEPATCH_DECORATION_SIZE);

        fill(left + BOX_TOP_LEFT_X, top + BOX_TOP_LEFT_Y, right - NINEPATCH_DECORATION_SIZE, bottom - NINEPATCH_DECORATION_SIZE, NINEPATCH_FILL_COLOR);

        GlStateManager.pushMatrix();
        GlStateManager.translatef(left + 8f, top + 8f, 0f);
        GlStateManager.scalef(2f, 2f, 2f);
        itemRenderer.renderGuiItem(representation, 0, 0);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, offsetY, 0);
        view.render(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mouseScrolled(double double_1, double double_2, double amount) {
        offsetY += 4 * amount;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
