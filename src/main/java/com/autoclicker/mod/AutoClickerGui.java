package com.autoclicker.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

/**
 * AutoClicker GUI — opened with the configured key (default O).
 *
 * Layout (centered panel):
 *   ┌─────────────────────────────┐
 *   │  AutoClicker          v2.0  │
 *   ├─────────────────────────────┤
 *   │  Min CPS      [  10  ]      │
 *   │  Chance min   [ 20%  ]      │
 *   │  Chance max   [ 80%  ]      │
 *   │  Mode  [ Normal ][ Extra ][ Extra+ ] │
 *   │  GUI key      [  O   ]      │
 *   ├─────────────────────────────┤
 *   │   [ Enable ]     [ Close ]  │
 *   └─────────────────────────────┘
 *
 * On close: restores game state fully (mouse grabbed, no lingering elements).
 */
public class AutoClickerGui extends GuiScreen {

    // Panel dimensions
    private static final int PW = 260;
    private static final int PH = 230;

    // Colors
    private static final int COL_BG        = color(12, 12, 12, 210);
    private static final int COL_PANEL     = color(20, 20, 20, 230);
    private static final int COL_BORDER    = color(55, 55, 55, 255);
    private static final int COL_ACCENT    = color(90, 200, 130, 255);
    private static final int COL_ACCENT_OFF= color(200, 80, 80, 255);
    private static final int COL_TEXT      = color(220, 220, 220, 255);
    private static final int COL_MUTED     = color(120, 120, 120, 255);
    private static final int COL_FIELD_BG  = color(30, 30, 30, 255);
    private static final int COL_BTN       = color(38, 38, 38, 255);
    private static final int COL_BTN_HOV   = color(55, 55, 55, 255);

    private final Config cfg = Config.get();

    // Text fields
    private GuiTextField fieldMinCps;
    private GuiTextField fieldChanceMin;
    private GuiTextField fieldChanceMax;
    private GuiTextField fieldGuiKey;

    // Buttons
    private GuiButton btnToggle;
    private GuiButton btnClose;
    private GuiButton btnModeNormal;
    private GuiButton btnModeExtra;
    private GuiButton btnModeExtraPlus;

    // Panel top-left
    private int px, py;

    // Whether we are capturing a new key for the GUI hotkey
    private boolean capturingKey = false;

