package com.autoclicker.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid          = AutoClickerMod.MODID,
    name           = AutoClickerMod.NAME,
    version        = AutoClickerMod.VERSION,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class AutoClickerMod {

    public static final String MODID   = "autoclickermod";
    public static final String NAME    = "AutoClicker";
    public static final String VERSION = "2.0.0";

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new AutoClickerHandler());
        logger.info("[AutoClicker] loaded.");
    }
}
