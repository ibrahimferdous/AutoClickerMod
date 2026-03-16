package com.autoclicker.mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class AutoClickerHandler {

    // --- Constants --------------------------------------------------------------

    /** CPS tracking window — 0.3 s in milliseconds. */
    private static final long WINDOW_MS = 300L;

    // --- State ------------------------------------------------------------------

    private final Minecraft mc     = Minecraft.getMinecraft();
    private final Random    random = new Random();
    private final Config    cfg    = Config.get();

    /** Timestamps (ms) of real left-clicks inside the sliding window. */
    private final Deque<Long> window = new ArrayDeque<>();

    /** True when the next client tick should fire an extra click. */
    private boolean pendingClick = false;

    // --- Events -----------------------------------------------------------------

    /** Opens/closes the GUI when the configured key is pressed. */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) return;
        if (Keyboard.getEventKey() != cfg.guiKey) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (mc.currentScreen instanceof AutoClickerGui) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (mc.currentScreen == null) {
            mc.displayGuiScreen(new AutoClickerGui());
        }
    }

    /** Detects real left-clicks and decides whether to schedule an extra one. */
    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (Mouse.getEventButton() != 0 || !Mouse.getEventButtonState()) return;
        if (!isActive()) return;

        long now = System.currentTimeMillis();
        window.addLast(now);
        evict(now);

        double cps = window.size() / (WINDOW_MS / 1000.0);
        if (cps < cfg.minCps) return;

        double[] range = cfg.effectiveRange();
        double low  = range[0];
        double high = range[1];

        // Two-roll: randomise the threshold each click, then roll against it
        double threshold = low + random.nextDouble() * (high - low);
        if (random.nextDouble() < threshold) {
            pendingClick = true;
        }
    }

    /** Fires the pending extra click at the start of the next client tick. */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!pendingClick) return;

        pendingClick = false;

        // Re-check: a screen may have opened between the click and this tick
        if (!isActive()) return;

        injectClick();
    }

    // --- Helpers ----------------------------------------------------------------

    private boolean isActive() {
        return cfg.enabled
            && mc.theWorld  != null
            && mc.thePlayer != null
            && !mc.isGamePaused()
            && mc.currentScreen == null;
    }

    private void evict(long now) {
        long cutoff = now - WINDOW_MS;
        while (!window.isEmpty() && window.peekFirst() < cutoff) {
            window.pollFirst();
        }
    }

    private void injectClick() {
        try {
            int key = mc.gameSettings.keyBindAttack.getKeyCode();
            KeyBinding.setKeyBindState(key, true);
            mc.playerController.updateController();
            KeyBinding.setKeyBindState(key, false);
        } catch (Exception e) {
            AutoClickerMod.logger.warn("[AutoClicker] inject failed: " + e.getMessage());
        }
    }
}
