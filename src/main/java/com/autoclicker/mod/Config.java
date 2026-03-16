package com.autoclicker.mod;

/**
 * All user-configurable settings in one place.
 * Randomisation modes:
 *   NORMAL   — chance re-rolls each click in [chanceMin, chanceMax]
 *   EXTRA    — chance clamped to upper half  [midpoint, chanceMax]
 *   EXTRA_PLUS — fires on every qualifying click (100 %)
 */
public class Config {

    public enum RandomMode {
        NORMAL("Normal"),
        EXTRA("Extra"),
        EXTRA_PLUS("Extra+");

        public final String label;
        RandomMode(String label) { this.label = label; }
    }

    // --- Defaults ---------------------------------------------------------------
    public static final int    DEFAULT_MIN_CPS    = 10;
    public static final double DEFAULT_CHANCE_MIN = 0.20;
    public static final double DEFAULT_CHANCE_MAX = 0.80;
    public static final RandomMode DEFAULT_MODE   = RandomMode.NORMAL;
    public static final int    DEFAULT_GUI_KEY    = org.lwjgl.input.Keyboard.KEY_O;

    // --- Live values ------------------------------------------------------------
    public volatile int        minCps      = DEFAULT_MIN_CPS;
    public volatile double     chanceMin   = DEFAULT_CHANCE_MIN;
    public volatile double     chanceMax   = DEFAULT_CHANCE_MAX;
    public volatile RandomMode mode        = DEFAULT_MODE;
    public volatile int        guiKey      = DEFAULT_GUI_KEY;
    public volatile boolean    enabled     = true;

    // Singleton
    private static final Config INSTANCE = new Config();
    private Config() {}
    public static Config get() { return INSTANCE; }

    /**
     * Resolves the effective [low, high] probability range for the current mode.
     * Returns a double[2] — { low, high }.
     */
    public double[] effectiveRange() {
        switch (mode) {
            case EXTRA:
                double mid = (chanceMin + chanceMax) / 2.0;
                return new double[]{ mid, chanceMax };
            case EXTRA_PLUS:
                return new double[]{ 1.0, 1.0 };
            default: // NORMAL
                return new double[]{ chanceMin, chanceMax };
        }
    }
}
