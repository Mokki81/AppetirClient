package com.appetir.gui;

import com.appetir.AppetirClient;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    private static final int SIDEBAR_X = 8, SIDEBAR_W = 130;
    private static final int PANEL_X = SIDEBAR_X + SIDEBAR_W + 6, PANEL_W = 240;
    private static final int ROW_H = 54, CAT_ROW_H = 34, TOP = 8;

    private Module.Category selectedCategory = Module.Category.COMBAT;
    private boolean showTheme = false;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int scrollOffset = 0;

    public ClickGUI() { super(new LiteralText("ClickGUI")); }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        int H = this.height;
        int accent = ThemeManager.getAccentColor();
        int dim = (accent & 0x00FFFFFF) | 0x66000000;
        int sideH = H - TOP * 2;

        fill(m, 0, 0, this.width, H, 0xBB000010);

        // Sidebar
        fill(m, SIDEBAR_X, TOP, SIDEBAR_X+SIDEBAR_W, TOP+sideH, 0xE0080812);
        box(m, SIDEBAR_X, TOP, SIDEBAR_X+SIDEBAR_W, TOP+sideH, dim);
        fill(m, SIDEBAR_X, TOP, SIDEBAR_X+SIDEBAR_W, TOP+44, 0xE0060610);
        int cx = SIDEBAR_X + SIDEBAR_W/2;
        drawCenteredString(m, textRenderer, AppetirClient.NAME, cx, TOP+10, accent);
        drawCenteredString(m, textRenderer, "v"+AppetirClient.VERSION, cx, TOP+24, 0xFFAAAAAA);

        // Search
        int sy = TOP+48;
        fill(m, SIDEBAR_X+4, sy, SIDEBAR_X+SIDEBAR_W-4, sy+18, searchFocused?0xFF1A1A2E:0xFF111120);
        box(m, SIDEBAR_X+4, sy, SIDEBAR_X+SIDEBAR_W-4, sy+18, searchFocused?accent:dim);
        String sd = searchQuery.isEmpty()?(searchFocused?"|":"Search..."):searchQuery+(searchFocused?"|":"");
        drawString(m, textRenderer, sd, SIDEBAR_X+8, sy+5, searchQuery.isEmpty()?0xFF555566:0xFFFFFFFF);

        // Categories
        int cy = sy+22;
        for (Module.Category cat : Module.Category.values()) {
            boolean sel = cat==selectedCategory && !showTheme;
            boolean hov = !showTheme && in(mx,my,SIDEBAR_X,cy,SIDEBAR_X+SIDEBAR_W,cy+CAT_ROW_H);
            catRow(m, cat.name(), cy, sel, hov, accent);
            cy += CAT_ROW_H;
        }
        catRow(m, "Theme", cy, showTheme, in(mx,my,SIDEBAR_X,cy,SIDEBAR_X+SIDEBAR_W,cy+CAT_ROW_H), accent);

        // Panel
        fill(m, PANEL_X, TOP, PANEL_X+PANEL_W, TOP+sideH, 0xE0101018);
        box(m, PANEL_X, TOP, PANEL_X+PANEL_W, TOP+sideH, dim);
        fill(m, PANEL_X, TOP, PANEL_X+PANEL_W, TOP+22, 0xE00C0C18);
        String lbl = showTheme?"Theme":(!searchQuery.isEmpty()?"Search":selectedCategory.name());
        drawString(m, textRenderer, "Category: ", PANEL_X+8, TOP+7, 0xFF888899);
        drawString(m, textRenderer, lbl, PANEL_X+8+textRenderer.getWidth("Category: "), TOP+7, accent);

        if (showTheme) themes(m, mx, my, accent);
        else modules(m, mx, my, accent);

        super.render(m, mx, my, delta);
    }

    private void catRow(MatrixStack m, String name, int y, boolean sel, boolean hov, int accent) {
        if (sel) fill(m, SIDEBAR_X,y,SIDEBAR_X+SIDEBAR_W,y+CAT_ROW_H, 0x335B8CFF);
        else if (hov) fill(m, SIDEBAR_X,y,SIDEBAR_X+SIDEBAR_W,y+CAT_ROW_H, 0x1AFFFFFF);
        if (sel) fill(m, SIDEBAR_X,y+3,SIDEBAR_X+3,y+CAT_ROW_H-3, accent);
        drawCenteredString(m, textRenderer, name, SIDEBAR_X+SIDEBAR_W/2, y+12,
                sel?0xFFFFFFFF:hov?0xFFCCCCDD:0xFF888899);
    }

    private void modules(MatrixStack m, int mx, int my, int accent) {
        List<Module> mods = getMods();
        int maxV = (this.height-TOP*2-22)/ROW_H;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0,mods.size()-maxV)));
        int modY = TOP+26;
        for (int i=scrollOffset; i<Math.min(mods.size(),scrollOffset+maxV); i++) {
            Module mod = mods.get(i);
            boolean hov = in(mx,my,PANEL_X,modY,PANEL_X+PANEL_W,modY+ROW_H);
            if (hov) fill(m, PANEL_X,modY,PANEL_X+PANEL_W,modY+ROW_H, 0x1AFFFFFF);
            fill(m, PANEL_X+8,modY,PANEL_X+PANEL_W-8,modY+1, 0x22FFFFFF);
            drawString(m, textRenderer, mod.getName(), PANEL_X+12, modY+10,
                    mod.isEnabled()?0xFFFFFFFF:0xFFAAAAAA);
            String desc = mod.getDescription().length()>30?mod.getDescription().substring(0,28)+"...":mod.getDescription();
            drawString(m, textRenderer, desc, PANEL_X+12, modY+22, 0xFF666677);
            toggle(m, PANEL_X+PANEL_W-36, modY+ROW_H/2-6, mod.isEnabled(), accent);
            modY += ROW_H;
        }
        if (mods.size()>maxV) {
            int sbH=(this.height-TOP*2-22), thH=Math.max(20,sbH*maxV/mods.size());
            int thY=TOP+22+(sbH-thH)*scrollOffset/Math.max(1,mods.size()-maxV);
            fill(m, PANEL_X+PANEL_W-4,TOP+22,PANEL_X+PANEL_W-2,TOP+22+sbH, 0x22FFFFFF);
            fill(m, PANEL_X+PANEL_W-4,thY,PANEL_X+PANEL_W-2,thY+thH, accent);
        }
    }

    private void themes(MatrixStack m, int mx, int my, int accent) {
        ThemeManager.Theme[] ts = ThemeManager.Theme.values();
        int cW=(PANEL_W-16)/3, cH=52, sx=PANEL_X+8, sy=TOP+30;
        for (int i=0; i<ts.length; i++) {
            int x=sx+(i%3)*cW, y=sy+(i/3)*cH;
            boolean sel=ThemeManager.getCurrent()==ts[i], hov=in(mx,my,x,y,x+cW-4,y+cH-4);
            fill(m, x,y,x+cW-4,y+cH-4, sel?0x33FFFFFF:hov?0x1AFFFFFF:0x11FFFFFF);
            if (sel) box(m, x,y,x+cW-4,y+cH-4, ts[i].colorPrimary);
            drawString(m, textRenderer, ts[i].name, x+4, y+6, sel?0xFFFFFFFF:0xFFAAAAAA);
            for (int px=0; px<cW-12; px++)
                fill(m, x+4+px,y+20,x+5+px,y+26, lerp(ts[i].colorPrimary,ts[i].colorSecondary,(float)px/(cW-12)));
        }
    }

    private void toggle(MatrixStack m, int x, int y, boolean on, int accent) {
        fill(m, x,y,x+26,y+13, on?accent:0xFF303040);
        int kx=on?x+14:x+1;
        fill(m, kx,y+1,kx+11,y+12, 0xFFFFFFFF);
    }

    private void box(MatrixStack m, int x1, int y1, int x2, int y2, int c) {
        fill(m,x1,y1,x2,y1+1,c); fill(m,x1,y2-1,x2,y2,c);
        fill(m,x1,y1,x1+1,y2,c); fill(m,x2-1,y1,x2,y2,c);
    }

    private boolean in(int mx,int my,int x1,int y1,int x2,int y2) {
        return mx>=x1&&mx<=x2&&my>=y1&&my<=y2;
    }

    private int lerp(int c1, int c2, float t) {
        int r=(int)(((c1>>16)&0xFF)*(1-t)+((c2>>16)&0xFF)*t);
        int g=(int)(((c1>>8)&0xFF)*(1-t)+((c2>>8)&0xFF)*t);
        int b=(int)(((c1)&0xFF)*(1-t)+((c2)&0xFF)*t);
        return 0xFF000000|(r<<16)|(g<<8)|b;
    }

    private List<Module> getMods() {
        ModuleManager mm=ModuleManager.getInstance();
        if (mm==null) return new ArrayList<>();
        if (!searchQuery.isEmpty()) {
            List<Module> res=new ArrayList<>();
            String q=searchQuery.toLowerCase();
            for (Module mod:mm.getModules())
                if (mod.getName().toLowerCase().contains(q)||mod.getDescription().toLowerCase().contains(q)) res.add(mod);
            return res;
        }
        return mm.getByCategory(selectedCategory);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int sy=TOP+48;
        searchFocused=in((int)mx,(int)my,SIDEBAR_X+4,sy,SIDEBAR_X+SIDEBAR_W-4,sy+18);
        int cy=sy+22;
        for (Module.Category cat:Module.Category.values()) {
            if (in((int)mx,(int)my,SIDEBAR_X,cy,SIDEBAR_X+SIDEBAR_W,cy+CAT_ROW_H)) {
                selectedCategory=cat; showTheme=false; scrollOffset=0; return true;
            }
            cy+=CAT_ROW_H;
        }
        if (in((int)mx,(int)my,SIDEBAR_X,cy,SIDEBAR_X+SIDEBAR_W,cy+CAT_ROW_H)) { showTheme=true; return true; }

        if (showTheme) {
            ThemeManager.Theme[] ts=ThemeManager.Theme.values();
            int cW=(PANEL_W-16)/3,cH=52,sx=PANEL_X+8,tsy=TOP+30;
            for (int i=0;i<ts.length;i++) {
                int x=sx+(i%3)*cW,y=tsy+(i/3)*cH;
                if (in((int)mx,(int)my,x,y,x+cW-4,y+cH-4)) { ThemeManager.setCurrent(ts[i]); return true; }
            }
        } else {
            List<Module> mods=getMods();
            int maxV=(this.height-TOP*2-22)/ROW_H, modY=TOP+26;
            for (int i=scrollOffset;i<Math.min(mods.size(),scrollOffset+maxV);i++) {
                if (in((int)mx,(int)my,PANEL_X,modY,PANEL_X+PANEL_W,modY+ROW_H)) { mods.get(i).toggle(); return true; }
                modY+=ROW_H;
            }
        }
        return super.mouseClicked(mx,my,btn);
    }

    @Override public boolean mouseScrolled(double mx,double my,double amt) { scrollOffset-=(int)amt; return true; }

    @Override
    public boolean keyPressed(int key,int scan,int mods) {
        if (searchFocused) {
            if (key==259&&!searchQuery.isEmpty()) { searchQuery=searchQuery.substring(0,searchQuery.length()-1); return true; }
            if (key==256) { searchFocused=false; return true; }
        }
        return super.keyPressed(key,scan,mods);
    }

    @Override
    public boolean charTyped(char chr,int mods) {
        if (searchFocused&&chr>=32) { searchQuery+=chr; return true; }
        return false;
    }

    @Override public boolean shouldPause() { return false; }
}
