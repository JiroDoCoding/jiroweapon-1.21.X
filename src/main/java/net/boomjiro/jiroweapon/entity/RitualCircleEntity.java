package net.boomjiro.jiroweapon.entity;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class RitualCircleEntity extends Entity {

    public static final int RADIUS = 8;
    private static final int DURATION_TICKS = 80;   // ~4 seconds
    private static final int DAMAGE_INTERVAL = 20;  // once per second
    private static final float DAMAGE_AMOUNT = 2.0F; // 1 heart

    private UUID ownerUuid;

    public RitualCircleEntity(EntityType<? extends RitualCircleEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUuid();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // no tracked data yet
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Owner", NbtElement.INT_ARRAY_TYPE)) {
            ownerUuid = nbt.getUuid("Owner");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 🔹 BIG bounding box so the circle keeps rendering from farther away
        this.setBoundingBox(new Box(
                getX() - RADIUS, getY() - 2, getZ() - RADIUS,
                getX() + RADIUS, getY() + 2, getZ() + RADIUS
        ));

        if (this.age >= DURATION_TICKS) {
            this.discard();
            return;
        }

        World world = this.getWorld();

        // Client: a few ambient particles only (shape comes from renderer)
        if (world.isClient) {
            if (this.age < 5) {
                spawnSpawnBurst(world);
            }
            spawnAmbientParticles(world);
            return;
        }

        // Server: damage + lifesteal
        if (this.age % DAMAGE_INTERVAL == 0) {
            ServerWorld serverWorld = (ServerWorld) world;
            LivingEntity owner = null;

            if (ownerUuid != null) {
                Entity e = serverWorld.getEntity(ownerUuid);
                if (e instanceof LivingEntity living) {
                    owner = living;
                }
            }

            Box box = new Box(
                    getX() - RADIUS, getY() - 1, getZ() - RADIUS,
                    getX() + RADIUS, getY() + 3, getZ() + RADIUS
            );

            List<LivingEntity> targets = world.getNonSpectatingEntities(LivingEntity.class, box);
            float total = 0f;

            for (LivingEntity target : targets) {
                if (ownerUuid != null && target.getUuid().equals(ownerUuid)) continue;
                if (!target.isAlive()) continue;

                DamageSource source = world.getDamageSources().magic();
                if (target.damage(source, DAMAGE_AMOUNT)) {
                    total += DAMAGE_AMOUNT;
                }
            }

            if (owner != null && total > 0f) {
                owner.heal(total);
            }
        }
    }

    private void spawnAmbientParticles(World world) {
        double cx = getX();
        double cy = getY() + 0.1;
        double cz = getZ();

        // A few souls rising from the circle
        for (int i = 0; i < 4; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double radius = 1.0 + world.random.nextDouble() * (RADIUS - 1.5);
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            world.addParticle(
                    ParticleTypes.SOUL,
                    cx + dx,
                    cy,
                    cz + dz,
                    0.0,
                    0.04,
                    0.0
            );
        }
    }

    private void spawnSpawnBurst(World world) {
        double centerX = getX();
        double centerY = getY() + 0.1;
        double centerZ = getZ();

        int ringPoints = 40;
        double radius = 1.0 + (this.age + 1) * 0.5;

        for (int i = 0; i < ringPoints; i++) {
            double angle = (Math.PI * 2 * i) / ringPoints;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            world.addParticle(
                    ParticleTypes.FLAME,
                    centerX + dx,
                    centerY + 0.05,
                    centerZ + dz,
                    0.0,
                    0.02,
                    0.0
            );
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }
}