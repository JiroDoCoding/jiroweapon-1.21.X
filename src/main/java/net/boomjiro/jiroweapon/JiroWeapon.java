package net.boomjiro.jiroweapon;

import net.boomjiro.jiroweapon.item.ModItemGroups;
import net.boomjiro.jiroweapon.item.Moditems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JiroWeapon implements ModInitializer {
	public static final String MOD_ID = "jiroweapon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModItemGroups.registerItemGroups();
        Moditems.registerModItems();
	}
}