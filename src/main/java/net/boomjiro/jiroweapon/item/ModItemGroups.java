package net.boomjiro.jiroweapon.item;

import net.boomjiro.jiroweapon.JiroWeapon;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup REAPER_BELL_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(JiroWeapon.MOD_ID, "reaper_bell_item"),
            FabricItemGroup.builder().icon(() -> new ItemStack(Moditems.REAPER_BELL))
                    .displayName(Text.translatable("itemgroup.jiroweapon.jiro_weapon"))
                    .entries((displayContext, entries) -> {
                        entries.add(Moditems.REAPER_BELL);
                    })
                    .build());

    public static void registerItemGroups() {
        JiroWeapon.LOGGER.info("Registering Item Groups for " + JiroWeapon.MOD_ID);
    }
}