    @Override
    public void initGui() {
        px = (width  - PW) / 2;
        py = (height - PH) / 2;

        int fx  = px + 130;   // field x
        int fw  = 80;          // field width
        int fh  = 16;          // field height
        int row = py + 48;     // first row y
        int gap = 24;          // row gap

        // --- Text fields ---
        fieldMinCps = field(0, fx, row,        fw, fh, String.valueOf(cfg.minCps));
        fieldChanceMin = field(1, fx, row + gap,   fw, fh, pct(cfg.chanceMin));
        fieldChanceMax = field(2, fx, row + gap*2, fw, fh, pct(cfg.chanceMax));
        fieldGuiKey    = field(3, fx, row + gap*4, fw, fh, Keyboard.getKeyName(cfg.guiKey));

        buttonList.add(fieldMinCps);
        buttonList.add(fieldChanceMin);
        buttonList.add(fieldChanceMax);
        buttonList.add(fieldGuiKey);

        // --- Mode buttons ---
        int modeY = row + gap * 3;
        int mw = 66;
        btnModeNormal    = modeBtn(10, px + 18,        modeY, mw, Config.RandomMode.NORMAL);
        btnModeExtra     = modeBtn(11, px + 18 + mw+4, modeY, mw, Config.RandomMode.EXTRA);
        btnModeExtraPlus = modeBtn(12, px + 18 + mw*2+8, modeY, mw + 8, Config.RandomMode.EXTRA_PLUS);

        buttonList.add(btnModeNormal);
        buttonList.add(btnModeExtra);
        buttonList.add(btnModeExtraPlus);

        // --- Bottom buttons ---
        int botY = py + PH - 34;
        btnToggle = new StyledButton(20, px + 18,       botY, 106, 22, toggleLabel());
        btnClose  = new StyledButton(21, px + 136,      botY, 106, 22, "Close");

        buttonList.add(btnToggle);
        buttonList.add(btnClose);

        refreshModeButtons();
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        // --- Full-screen dim ---
        drawRect(0, 0, width, height, COL_BG);

        // --- Panel background ---
        drawRect(px, py, px + PW, py + PH, COL_PANEL);

        // --- Border ---
        drawBorder(px, py, PW, PH, COL_BORDER);

        // --- Title bar ---
        int titleBarBot = py + 32;
        drawRect(px, py, px + PW, titleBarBot, color(26, 26, 26, 255));
        drawBorderBottom(px, py, PW, 32, COL_BORDER);

        mc.fontRendererObj.drawString("AutoClicker", px + 14, py + 12, COL_TEXT);
        mc.fontRendererObj.drawString("v2.0", px + PW - 26, py + 12, COL_MUTED);

        // --- Row labels ---
        int row = py + 48;
        int gap = 24;
        int lx  = px + 18;
        int ly  = row + 3;
        drawLabel("Min CPS",    lx, ly);
        drawLabel("Chance min", lx, ly + gap);
        drawLabel("Chance max", lx, ly + gap * 2);
        drawLabel("Mode",       lx, ly + gap * 3);
        drawLabel("GUI key",    lx, ly + gap * 4);

        // --- Separator before buttons ---
        drawRect(px, py + PH - 42, px + PW, py + PH - 41, COL_BORDER);

        // Key capture hint
        if (capturingKey) {
            String hint = "Press any key...";
            int hw = mc.fontRendererObj.getStringWidth(hint);
            mc.fontRendererObj.drawString(hint, px + 130, py + PH - 38 + gap * 4 - gap * 4 + row - py - 48 + gap * 4 + 3, COL_ACCENT);
        }

        // Draw text fields and buttons
        for (Object o : buttonList) {
            if (o instanceof GuiTextField) ((GuiTextField) o).drawTextBox();
            else if (o instanceof GuiButton) ((GuiButton) o).drawButton(mc, mx, my);
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn.id == 20) {                         // toggle enable
            cfg.enabled = !cfg.enabled;
            btnToggle.displayString = toggleLabel();

        } else if (btn.id == 21) {                  // close
            applyFields();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();

        } else if (btn.id == 10) setMode(Config.RandomMode.NORMAL);
          else if (btn.id == 11) setMode(Config.RandomMode.EXTRA);
          else if (btn.id == 12) setMode(Config.RandomMode.EXTRA_PLUS);
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (capturingKey) {
            if (key != Keyboard.KEY_ESCAPE) {
                cfg.guiKey = key;
                fieldGuiKey.setText(Keyboard.getKeyName(key));
            }
            capturingKey = false;
            fieldGuiKey.setFocused(false);
            return;
        }

        if (key == Keyboard.KEY_ESCAPE) {
            applyFields();
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
            return;
        }

        // Route to focused field
        if (fieldMinCps.isFocused())    fieldMinCps.textboxKeyTyped(c, key);
        else if (fieldChanceMin.isFocused()) fieldChanceMin.textboxKeyTyped(c, key);
        else if (fieldChanceMax.isFocused()) fieldChanceMax.textboxKeyTyped(c, key);
        else if (fieldGuiKey.isFocused()) {
            // Any key typed into the key field starts capture mode
            capturingKey = true;
            fieldGuiKey.setText("...");
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
        fieldMinCps.mouseClicked(mx, my, btn);
        fieldChanceMin.mouseClicked(mx, my, btn);
        fieldChanceMax.mouseClicked(mx, my, btn);

        // Click on key field → start capture
        if (fieldGuiKey.mouseClicked(mx, my, btn)) {
            capturingKey = true;
            fieldGuiKey.setText("...");
        }
    }

    @Override
    public void updateScreen() {
        fieldMinCps.updateCursorCounter();
        fieldChanceMin.updateCursorCounter();
        fieldChanceMax.updateCursorCounter();
        fieldGuiKey.updateCursorCounter();
    }

    /** True — clicking outside the panel doesn't close it accidentally. */
    @Override
    public boolean doesGuiPauseGame() { return false; }

    // --- Internal helpers -------------------------------------------------------

    private void applyFields() {
        try { cfg.minCps = Math.max(1, Integer.parseInt(fieldMinCps.getText().trim())); } catch (Exception ignored) {}
        try { cfg.chanceMin = Math.min(0.99, Math.max(0.01, parsePct(fieldChanceMin.getText()))); } catch (Exception ignored) {}
        try { cfg.chanceMax = Math.min(1.00, Math.max(cfg.chanceMin + 0.01, parsePct(fieldChanceMax.getText()))); } catch (Exception ignored) {}
    }

    private void setMode(Config.RandomMode m) {
        cfg.mode = m;
        refreshModeButtons();
    }

    private void refreshModeButtons() {
        styleMode(btnModeNormal,    cfg.mode == Config.RandomMode.NORMAL);
        styleMode(btnModeExtra,     cfg.mode == Config.RandomMode.EXTRA);
        styleMode(btnModeExtraPlus, cfg.mode == Config.RandomMode.EXTRA_PLUS);
    }

    private void styleMode(GuiButton b, boolean active) {
        b.enabled = !active; // active button appears "pressed" (dimmed)
    }

    private String toggleLabel() {
        return cfg.enabled ? "§aEnabled" : "§cDisabled";
    }

    private GuiTextField field(int id, int x, int y, int w, int h, String val) {
        GuiTextField f = new GuiTextField(id, mc.fontRendererObj, x, y, w, h);
        f.setMaxStringLength(8);
        f.setText(val);
        f.setEnableBackgroundDrawing(true);
        return f;
    }

    private GuiButton modeBtn(int id, int x, int y, int w, Config.RandomMode mode) {
        return new StyledButton(id, x, y, w, 16, mode.label);
    }

    private void drawLabel(String text, int x, int y) {
        mc.fontRendererObj.drawString(text, x, y, COL_MUTED);
    }

    private void drawBorder(int x, int y, int w, int h, int col) {
        drawRect(x, y, x + w, y + 1, col);
        drawRect(x, y + h - 1, x + w, y + h, col);
        drawRect(x, y, x + 1, y + h, col);
        drawRect(x + w - 1, y, x + w, y + h, col);
    }

    private void drawBorderBottom(int x, int y, int w, int h, int col) {
        drawRect(x, y + h, x + w, y + h + 1, col);
    }

    private static String pct(double v) {
        return String.valueOf((int)(v * 100));
    }

    private static double parsePct(String s) {
        s = s.replace("%", "").trim();
        double v = Double.parseDouble(s);
        return v > 1 ? v / 100.0 : v;
    }

    private static int color(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // --- Styled button (darker, borderless) ------------------------------------
    private static class StyledButton extends GuiButton {
        StyledButton(int id, int x, int y, int w, int h, String label) {
            super(id, x, y, w, h, label);
        }

        @Override
        public void drawButton(Minecraft mc, int mx, int my) {
            if (!visible) return;
            hovered = mx >= xPosition && my >= yPosition
                   && mx < xPosition + width && my < yPosition + height;

            int bg = hovered ? COL_BTN_HOV : COL_BTN;
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bg);
            drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, COL_BORDER);
            drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, COL_BORDER);
            drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, COL_BORDER);
            drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, COL_BORDER);

            // Dim if disabled (used for active mode button)
            int textCol = enabled ? COL_TEXT : COL_MUTED;
            mc.fontRendererObj.drawString(
                displayString,
                xPosition + (width  - mc.fontRendererObj.getStringWidth(displayString)) / 2,
                yPosition + (height - 8) / 2,
                textCol
            );
        }
    }
}
