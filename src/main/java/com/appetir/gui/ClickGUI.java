package com.appetir.gui;

import com.appetir.client.ClientMode;
import com.appetir.config.ConfigManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import com.appetir.settings.Setting;
import com.appetir.util.BindManager;
import com.appetir.util.KeyUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGUI extends Screen {

    private static final int SIDEBAR_W = 132;
    private static final int PANEL_W   = 300;
    private static final int ROW_H     = 38;
    private static final int SET_H     = 17;
    private static final int CAT_H     = 27;
    private static final int TOP       = 14;
    private static final int GAP       = 8;

    private Module.Category selected = Module.Category.RENDER;
    private boolean showTheme = false;
    private String search = "";
    private boolean searchFocused = false;
    private int scroll = 0;
    private final Set<String> expanded = new HashSet<>();

    public ClickGUI() {
        super(new LiteralText("Appetir"));
        if (ClientMode.isFull()) selected = Module.Category.COMBAT;
        else selected = Module.Category.RENDER;
    }

    private int contentHeight() {
        return this.height - TOP * 2 - 36;
    }

    /** Total pixel height of the module list including expanded settings. */
    private int measureContentPixels(List<Module> mods) {
        int h = 0;
        for (Module mod : mods) {
            h += ROW_H;
            if (expanded.contains(mod.getName())) {
                h += SET_H; // Bind row
                h += mod.getSettings().size() * SET_H;
                h += 6;
            }
        }
        return h;
    }

    /**
     * Max scroll offset in "module index" units, accounting for expanded rows
     * and viewport height so the list never empties.
     */
    private int getMaxScroll() {
        List<Module> mods = getFilteredModules();
        if (mods.isEmpty()) return 0;

        int viewport = contentHeight();
        int total = measureContentPixels(mods);
        if (total <= viewport) return 0;

        // Approximate: how many leading module rows we can skip before
        // remaining content still fills the viewport.
        int max = 0;
        for (int skip = 0; skip < mods.size(); skip++) {
            int remaining = 0;
            for (int i = skip; i < mods.size(); i++) {
                Module mod = mods.get(i);
                remaining += ROW_H;
                if (expanded.contains(mod.getName())) {
                    remaining += SET_H + mod.getSettings().size() * SET_H + 6;
                }
            }
            if (remaining <= viewport) {
                max = Math.max(0, skip);
                break;
            }
            max = skip;
        }
        return max;
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, getMaxScroll()));
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        clampScroll();

        int H = this.height;
        int accent = ThemeManager.getAccentColor();
        int accentSoft = ThemeManager.getAccentColor(0.25f);
        int accentLine = ThemeManager.getAccentColor(0.55f);
        int sideH = H - TOP * 2;

        fill(m, 0, 0, this.width, H, 0xD0000008);

        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;

        fill(m, sx - 1, TOP - 1, sx + SIDEBAR_W + 1, TOP + sideH + 1, accentSoft);
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + sideH, 0xF50A0A14);
        fill(m, sx, TOP, sx + SIDEBAR_W, TOP + 2, accentLine);

        fill(m, sx, TOP + 2, sx + SIDEBAR_W, TOP + 44, 0xF0080812);
        drawCenteredString(m, textRenderer, ClientMode.brandName(), sx + SIDEBAR_W / 2, TOP + 12, accent);
        drawCenteredString(m, textRenderer, ClientMode.brandSubtitle(), sx + SIDEBAR_W / 2, TOP + 24, 0xFF6A6A7A);
        fill(m, sx + 20, TOP + 38, sx + SIDEBAR_W - 20, TOP + 39, accentSoft);

        int sy = TOP + 50;
        boolean searchHov = hovered(mx, my, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16);
        fill(m, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16, searchFocused ? 0xFF141428 : 0xFF0C0C18);
        drawBorder(m, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16, searchFocused ? accent : (searchHov ? 0xFF3A3A50 : 0xFF222233));
        String sd = search.isEmpty() ? (searchFocused ? "|" : "Search") : search + (searchFocused ? "|" : "");
        drawString(m, textRenderer, sd, sx + 10, sy + 4, search.isEmpty() ? 0xFF4A4A5A : 0xFFE8E8F0);

        int cy = sy + 24;
        for (Module.Category cat : Module.Category.values()) {
            if (!ClientMode.isCategoryVisible(cat)) continue;
            boolean sel = cat == selected && !showTheme;
            boolean hov = !showTheme && hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H);
            drawCat(m, cat.displayName, cy, sel, hov, accent);
            cy += CAT_H;
        }
        drawCat(m, "Themes", cy, showTheme, hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H), accent);
        cy += CAT_H;
        if (ClientMode.isFull()) {
            drawCat(m, "Alts", cy, false, hovered(mx, my, sx, cy, sx + SIDEBAR_W, cy + CAT_H), accent);
            cy += CAT_H;
        }

        int modeY = TOP + sideH - 36;
        boolean modeHov = hovered(mx, my, sx + 6, modeY, sx + SIDEBAR_W - 6, modeY + 18);
        fill(m, sx + 6, modeY, sx + SIDEBAR_W - 6, modeY + 18,
                ClientMode.isClean() ? 0xFF1A3A2A : 0xFF2A1A3A);
        if (modeHov) fill(m, sx + 6, modeY, sx + SIDEBAR_W - 6, modeY + 18, 0x22FFFFFF);
        String modeLabel = ClientMode.isClean() ? "Mode: Clean" : "Mode: Full";
        drawCenteredString(m, textRenderer, modeLabel, sx + SIDEBAR_W / 2, modeY + 5,
                ClientMode.isClean() ? 0xFF55FF88 : accent);

        drawCenteredString(m, textRenderer, "INS / RShift", sx + SIDEBAR_W / 2, TOP + sideH - 14, 0xFF3A3A4A);

        fill(m, px - 1, TOP - 1, px + PANEL_W + 1, TOP + sideH + 1, accentSoft);
        fill(m, px, TOP, px + PANEL_W, TOP + sideH, 0xF50C0C18);
        fill(m, px, TOP, px + PANEL_W, TOP + 2, accentLine);

        fill(m, px, TOP + 2, px + PANEL_W, TOP + 24, 0xF00A0A14);
        String header = showTheme ? "Themes" : (search.isEmpty() ? selected.displayName : "Search");
        drawString(m, textRenderer, header, px + 12, TOP + 9, accent);

        if (BindManager.isListening()) {
            drawString(m, textRenderer, "Press key... DEL=unbind ESC=cancel",
                    px + 12, TOP + sideH - 14, accent);
        } else if (!showTheme) {
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
        } else if (hov) {
            fill(m, sx, y, sx + SIDEBAR_W, y + CAT_H, 0x14FFFFFF);
        }
        int col = sel ? 0xFFFFFFFF : (hov ? 0xFFD0D0E0 : 0xFF7A7A8A);
        drawCenteredString(m, textRenderer, name, sx + SIDEBAR_W / 2, y + 9, col);
    }

    private void renderModules(MatrixStack m, int mx, int my, int px, int accent) {
        List<Module> mods = getFilteredModules();
        int contentH = contentHeight();
        int y = TOP + 28;
        int skipped = 0;

        for (Module mod : mods) {
            if (skipped < scroll) { skipped++; continue; }
            if (y + 18 > TOP + 28 + contentH) break;

            boolean open = expanded.contains(mod.getName());
            boolean hov = hovered(mx, my, px + 4, y, px + PANEL_W - 4, y + ROW_H);
            boolean listening = BindManager.isListening() && BindManager.getListening() == mod;

            if (mod.isEnabled()) fill(m, px + 4, y, px + PANEL_W - 4, y + ROW_H, 0x18FFFFFF);
            else if (hov) fill(m, px + 4, y, px + PANEL_W - 4, y + ROW_H, 0x10FFFFFF);
            if (mod.isEnabled()) fill(m, px + 4, y + 6, px + 6, y + ROW_H - 6, accent);

            drawString(m, textRenderer, mod.getName(), px + 14, y + 7, mod.isEnabled() ? 0xFFFFFFFF : 0xFF9A9AAA);

            String keyName = listening ? "..." : KeyUtil.getKeyName(mod.getKey());
            if (!keyName.equals("NONE") || listening) {
                int kw = textRenderer.getWidth(keyName);
                drawString(m, textRenderer, keyName, px + PANEL_W - 46 - kw, y + 7,
                        listening ? accent : 0xFF888899);
            }

            String desc = mod.getDescription();
            if (desc.length() > 28) desc = desc.substring(0, 26) + "..";
            drawString(m, textRenderer, desc, px + 14, y + 20, 0xFF4A4A5A);
            drawString(m, textRenderer, open ? "−" : "+", px + PANEL_W - 52, y + 12, open ? accent : 0xFF5A5A6A);
            drawToggle(m, px + PANEL_W - 40, y + 13, mod.isEnabled(), accent);

            y += ROW_H;

            if (open) {
                fill(m, px + 10, y - 2, px + PANEL_W - 10, y - 1, 0x22FFFFFF);

                if (y + SET_H <= TOP + 28 + contentH) {
                    boolean bhov = hovered(mx, my, px + 12, y, px + PANEL_W - 8, y + SET_H);
                    if (bhov || listening) fill(m, px + 12, y, px + PANEL_W - 8, y + SET_H,
                            listening ? ThemeManager.withAlpha(accent, 0.2f) : 0x12FFFFFF);
                    drawString(m, textRenderer, "Bind", px + 20, y + 4, 0xFFA0A0B0);
                    String bval = listening ? "Press key..." : KeyUtil.getKeyName(mod.getKey());
                    drawString(m, textRenderer, bval,
                            px + PANEL_W - 14 - textRenderer.getWidth(bval), y + 4,
                            listening ? accent : 0xFFCCCCDD);
                    y += SET_H;
                }

                for (Setting s : mod.getSettings()) {
                    if (y + SET_H > TOP + 28 + contentH) break;
                    boolean shov = hovered(mx, my, px + 12, y, px + PANEL_W - 8, y + SET_H);
                    if (shov) fill(m, px + 12, y, px + PANEL_W - 8, y + SET_H, 0x12FFFFFF);
                    drawString(m, textRenderer, s.getName(), px + 20, y + 4, 0xFFA0A0B0);
                    String val = s.getDisplayValue();
                    if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        float pct = (float) ((ns.get() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                        int barX = px + PANEL_W - 90;
                        fill(m, barX, y + 6, barX + 50, y + 10, 0xFF1A1A28);
                        fill(m, barX, y + 6, barX + (int) (50 * pct), y + 10, accent);
                        drawString(m, textRenderer, val, barX + 54, y + 4, accent);
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
            drawCenteredString(m, textRenderer,
                    ClientMode.isClean() ? "Clean mode — QOL only" : "No modules",
                    px + PANEL_W / 2, TOP + 80, 0xFF4A4A5A);
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
            if (sel) fill(m, x - 1, y - 1, x + cardW - 5, y + cardH - 5, ThemeManager.withAlpha(t.colorPrimary, 0.35f));
            fill(m, x, y, x + cardW - 6, y + cardH - 6, sel ? 0x28FFFFFF : (hov ? 0x18FFFFFF : 0x0CFFFFFF));
            if (sel) drawBorder(m, x, y, x + cardW - 6, y + cardH - 6, t.colorPrimary);
            drawString(m, textRenderer, t.name, x + 8, y + 8, sel ? 0xFFFFFFFF : 0xFFC0C0D0);
            int stripY = y + 24;
            for (int p = 0; p < cardW - 22; p++) {
                float tt = (float) p / (cardW - 22);
                fill(m, x + 8 + p, stripY, x + 9 + p, stripY + 10,
                        ThemeManager.lerpColor(t.colorPrimary, t.colorSecondary, tt));
            }
        }
    }

    private void drawToggle(MatrixStack m, int x, int y, boolean on, int accent) {
        fill(m, x, y, x + 28, y + 12, on ? ThemeManager.withAlpha(accent, 0.85f) : 0xFF252535);
        int kx = on ? x + 16 : x + 2;
        fill(m, kx, y + 1, kx + 10, y + 11, 0xFFFFFFFF);
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
        List<Module> base;
        if (!search.isEmpty()) {
            String q = search.toLowerCase();
            base = new ArrayList<>();
            for (Module mod : mm.getModules()) {
                if (mod.getName().toLowerCase().contains(q) || mod.getDescription().toLowerCase().contains(q))
                    base.add(mod);
            }
        } else {
            base = mm.getByCategory(selected);
        }
        List<Module> out = new ArrayList<>();
        for (Module mod : base) {
            if (ClientMode.isModuleAllowed(mod)) out.add(mod);
        }
        return out;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (BindManager.isListening()) return true;

        int sx = TOP;
        int px = sx + SIDEBAR_W + GAP;
        int sy = TOP + 50;
        int sideH = this.height - TOP * 2;

        int modeY = TOP + sideH - 36;
        if (hovered((int) mx, (int) my, sx + 6, modeY, sx + SIDEBAR_W - 6, modeY + 18) && button == 0) {
            ClientMode.toggle();
            if (ClientMode.isClean() && (selected == Module.Category.COMBAT || selected == Module.Category.MOVEMENT))
                selected = Module.Category.RENDER;
            expanded.clear();
            scroll = 0;
            return true;
        }

        searchFocused = hovered((int) mx, (int) my, sx + 6, sy, sx + SIDEBAR_W - 6, sy + 16);

        int cy = sy + 24;
        for (Module.Category cat : Module.Category.values()) {
            if (!ClientMode.isCategoryVisible(cat)) continue;
            if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
                selected = cat; showTheme = false; scroll = 0; return true;
            }
            cy += CAT_H;
        }
        if (hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
            showTheme = true; return true;
        }
        cy += CAT_H;
        if (ClientMode.isFull() && hovered((int) mx, (int) my, sx, cy, sx + SIDEBAR_W, cy + CAT_H)) {
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
        int contentH = contentHeight();
        int y = TOP + 28;
        int skipped = 0;

        for (Module mod : mods) {
            if (skipped < scroll) { skipped++; continue; }
            if (y + 18 > TOP + 28 + contentH) break;

            if (hovered((int) mx, (int) my, px + 4, y, px + PANEL_W - 4, y + ROW_H)) {
                if (button == 1) {
                    if (expanded.contains(mod.getName())) expanded.remove(mod.getName());
                    else expanded.add(mod.getName());
                    clampScroll();
                } else if (button == 0) {
                    mod.toggle();
                }
                return true;
            }
            y += ROW_H;

            if (expanded.contains(mod.getName())) {
                if (y + SET_H <= TOP + 28 + contentH) {
                    if (hovered((int) mx, (int) my, px + 12, y, px + PANEL_W - 8, y + SET_H) && button == 0) {
                        BindManager.startListening(mod);
                        return true;
                    }
                    y += SET_H;
                }
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
                        if (cm != null) cm.markDirty();
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
        scroll = scroll - (int) amount;
        clampScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (BindManager.isListening()) {
            BindManager.onKey(key);
            return true;
        }
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
        if (BindManager.isListening()) return true;
        if (searchFocused && chr >= 32 && search.length() < 24) {
            search += chr;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void onClose() {
        BindManager.cancel();
        super.onClose();
    }
}
