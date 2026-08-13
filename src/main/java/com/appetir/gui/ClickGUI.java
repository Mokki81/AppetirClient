package com.appetir.gui;

import com.appetir.AppetirClient;
import com.appetir.config.ConfigManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import com.appetir.settings.Setting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Premium ClickGUI — glass panels, glow accents, expandable settings.
 */
public class ClickGUI extends Screen {

    private static final int SIDEBAR_W = 132;
    private static final int PANEL_W   = 300;
    private static final int ROW_H     = 38;
    private static final int SET_H     = 17;
    private static final int CAT_H     = 27;
    private static final int TOP       = 14;
    private static final int GAP       = 8;

    private Module.Category selected = Module.Category.COMBAT;
    private boolean showTheme = false;
    private String search = "";
    private boolean searchFocused = false;
    private int scroll = 0;
    private final Set<String> expanded = new HashSet<>();

    public ClickGUI() {
        super(new LiteralText("Appetir"));
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        int H = this.height;
        int accent = ThemeManager.getAccentColor();
        int accentSoft = ThemeManager.getAccentColor(0.25f);
        int accentLine = ThemeManager.getAccentColor(0.55f);
        int sideH = H - TOP * 2;

        // Deep dim
        fill(m, 0, 0, this.width, H, 0xD0000008);

        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;

        // ════════ SIDEBAR ════════
        // outer glow
        fill(m, sx - 1, TOP - 1, sx + SIDEBAR_W + 1, TOP + sideH + 1, accentSoft);
        // body
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + sideH, 0xF50A0A14);
        // top accent line
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + 2, accentLine);

        // Brand block
        fill(m, sx, TOP + 2, sx + SIDEBAR_W, TOP + 44, 0xF0080812);
        drawCenteredString(m, textRenderer, AppetirClient.NAME, sx + SIDEBAR_W / 2, TOP + 12, accent);
        drawCenteredString(m, textRenderer, "v" + AppetirClient.VERSION, sx + SIDEBAR_W / 2, TOP + 24, 0xFF6A6A7A);
        // brand underline
        fill(m, sx + 20, TOP + 38, sx + SIDEBAR_W - 20, TOP + 39, accentSoft);

        // Search
        int sy = TOP + 50;
        boolean searchHov = hovered(mx, my, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16);
        fill(m, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16, searchFocused ? 0xFF141428 : 0xFF0C0C18);
        drawBorder(m, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16, searchFocused ? accent : (searchHov ? 0xFF3A3A50 : 0xFF222233));
        String sd = search.isEmpty() ? (searchFocused ? "|" : "Search modules") : search + (searchFocused ? "|" : "");
        drawString(m, textRenderer, sd, sx + 10, sy + 4, search.isEmpty() ? 0xFF4A4A5A : 0xFFE8E8F0);

        // Categories
        int cy = sy + 24;
        for (Module.Category cat : Module.Category.values()) {
            boolean sel = cat == selected && !showTheme;
            boolean hov = !showTheme && hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H);
            drawCat(m, cat.displayName, cy, sel, hov, accent);
            cy += CAT_H;
        }
        drawCat(m, "Themes", cy, showTheme, hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H), accent);
        cy += CAT_H;
        drawCat(m, "Alts", cy, false, hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H), accent);

        // Sidebar footer
        drawCenteredString(m, textRenderer, "RShift", sx + SIDEBAR_W / 2, TOP + sideH - 14, 0xFF3A3A4A);

        // ════════ MAIN PANEL ════════
        fill(m, px - 1, TOP - 1, px + PANEL_W + 1, TOP + sideH + 1, accentSoft);
        fill(m, px, TOP, px + PANEL_W, TOP + sideH, 0xF50C0C18);
        fill(m, px, TOP, px + PANEL_W, TOP + 2, accentLine);

        // Header bar
        fill(m, px, TOP + 2, px + PANEL_W, TOP + 24, 0xF00A0A14);
        String header = showTheme ? "Themes" : (search.isEmpty() ? selected.displayName : "Search results");
        drawString(m, textRenderer, header, px + 12, TOP + 9, accent);
        if (!showTheme) {
            drawString(m, textRenderer, "RMB settings", px + PANEL_W - 78, TOP + 9, 0xFF4A4A5A);
        }

        if (showTheme) renderThemes(m, mx, my, px, accent);
        else renderModules(m, mx, my, px, accent);

        super.render(m, mx, my, delta);
    }

    private void drawCat(MatrixStack m, String name, int y, boolean sel, boolean hov, int accent) {
        int sx = TOP;
        if (sel) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x285B8CFF);
            fill(m, sx, y + 4, sx + 3, y + CAT_H - 4, accent);
            // soft right fade
            fill(m, sx + SIDEBAR_W - 8, y, sx + SIDEBAR_W, y + CAT_H, 0x155B8CFF);
        } else if (hov) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x14FFFFFF);
        }
        int col = sel ? 0xFFFFFFFF : (hov ? 0xFFD0D0E0 : 0xFF7A7A8A);
        drawCenteredString(m, textRenderer, name, sx + SIDEBAR_W / 2, y + 9, col);
    }

    private void renderModules(MatrixStack m, int mx, int my, int px, int accent) {
        List<Module> mods = getFilteredModules();
        int contentH = this.height - TOP * 2 - 36;
        int y = TOP + 28;
        int skipped = 0;

        for (Module mod : mods) {
            if (skipped < scroll) { skipped++; continue; }
            if (y + 18 > TOP + 28 + contentH) break;

            boolean open = expanded.contains(mod.getName());
            boolean hov = hovered(mx, my, px + 4, y, px + PANEL_W - 4, y + ROW_H);

            // row background
            if (mod.isEnabled()) {
                fill(m, px + 4, y, px + PANEL_W - 4, y + ROW_H, 0x18FFFFFF);
            } else if (hov) {
                fill(m, px + 4, y, px + PANEL_W - 4, y + ROW_H, 0x10FFFFFF);
            }

            // left enable indicator
            if (mod.isEnabled()) {
                fill(m, px + 4, y + 6, px + 6, y + ROW_H - 6, accent);
            }

            // name
            String arrow = mod.hasSettings() ? (open ? "  ·" : "  ·") : "";
            int nameCol = mod.isEnabled() ? 0xFFFFFFFF : 0xFF9A9AAA;
            drawString(m, textRenderer, mod.getName() + arrow, px + 14, y + 7, nameCol);

            // desc
            String desc = mod.getDescription();
            if (desc.length() > 30) desc = desc.substring(0, 28) + "..";
            drawString(m, textRenderer, desc, px + 14, y + 20, 0xFF4A4A5A);

            // settings badge
            if (mod.hasSettings()) {
                String badge = open ? "−" : "+";
                drawString(m, textRenderer, badge, px + PANEL_W - 52, y + 12, open ? accent : 0xFF5A5A6A);
            }

            // toggle
            drawToggle(m, px + PANEL_W - 40, y + 13, mod.isEnabled(), accent);

            y += ROW_H;

            // expanded settings
            if (open) {
                fill(m, px + 10, y - 2, px + PANEL_W - 10, y - 1, 0x22FFFFFF);
                for (Setting s : mod.getSettings()) {
                    if (y + SET_H > TOP + 28 + contentH) break;
                    boolean shov = hovered(mx, my, px + 12, y, px + PANEL_W - 8, y + SET_H);
                    if (shov) fill(m, px + 12, y, px + PANEL_W - 8, y + SET_H, 0x12FFFFFF);

                    drawString(m, textRenderer, s.getName(), px + 20, y + 4, 0xFFA0A0B0);

                    // value with type hint
                    String val = s.getDisplayValue();
                    if (s instanceof NumberSetting) {
                        // mini bar
                        NumberSetting ns = (NumberSetting) s;
                        float pct = (float) ((ns.get() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                        int barX = px + PANEL_W - 90;
                        int barW = 50;
                        fill(m, barX, y + 6, barX + barW, y + 10, 0xFF1A1A28);
                        fill(m, barX, y + 6, barX + (int) (barW * pct), y + 10, accent);
                        drawString(m, textRenderer, val, barX + barW + 4, y + 4, accent);
                    } else if (s instanceof BooleanSetting) {
                        boolean on = ((BooleanSetting) s).get();
                        drawString(m, textRenderer, val, px + PANEL_W - 14 - textRenderer.getWidth(val), y + 4,
                                on ? 0xFF55FF88 : 0xFFFF6B6B);
                    } else {
                        drawString(m, textRenderer, val, px + PANEL_W - 14 - textRenderer.getWidth(val), y + 4, accent);
                    }
                    y += SET_H;
                }
                y += 6;
            }
        }

        if (mods.isEmpty()) {
            drawCenteredString(m, textRenderer, "No modules found", px + PANEL_W / 2, TOP + 80, 0xFF4A4A5A);
        }
    }

    private void renderThemes(MatrixStack m, int mx, int my, int px, int accent) {
        ThemeManager.Theme[] themes = ThemeManager.Theme.values();
        int cols = 2;
        int cardW = (PANEL_W - 24) / cols;
        int cardH = 50;
        int startX = px + 10;
        int startY = TOP + 32;

        for (int i = 0; i < themes.length; i++) {
            ThemeManager.Theme t = themes[i];
            int x = startX + (i % cols) * cardW;
            int y = startY + (i / cols) * cardH;
            boolean sel = ThemeManager.getCurrent() == t;
            boolean hov = hovered(mx, my, x, y, x + cardW - 6, y + cardH - 6);

            // card glow when selected
            if (sel) {
                fill(m, x - 1, y - 1, x + cardW - 5, y + cardH - 5, ThemeManager.withAlpha(t.colorPrimary, 0.35f));
            }
            fill(m, x, y, x + cardW - 6, y + cardH - 6, sel ? 0x28FFFFFF : (hov ? 0x18FFFFFF : 0x0CFFFFFF));
            if (sel) drawBorder(m, x, y, x + cardW - 6, y + cardH - 6, t.colorPrimary);

            drawString(m, textRenderer, t.name, x + 8, y + 8, sel ? 0xFFFFFFFF : 0xFFC0C0D0);

            // gradient preview strip
            int stripY = y + 24;
            for (int p = 0; p < cardW - 22; p++) {
                float tt = (float) p / (cardW - 22);
                int c = ThemeManager.lerpColor(t.colorPrimary, t.colorSecondary, tt);
                fill(m, x + 8 + p, stripY, x + 9 + p, stripY + 10, c);
            }
        }
    }

    private void drawToggle(MatrixStack m, int x, int y, boolean on, int accent) {
        // track
        fill(m, x, y, x + 28, y + 12, on ? ThemeManager.withAlpha(accent, 0.85f) : 0xFF252535);
        // soft edge
        if (on) fill(m, x, y, x + 28, y + 1, ThemeManager.withAlpha(0xFFFFFFFF, 0.15f));
        // knob
        int kx = on ? x + 16 : x + 2;
        fill(m, kx, y + 1, kx + 10, y + 11, 0xFFFFFFFF);
        // knob shine
        fill(m, kx + 1, y + 2, kx + 5, y + 5, 0x33FFFFFF);
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
                if (mod.getName().toLowerCase().contains(q) || mod.getDescription().toLowerCase().contains(q))
                    res.add(mod);
            }
            return res;
        }
        return mm.getByCategory(selected);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;
        int sy = TOP + 50;

        searchFocused = hovered((int) mx, (int) my, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16);

        int cy = sy + 24;
        for (Module.Category cat : Module.Category.values()) {
            if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
                selected = cat; showTheme = false; scroll = 0; return true;
            }
            cy += CAT_H;
        }
        if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
            showTheme = true; return true;
        }
        cy += CAT_H;
        if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
            this.client.setScreen(new AltManagerScreen(this));
            return true;
        }

        if (showTheme) {
            ThemeManager.Theme[] themes = ThemeManager.Theme.values();
            int cols = 2, cardW = (PANEL_W - 24) / cols, cardH = 50;
            int startX = px + 10, startY = TOP + 32;
            for (int i = 0; i < themes.length; i++) {
                int x = startX + (i % cols) * cardW;
                int y = startY + (i / cols) * cardH;
                if (hovered((int) mx, (int) my, x, y, x + cardW - 6, y + cardH - 6)) {
                    ThemeManager.setCurrent(themes[i]);
                    return true;
                }
            }
            return true;
        }

        List<Module> mods = getFilteredModules();
        int contentH = this.height - TOP * 2 - 36;
        int y = TOP + 28;
        int skipped = 0;

        for (Module mod : mods) {
            if (skipped < scroll) { skipped++; continue; }
            if (y + 18 > TOP + 28 + contentH) break;

            if (hovered((int) mx, (int) my, px + 4, y, px + PANEL_W - 4, y + ROW_H)) {
                if (button == 1 && mod.hasSettings()) {
                    if (expanded.contains(mod.getName())) expanded.remove(mod.getName());
                    else expanded.add(mod.getName());
                } else if (button == 0) {
                    mod.toggle();
                }
                return true;
            }
            y += ROW_H;

            if (expanded.contains(mod.getName())) {
                for (Setting s : mod.getSettings()) {
                    if (y + SET_H > TOP + 28 + contentH) break;
                    if (hovered((int) mx, (int) my, px + 12, y, px + PANEL_W - 8, y + SET_H) && button == 0) {
                        if (s instanceof BooleanSetting) ((BooleanSetting) s).toggle();
                        else if (s instanceof ModeSetting) ((ModeSetting) s).cycle();
                        else if (s instanceof NumberSetting) {
                            NumberSetting ns = (NumberSetting) s;
                            if (mx > px + PANEL_W - 90) ns.increment();
                            else ns.decrement();
                        }
                        ConfigManager cm = ConfigManager.getInstance();
                        if (cm != null) cm.saveQuiet();
                        return true;
                    }
                    y += SET_H;
                }
                y += 6;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        scroll = Math.max(0, scroll - (int) amount);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (searchFocused) {
            if (key == 259 && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                return true;
            }
            if (key == 256) { searchFocused = false; return true; }
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
    public boolean shouldPause() { return false; }
}
