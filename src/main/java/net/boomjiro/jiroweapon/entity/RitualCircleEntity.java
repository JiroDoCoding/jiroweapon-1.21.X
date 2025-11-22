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
    public static final int DURATION_TICKS = 80;
    private static final int DAMAGE_INTERVAL = 20;
    private static final float DAMAGE_AMOUNT = 2.0F;

    private UUID ownerUuid;

    public RitualCircleEntity(EntityType<? extends RitualCircleEntity> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0F;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUuid();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
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

        double cx = this.getX();
        double cy = this.getY();
        double cz = this.getZ();

        double yMin = cy - 0.1;
        double yMax = cy + 0.5;

        this.setBoundingBox(new Box(
                cx - RADIUS, yMin, cz - RADIUS,
                cx + RADIUS, yMax, cz + RADIUS
        ));

        if (this.age >= DURATION_TICKS) {
            this.discard();
            return;
        }

        World world = this.getWorld();

        if (world.isClient) {
            if (this.age < 5) {
                spawnSpawnBurst(world);
            }
            spawnAmbientParticles(world);
            return;
        }

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
                    cx - RADIUS, cy - 0.1, cz - RADIUS,
                    cx + RADIUS, cy + 3.0, cz + RADIUS
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

        for (int i = 0; i < 6; i++) {
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

        int ringPoints = 22;
        double baseRadius = RADIUS - 1.0;
        double t = (this.age % 40) / 40.0;

        for (int i = 0; i < ringPoints; i++) {
            double angle = (2 * Math.PI * i / ringPoints) + t * 2 * Math.PI;
            double radius = baseRadius + world.random.nextDouble() * 0.5;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            double px = cx + dx;
            double py = cy + 0.15 + world.random.nextDouble() * 0.1;
            double pz = cz + dz;

            world.addParticle(
                    ParticleTypes.CRIMSON_SPORE,
                    px, py, pz,
                    (world.random.nextDouble() - 0.5) * 0.01,
                    0.02 + world.random.nextDouble() * 0.02,
                    (world.random.nextDouble() - 0.5) * 0.01
            );

            world.addParticle(
                    ParticleTypes.ASH,
                    px, py + 0.05, pz,
                    0.0,
                    0.01,
                    0.0
            );
        }

        double crossBaseY = getY() + 2.5;
        double verticalHalf = RADIUS * 0.9;
        double horizontalHalf = RADIUS * 0.6;
        double thickness = 0.3;

        int verticalSteps = 24;
        int horizontalSteps = 18;

        double topY = crossBaseY - verticalHalf * 0.3;
        double bottomY = crossBaseY + verticalHalf;

        for (int i = 0; i <= verticalSteps; i++) {
            double f = i / (double) verticalSteps;
            double py = topY + (bottomY - topY) * f;

            double ox = (world.random.nextDouble() - 0.5) * thickness;
            double oz = (world.random.nextDouble() - 0.5) * thickness;

            world.addParticle(
                    ParticleTypes.CRIMSON_SPORE,
                    cx + ox,
                    py,
                    cz + oz,
                    0.0,
                    0.01,
                    0.0
            );

            if (world.random.nextFloat() < 0.25f) {
                world.addParticle(
                        ParticleTypes.ASH,
                        cx + ox * 0.8,
                        py + 0.05,
                        cz + oz * 0.8,
                        0.0,
                        0.005,
                        0.0
                );
            }
        }

        double barY = topY + (bottomY - topY) * 0.35;

        for (int i = 0; i <= horizontalSteps; i++) {
            double f = i / (double) horizontalSteps;
            double x = cx - horizontalHalf + 2 * horizontalHalf * f;

            double ox = (world.random.nextDouble() - 0.5) * thickness * 0.6;
            double oz = (world.random.nextDouble() - 0.5) * thickness * 0.6;

            world.addParticle(
                    ParticleTypes.CRIMSON_SPORE,
                    x + ox,
                    barY,
                    cz + oz,
                    0.0,
                    0.01,
                    0.0
            );

            if (world.random.nextFloat() < 0.25f) {
                world.addParticle(
                        ParticleTypes.ASH,
                        x + ox * 0.8,
                        barY + 0.05,
                        cz + oz * 0.8,
                        0.0,
                        0.005,
                        0.0
                );
            }
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