package com.appetir.gui;

import com.appetir.AppetirClient;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern ClickGUI — redesigned for better visuals and usability.
 * Right Shift to open.
 */
public class ClickGUI extends Screen {

    private static final int SIDEBAR_W = 128;
    private static final int PANEL_W   = 260;
    private static final int ROW_H     = 42;
    private static final int CAT_H     = 28;
    private static final int TOP       = 12;
    private static final int GAP       = 6;

    private Module.Category selected = Module.Category.COMBAT;
    private boolean showTheme = false;
    private boolean showAlts  = false;
    private String search = "";
    private boolean searchFocused = false;
    private int scroll = 0;

    public ClickGUI() {
        super(new LiteralText("Appetir ClickGUI"));
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        int H = this.height;
        int accent = ThemeManager.getAccentColor();
        int dim = ThemeManager.getAccentColor(0.35f);
        int sideH = H - TOP * 2;

        // Dim background
        fill(m, 0, 0, this.width, H, 0xCC00000A);

        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;

        // ── Sidebar ──────────────────────────────────────────────
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + sideH, 0xF0080814);
        drawBorder(m, sx, TOP, sx + SIDEBAR_W, TOP + sideH, dim);

        // Header
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + 40, 0xF0060610);
        drawCenteredString(m, textRenderer, AppetirClient.NAME, sx + SIDEBAR_W / 2, TOP + 8, accent);
        drawCenteredString(m, textRenderer, "v" + AppetirClient.VERSION, sx + SIDEBAR_W / 2, TOP + 20, 0xFF888899);

        // Search
        int sy = TOP + 46;
        fill(m, sx + 4, sy, sx + SIDEBAR_W - 4, sy + 16, searchFocused ? 0xFF16162A : 0xFF0E0E18);
        drawBorder(m, sx + 4, sy, sx + SIDEBAR_W - 4, sy + 16, searchFocused ? accent : 0xFF2A2A3A);
        String sd = search.isEmpty() ? (searchFocused ? "|" : "Search...") : search + (searchFocused ? "|" : "");
        drawString(m, textRenderer, sd, sx + 8, sy + 4, search.isEmpty() ? 0xFF555566 : 0xFFE0E0E0);

        // Categories
        int cy = sy + 22;
        for (Module.Category cat : Module.Category.values()) {
            boolean sel = cat == selected && !showTheme && !showAlts;
            boolean hov = !showTheme && !showAlts && hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H);
            drawCategory(m, cat.displayName, cy, sel, hov, accent);
            cy += CAT_H;
        }

        // Theme button
        boolean themeSel = showTheme;
        boolean themeHov = hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H);
        drawCategory(m, "Theme", cy, themeSel, themeHov, accent);
        cy += CAT_H;

        // Alts button
        boolean altsSel = showAlts;
        boolean altsHov = hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H);
        drawCategory(m, "Alts", cy, altsSel, altsHov, accent);

        // ── Content panel ────────────────────────────────────────
        fill(m, px, TOP, px + PANEL_W, TOP + sideH, 0xF010101A);
        drawBorder(m, px, TOP, px + PANEL_W, TOP + sideH, dim);

        // Panel header
        fill(m, px, TOP, px + PANEL_W, TOP + 22, 0xF00C0C16);
        String header = showAlts ? "Alt Manager" : (showTheme ? "Themes" : (search.isEmpty() ? selected.displayName : "Search"));
        drawString(m, textRenderer, header, px + 10, TOP + 7, accent);

        if (showAlts) {
            // Redirect hint — open full AltManagerScreen
            drawCenteredString(m, textRenderer, "Click to open full Alt Manager", px + PANEL_W / 2, TOP + 60, 0xFFAAAAAA);
            drawCenteredString(m, textRenderer, "or press Right Control", px + PANEL_W / 2, TOP + 74, 0xFF666677);
        } else if (showTheme) {
            renderThemes(m, mx, my, px, accent);
        } else {
            renderModules(m, mx, my, px, accent);
        }

        // Footer
        drawString(m, textRenderer, "RShift — close  ·  RCtrl — Alts", px + 8, TOP + sideH - 14, 0xFF444455);

        super.render(m, mx, my, delta);
    }

    private void drawCategory(MatrixStack m, String name, int y, boolean sel, boolean hov, int accent) {
        int sx = TOP;
        if (sel) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x335B8CFF);
            fill(m, sx, y + 4, sx + 3, y + CAT_H - 4, accent);
        } else if (hov) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x18FFFFFF);
        }
        int color = sel ? 0xFFFFFFFF : (hov ? 0xFFCCCCDD : 0xFF888899);
        drawCenteredString(m, textRenderer, name, sx + SIDEBAR_W / 2, y + 9, color);
    }

    private void renderModules(MatrixStack m, int mx, int my, int px, int accent) {
        List<Module> mods = getFilteredModules();
        int maxVisible = Math.max(1, (this.height - TOP * 2 - 30) / ROW_H);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, mods.size() - maxVisible)));

        int modY = TOP + 28;
        for (int i = scroll; i < Math.min(mods.size(), scroll + maxVisible); i++) {
            Module mod = mods.get(i);
            boolean hov = hovered(mx, my, px, modY, px + PANEL_W, modY + ROW_H);

            if (hov) fill(m, px + 2, modY, px + PANEL_W - 2, modY + ROW_H, 0x15FFFFFF);

            // Separator line
            fill(m, px + 10, modY, px + PANEL_W - 10, modY + 1, 0x18FFFFFF);

            // Name + description
            int nameColor = mod.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA;
            drawString(m, textRenderer, mod.getName(), px + 12, modY + 8, nameColor);

            String desc = mod.getDescription();
            if (desc.length() > 32) desc = desc.substring(0, 30) + "..";
            drawString(m, textRenderer, desc, px + 12, modY + 20, 0xFF555566);

            // Toggle switch
            drawToggle(m, px + PANEL_W - 40, modY + ROW_H / 2 - 6, mod.isEnabled(), accent);

            modY += ROW_H;
        }

        // Scrollbar
        if (mods.size() > maxVisible) {
            int sbH = this.height - TOP * 2 - 30;
            int thumbH = Math.max(16, sbH * maxVisible / mods.size());
            int thumbY = TOP + 26 + (sbH - thumbH) * scroll / Math.max(1, mods.size() - maxVisible);
            fill(m, px + PANEL_W - 5, TOP + 26, px + PANEL_W - 3, TOP + 26 + sbH, 0x22FFFFFF);
            fill(m, px + PANEL_W - 5, thumbY, px + PANEL_W - 3, thumbY + thumbH, accent);
        }
    }

    private void renderThemes(MatrixStack m, int mx, int my, int px, int accent) {
        ThemeManager.Theme[] themes = ThemeManager.Theme.values();
        int cols = 2;
        int cardW = (PANEL_W - 24) / cols;
        int cardH = 48;
        int startX = px + 10;
        int startY = TOP + 32;

        for (int i = 0; i < themes.length; i++) {
            ThemeManager.Theme t = themes[i];
            int col = i % cols;
            int row = i / cols;
            int x = startX + col * cardW;
            int y = startY + row * cardH;

            boolean sel = ThemeManager.getCurrent() == t;
            boolean hov = hovered(mx, my, x, y, x + cardW - 6, y + cardH - 6);

            fill(m, x, y, x + cardW - 6, y + cardH - 6, sel ? 0x33FFFFFF : (hov ? 0x1AFFFFFF : 0x0DFFFFFF));
            if (sel) drawBorder(m, x, y, x + cardW - 6, y + cardH - 6, t.colorPrimary);

            drawString(m, textRenderer, t.name, x + 6, y + 6, sel ? 0xFFFFFFFF : 0xFFCCCCCC);

            // Color preview bar
            for (int p = 0; p < cardW - 18; p++) {
                float tt = (float) p / (cardW - 18);
                int c = ThemeManager.lerpColor(t.colorPrimary, t.colorSecondary, tt);
                fill(m, x + 6 + p, y + 22, x + 7 + p, y + 30, c);
            }
        }
    }

    private void drawToggle(MatrixStack m, int x, int y, boolean on, int accent) {
        fill(m, x, y, x + 28, y + 12, on ? accent : 0xFF2A2A3A);
        int knobX = on ? x + 16 : x + 2;
        fill(m, knobX, y + 1, knobX + 10, y + 11, 0xFFFFFFFF);
    }

    private void drawBorder(MatrixStack m, int x1, int y1, int x2, int y2, int c) {
        fill(m, x1, y1, x2, y1 + 1, c);
        fill(m, x1, y2 - 1, x2, y2, c);
        fill(m, x1, y1, x1 + 1, y2, c);
        fill(m, x2 - 1, y1, x2, y2, c);
    }

    private boolean hovered(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    private List<Module> getFilteredModules() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return new ArrayList<>();

        if (!search.isEmpty()) {
            String q = search.toLowerCase();
            List<Module> res = new ArrayList<>();
            for (Module mod : mm.getModules()) {
                if (mod.getName().toLowerCase().contains(q) ||
                    mod.getDescription().toLowerCase().contains(q)) {
                    res.add(mod);
                }
            }
            return res;
        }
        return mm.getByCategory(selected);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;
        int sy = TOP + 46;

        // Search focus
        searchFocused = hovered((int) mx, (int) my, sx + 4, sy, sx + SIDEBAR_W - 4, sy + 16);

        // Categories
        int cy = sy + 22;
        for (Module.Category cat : Module.Category.values()) {
            if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
                selected = cat;
                showTheme = false;
                showAlts = false;
                scroll = 0;
                return true;
            }
            cy += CAT_H;
        }

        // Theme
        if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
            showTheme = true;
            showAlts = false;
            return true;
        }
        cy += CAT_H;

        // Alts → open full screen
        if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
            this.client.setScreen(new AltManagerScreen(this));
            return true;
        }

        if (showTheme) {
            ThemeManager.Theme[] themes = ThemeManager.Theme.values();
            int cols = 2;
            int cardW = (PANEL_W - 24) / cols;
            int cardH = 48;
            int startX = px + 10;
            int startY = TOP + 32;
            for (int i = 0; i < themes.length; i++) {
                int col = i % cols;
                int row = i / cols;
                int x = startX + col * cardW;
                int y = startY + row * cardH;
                if (hovered((int) mx, (int) my, x, y, x + cardW - 6, y + cardH - 6)) {
                    ThemeManager.setCurrent(themes[i]);
                    return true;
                }
            }
        } else if (!showAlts) {
            List<Module> mods = getFilteredModules();
            int maxVisible = Math.max(1, (this.height - TOP * 2 - 30) / ROW_H);
            int modY = TOP + 28;
            for (int i = scroll; i < Math.min(mods.size(), scroll + maxVisible); i++) {
                if (hovered((int) mx, (int) my, px, modY, px + PANEL_W, modY + ROW_H)) {
                    mods.get(i).toggle();
                    return true;
                }
                modY += ROW_H;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        scroll -= (int) amount;
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (searchFocused) {
            if (key == 259 && !search.isEmpty()) { // backspace
                search = search.substring(0, search.length() - 1);
                return true;
            }
            if (key == 256) { // esc
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(key, scan, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && chr >= 32 && search.length() < 24) {
            search += chr;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
