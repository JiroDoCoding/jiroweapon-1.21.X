package net.boomjiro.jiroweapon.item;
import net.boomjiro.jiroweapon.item.ReaperBellItem;

import net.boomjiro.jiroweapon.JiroWeapon;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Moditems {

    public static final Item REAPER_BELL = registerItem("reaper_bell",
            new ReaperBellItem(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(JiroWeapon.MOD_ID, name), item);
    }

    public static void registerModItems() {
        JiroWeapon.LOGGER.info("Registering Mod Items for " + JiroWeapon.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(REAPER_BELL);
        });
    }
}
