package net.boomjiro.jiroweapon.entity;

import net.boomjiro.jiroweapon.JiroWeapon;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<ReaperSkullProjectileEntity> REAPER_SKULL =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(JiroWeapon.MOD_ID, "reaper_skull"),
                    FabricEntityTypeBuilder.<ReaperSkullProjectileEntity>create(
                                    SpawnGroup.MISC, ReaperSkullProjectileEntity::new)
                            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                            .trackRangeBlocks(64)
                            .trackedUpdateRate(10)
                            .build()
            );

    public static final EntityType<RitualCircleEntity> RITUAL_CIRCLE =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of(JiroWeapon.MOD_ID, "ritual_circle"),
                    FabricEntityTypeBuilder.<RitualCircleEntity>create(
                                    SpawnGroup.MISC, RitualCircleEntity::new)
                            .dimensions(EntityDimensions.fixed(0.1f, 0.1f))
                            .trackRangeBlocks(64)
                            .trackedUpdateRate(10)
                            .build()
            );

    public static void registerModEntities() {
        JiroWeapon.LOGGER.info("Registering mod entities for {}", JiroWeapon.MOD_ID);
    }
}