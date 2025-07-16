package net.teamremastered.tlc;

import net.fabricmc.api.ModInitializer;
import net.teamremastered.tlc.registries.LCProcessors;
import net.teamremastered.tlc.registries.LCStructure;
import net.teamremastered.tlc.util.LCMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheLostCastle implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("tlc");
	public static final String MODID = "tlc";

	@Override
	public void onInitialize() {

		LCStructure.init();
		LCProcessors.init();
		LCMap.registerVillagerTrades();
	}
}
