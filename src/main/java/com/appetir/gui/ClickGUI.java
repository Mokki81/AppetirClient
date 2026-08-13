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
 * Modern ClickGUI with expandable module settings.
 * LMB — toggle module / change setting
 * RMB — expand/collapse settings
 */
public class ClickGUI extends Screen {

    private static final int SIDEBAR_W = 128;
    private static final int PANEL_W   = 280;
    private static final int ROW_H     = 36;
    private static final int SET_H     = 18;
    private static final int CAT_H     = 26;
    private static final int TOP       = 10;
    private static final int GAP       = 6;

    private Module.Category selected = Module.Category.COMBAT;
    private boolean showTheme = false;
    private String search = "";
    private boolean searchFocused = false;
    private int scroll = 0;
    private final Set<String> expanded = new HashSet<>();

    public ClickGUI() {
        super(new LiteralText("Appetir ClickGUI"));
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        int H = this.height;
        int accent = ThemeManager.getAccentColor();
        int dim = ThemeManager.getAccentColor(0.35f);
        int sideH = H - TOP * 2;

        fill(m, 0, 0, this.width, H, 0xCC00000A);

        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;

        // Sidebar
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + sideH, 0xF0080814);
        drawBorder(m, sx, TOP, sx + SIDEBAR_W, TOP + sideH, dim);

        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + 36, 0xF0060610);
        drawCenteredString(m, textRenderer, AppetirClient.NAME, sx + SIDEBAR_W / 2, TOP + 6, accent);
        drawCenteredString(m, textRenderer, "v" + AppetirClient.VERSION, sx + SIDEBAR_W / 2, TOP + 18, 0xFF888899);

        int sy = TOP + 42;
        fill(m, sx + 4, sy, sx + SIDEBAR_W - 4, sy + 14, searchFocused ? 0xFF16162A : 0xFF0E0E18);
        drawBorder(m, sx + 4, sy, sx + SIDEBAR_W - 4, sy + 14, searchFocused ? accent : 0xFF2A2A3A);
        String sd = search.isEmpty() ? (searchFocused ? "|" : "Search...") : search + (searchFocused ? "|" : "");
        drawString(m, textRenderer, sd, sx + 7, sy + 3, search.isEmpty() ? 0xFF555566 : 0xFFE0E0E0);

        int cy = sy + 20;
        for (Module.Category cat : Module.Category.values()) {
            boolean sel = cat == selected && !showTheme;
            boolean hov = !showTheme && hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H);
            drawCategory(m, cat.displayName, cy, sel, hov, accent);
            cy += CAT_H;
        }
        drawCategory(m, "Theme", cy, showTheme, hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H), accent);
        cy += CAT_H;
        drawCategory(m, "Alts", cy, false, hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H), accent);

        // Content
        fill(m, px, TOP, px + PANEL_W, TOP + sideH, 0xF010101A);
        drawBorder(m, px, TOP, px + PANEL_W, TOP + sideH, dim);
        fill(m, px, TOP, px + PANEL_W, TOP + 20, 0xF00C0C16);

        String header = showTheme ? "Themes" : (search.isEmpty() ? selected.displayName : "Search");
        drawString(m, textRenderer, header, px + 8, TOP + 6, accent);
        drawString(m, textRenderer, "RMB — settings", px + PANEL_W - 80, TOP + 6, 0xFF555566);

        if (showTheme) renderThemes(m, mx, my, px, accent);
        else renderModules(m, mx, my, px, accent);

        drawString(m, textRenderer, "RShift close · RCtrl Alts", px + 8, TOP + sideH - 12, 0xFF444455);
        super.render(m, mx, my, delta);
    }

    private void drawCategory(MatrixStack m, String name, int y, boolean sel, boolean hov, int accent) {
        int sx = TOP;
        if (sel) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x335B8CFF);
            fill(m, sx, y + 3, sx + 3, y + CAT_H - 3, accent);
        } else if (hov) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x18FFFFFF);
        }
        drawCenteredString(m, textRenderer, name, sx + SIDEBAR_W / 2, y + 8,
                sel ? 0xFFFFFFFF : (hov ? 0xFFCCCCDD : 0xFF888899));
    }

    private void renderModules(MatrixStack m, int mx, int my, int px, int accent) {
        List<Module> mods = getFilteredModules();
        int contentH = this.height - TOP * 2 - 32;
        int y = TOP + 24;
        int drawn = 0;
        int skipped = 0;

        for (Module mod : mods) {
            int blockH = ROW_H + (expanded.contains(mod.getName()) ? mod.getSettings().size() * SET_H + 4 : 0);
            if (skipped < scroll) {
                skipped++;
                continue;
            }
            if (y + 20 > TOP + 24 + contentH) break;

            boolean hov = hovered(mx, my, px, y, px + PANEL_W, y + ROW_H);
            if (hov) fill(m, px + 2, y, px + PANEL_W - 2, y + ROW_H, 0x15FFFFFF);

            fill(m, px + 8, y, px + PANEL_W - 8, y + 1, 0x15FFFFFF);

            int nameColor = mod.isEnabled() ? 0xFFFFFFFF : 0xFF999999;
            String name = mod.getName() + (mod.hasSettings() ? (expanded.contains(mod.getName()) ? " ▾" : " ▸") : "");
            drawString(m, textRenderer, name, px + 10, y + 6, nameColor);

            String desc = mod.getDescription();
            if (desc.length() > 28) desc = desc.substring(0, 26) + "..";
            drawString(m, textRenderer, desc, px + 10, y + 18, 0xFF555566);

            drawToggle(m, px + PANEL_W - 38, y + 12, mod.isEnabled(), accent);

            y += ROW_H;

            // Settings
            if (expanded.contains(mod.getName())) {
                for (Setting s : mod.getSettings()) {
                    if (y + SET_H > TOP + 24 + contentH) break;
                    boolean shov = hovered(mx, my, px + 12, y, px + PANEL_W - 8, y + SET_H);
                    if (shov) fill(m, px + 12, y, px + PANEL_W - 8, y + SET_H, 0x12FFFFFF);

                    drawString(m, textRenderer, s.getName(), px + 18, y + 5, 0xFFBBBBCC);
                    String val = s.getDisplayValue();
                    int vw = textRenderer.getWidth(val);
                    drawString(m, textRenderer, val, px + PANEL_W - 14 - vw, y + 5, accent);
                    y += SET_H;
                }
                y += 4;
            }
            drawn++;
        }
    }

    private void renderThemes(MatrixStack m, int mx, int my, int px, int accent) {
        ThemeManager.Theme[] themes = ThemeManager.Theme.values();
        int cols = 2;
        int cardW = (PANEL_W - 20) / cols;
        int cardH = 44;
        int startX = px + 8;
        int startY = TOP + 28;

        for (int i = 0; i < themes.length; i++) {
            ThemeManager.Theme t = themes[i];
            int x = startX + (i % cols) * cardW;
            int y = startY + (i / cols) * cardH;
            boolean sel = ThemeManager.getCurrent() == t;
            boolean hov = hovered(mx, my, x, y, x + cardW - 4, y + cardH - 4);

            fill(m, x, y, x + cardW - 4, y + cardH - 4, sel ? 0x33FFFFFF : (hov ? 0x1AFFFFFF : 0x0DFFFFFF));
            if (sel) drawBorder(m, x, y, x + cardW - 4, y + cardH - 4, t.colorPrimary);
            drawString(m, textRenderer, t.name, x + 5, y + 5, sel ? 0xFFFFFFFF : 0xFFCCCCCC);
            for (int p = 0; p < cardW - 14; p++) {
                float tt = (float) p / (cardW - 14);
                fill(m, x + 5 + p, y + 20, x + 6 + p, y + 28, ThemeManager.lerpColor(t.colorPrimary, t.colorSecondary, tt));
            }
        }
    }

    private void drawToggle(MatrixStack m, int x, int y, boolean on, int accent) {
        fill(m, x, y, x + 26, y + 11, on ? accent : 0xFF2A2A3A);
        int knobX = on ? x + 15 : x + 1;
        fill(m, knobX, y + 1, knobX + 10, y + 10, 0xFFFFFFFF);
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
        int sy = TOP + 42;

        searchFocused = hovered((int) mx, (int) my, sx + 4, sy, sx + SIDEBAR_W - 4, sy + 14);

        int cy = sy + 20;
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
            int cols = 2, cardW = (PANEL_W - 20) / cols, cardH = 44;
            int startX = px + 8, startY = TOP + 28;
            for (int i = 0; i < themes.length; i++) {
                int x = startX + (i % cols) * cardW;
                int y = startY + (i / cols) * cardH;
                if (hovered((int) mx, (int) my, x, y, x + cardW - 4, y + cardH - 4)) {
                    ThemeManager.setCurrent(themes[i]);
                    return true;
                }
            }
            return true;
        }

        // Modules + settings
        List<Module> mods = getFilteredModules();
        int contentH = this.height - TOP * 2 - 32;
        int y = TOP + 24;
        int skipped = 0;

        for (Module mod : mods) {
            if (skipped < scroll) { skipped++; continue; }
            if (y + 20 > TOP + 24 + contentH) break;

            if (hovered((int) mx, (int) my, px, y, px + PANEL_W, y + ROW_H)) {
                if (button == 1 && mod.hasSettings()) { // RMB expand
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
                    if (y + SET_H > TOP + 24 + contentH) break;
                    if (hovered((int) mx, (int) my, px + 12, y, px + PANEL_W - 8, y + SET_H) && button == 0) {
                        if (s instanceof BooleanSetting) {
                            ((BooleanSetting) s).toggle();
                        } else if (s instanceof ModeSetting) {
                            ((ModeSetting) s).cycle();
                        } else if (s instanceof NumberSetting) {
                            NumberSetting ns = (NumberSetting) s;
                            if (mx > px + PANEL_W / 2) ns.increment();
                            else ns.decrement();
                        }
                        ConfigManager cm = ConfigManager.getInstance();
                        if (cm != null) cm.saveQuiet();
                        return true;
                    }
                    y += SET_H;
                }
                y += 4;
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
