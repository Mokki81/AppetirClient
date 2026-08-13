package com.appetir.gui;

import com.appetir.alt.Alt;
import com.appetir.alt.AltManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.List;

/**
 * Offline Alt Manager GUI.
 * Opened from ClickGUI or Right Control.
 */
public class AltManagerScreen extends Screen {

    private static final int PANEL_W = 320;
    private static final int ROW_H = 28;
    private static final int PAD = 10;

    private final Screen parent;
    private String input = "";
    private boolean inputFocused = true;
    private int scroll = 0;
    private String status = "";
    private long statusTime = 0;

    public AltManagerScreen(Screen parent) {
        super(new LiteralText("Alt Manager"));
        this.parent = parent;
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        this.renderBackground(m);

        int accent = ThemeManager.getAccentColor();
        int cx = this.width / 2;
        int top = 30;
        int panelX = cx - PANEL_W / 2;
        int panelH = this.height - 60;

        // Panel background
        fill(m, panelX, top, panelX + PANEL_W, top + panelH, 0xE0101018);
        // Border
        drawBorder(m, panelX, top, panelX + PANEL_W, top + panelH, accent);

        // Title
        drawCenteredString(m, textRenderer, "Alt Manager", cx, top + 8, accent);
        drawCenteredString(m, textRenderer, "Offline only · Current: " + AltManager.getInstance().getCurrentName(),
                cx, top + 20, 0xFFAAAAAA);

        // Input field
        int inputY = top + 38;
        fill(m, panelX + PAD, inputY, panelX + PANEL_W - PAD - 70, inputY + 18, inputFocused ? 0xFF1A1A2E : 0xFF111120);
        drawBorder(m, panelX + PAD, inputY, panelX + PANEL_W - PAD - 70, inputY + 18, inputFocused ? accent : 0xFF333344);
        String shown = input.isEmpty() ? (inputFocused ? "|" : "Nickname...") : input + (inputFocused ? "|" : "");
        drawString(m, textRenderer, shown, panelX + PAD + 4, inputY + 5, input.isEmpty() ? 0xFF555566 : 0xFFFFFFFF);

        // Add button
        boolean addHov = in(mx, my, panelX + PANEL_W - PAD - 64, inputY, panelX + PANEL_W - PAD, inputY + 18);
        fill(m, panelX + PANEL_W - PAD - 64, inputY, panelX + PANEL_W - PAD, inputY + 18, addHov ? accent : 0xFF2A2A3A);
        drawCenteredString(m, textRenderer, "Add", panelX + PANEL_W - PAD - 32, inputY + 5, 0xFFFFFFFF);

        // Alts list
        List<Alt> alts = AltManager.getInstance().getAlts();
        int listTop = inputY + 28;
        int listH = panelH - 90;
        int maxVisible = Math.max(1, listH / ROW_H);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, alts.size() - maxVisible)));

        int y = listTop;
        for (int i = scroll; i < Math.min(alts.size(), scroll + maxVisible); i++) {
            Alt alt = alts.get(i);
            boolean hov = in(mx, my, panelX + PAD, y, panelX + PANEL_W - PAD, y + ROW_H - 2);
            boolean current = alt.getName().equalsIgnoreCase(AltManager.getInstance().getCurrentName());

            fill(m, panelX + PAD, y, panelX + PANEL_W - PAD, y + ROW_H - 2,
                    current ? 0x335B8CFF : (hov ? 0x22FFFFFF : 0x11FFFFFF));

            drawString(m, textRenderer, alt.getName(), panelX + PAD + 6, y + 6,
                    current ? accent : 0xFFFFFFFF);
            drawString(m, textRenderer, current ? "§a[ACTIVE]" : "", panelX + PAD + 110, y + 6, 0xFF55FF55);

            // Login btn
            int bx = panelX + PANEL_W - PAD - 110;
            boolean loginHov = in(mx, my, bx, y + 4, bx + 50, y + 20);
            fill(m, bx, y + 4, bx + 50, y + 20, loginHov ? 0xFF2E7D32 : 0xFF1B5E20);
            drawCenteredString(m, textRenderer, "Login", bx + 25, y + 8, 0xFFFFFFFF);

            // Delete btn
            int dx = panelX + PANEL_W - PAD - 54;
            boolean delHov = in(mx, my, dx, y + 4, dx + 44, y + 20);
            fill(m, dx, y + 4, dx + 44, y + 20, delHov ? 0xFFC62828 : 0xFF8B0000);
            drawCenteredString(m, textRenderer, "Del", dx + 22, y + 8, 0xFFFFFFFF);

            y += ROW_H;
        }

        if (alts.isEmpty()) {
            drawCenteredString(m, textRenderer, "No alts yet. Add one above.", cx, listTop + 30, 0xFF666677);
        }

        // Status
        if (!status.isEmpty() && System.currentTimeMillis() - statusTime < 3000) {
            drawCenteredString(m, textRenderer, status, cx, top + panelH - 18, 0xFF55FF55);
        }

        // Back hint
        drawCenteredString(m, textRenderer, "ESC — back", cx, this.height - 18, 0xFF666677);

        super.render(m, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int cx = this.width / 2;
        int top = 30;
        int panelX = cx - PANEL_W / 2;
        int inputY = top + 38;

        // Input focus
        inputFocused = in((int) mx, (int) my, panelX + PAD, inputY, panelX + PANEL_W - PAD - 70, inputY + 18);

        // Add button
        if (in((int) mx, (int) my, panelX + PANEL_W - PAD - 64, inputY, panelX + PANEL_W - PAD, inputY + 18)) {
            if (AltManager.getInstance().addAlt(input)) {
                status = "Added: " + input;
                statusTime = System.currentTimeMillis();
                input = "";
            } else {
                status = "Invalid or already exists";
                statusTime = System.currentTimeMillis();
            }
            return true;
        }

        // List actions
        List<Alt> alts = AltManager.getInstance().getAlts();
        int listTop = inputY + 28;
        int listH = (this.height - 60) - 90;
        int maxVisible = Math.max(1, listH / ROW_H);
        int y = listTop;

        for (int i = scroll; i < Math.min(alts.size(), scroll + maxVisible); i++) {
            Alt alt = alts.get(i);
            int bx = panelX + PANEL_W - PAD - 110;
            int dx = panelX + PANEL_W - PAD - 54;

            if (in((int) mx, (int) my, bx, y + 4, bx + 50, y + 20)) {
                if (AltManager.getInstance().login(alt)) {
                    status = "Logged in as " + alt.getName();
                    statusTime = System.currentTimeMillis();
                } else {
                    status = "Login failed";
                    statusTime = System.currentTimeMillis();
                }
                return true;
            }
            if (in((int) mx, (int) my, dx, y + 4, dx + 44, y + 20)) {
                AltManager.getInstance().removeAlt(alt);
                status = "Removed " + alt.getName();
                statusTime = System.currentTimeMillis();
                return true;
            }
            y += ROW_H;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        scroll -= (int) amount;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.client.setScreen(parent);
            return true;
        }
        if (inputFocused) {
            if (keyCode == 259 && !input.isEmpty()) { // Backspace
                input = input.substring(0, input.length() - 1);
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                if (AltManager.getInstance().addAlt(input)) {
                    status = "Added: " + input;
                    statusTime = System.currentTimeMillis();
                    input = "";
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (inputFocused && chr >= 32 && input.length() < 16) {
            if (Character.isLetterOrDigit(chr) || chr == '_') {
                input += chr;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void drawBorder(MatrixStack m, int x1, int y1, int x2, int y2, int color) {
        fill(m, x1, y1, x2, y1 + 1, color);
        fill(m, x1, y2 - 1, x2, y2, color);
        fill(m, x1, y1, x1 + 1, y2, color);
        fill(m, x2 - 1, y1, x2, y2, color);
    }

    private boolean in(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }
}
