package net.boomjiro.jiroweapon.entity;

import net.boomjiro.jiroweapon.item.Moditems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ReaperSkullProjectileEntity extends ThrownItemEntity {

    private float initialYaw;
    private float initialPitch;

    public ReaperSkullProjectileEntity(EntityType<? extends ReaperSkullProjectileEntity> type, World world) {
        super(type, world);
    }

    public ReaperSkullProjectileEntity(EntityType<? extends ReaperSkullProjectileEntity> type,
                                       World world,
                                       LivingEntity owner) {
        super(type, owner, world);
    }

    public ReaperSkullProjectileEntity(World world, LivingEntity owner) {
        super(ModEntities.REAPER_SKULL, owner, world);
    }

    // ✔ NEW: constructor that stores yaw/pitch
    public ReaperSkullProjectileEntity(World world, LivingEntity owner, float yaw, float pitch) {
        super(ModEntities.REAPER_SKULL, owner, world);
        this.initialYaw = yaw;
        this.initialPitch = pitch;
        this.setYaw(yaw);
        this.setPitch(pitch);
    }

    // ✔ Getters for renderer
    public float getInitialYaw() { return initialYaw; }
    public float getInitialPitch() { return initialPitch; }

    @Override
    protected Item getDefaultItem() {
        return Moditems.REAPER_BELL;
    }

    public ItemStack getStack() {
        return new ItemStack(Moditems.REAPER_BELL);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient) {
            World world = this.getWorld();

            RitualCircleEntity circle = new RitualCircleEntity(ModEntities.RITUAL_CIRCLE, world);
            if (this.getOwner() instanceof LivingEntity living) {
                circle.setOwner(living);
            }

            double x = hitResult.getPos().x;
            double y = hitResult.getPos().y;
            double z = hitResult.getPos().z;

            if (hitResult instanceof BlockHitResult blockHit) {
                // Use the exact hit height, slightly above the hit face
                y = blockHit.getPos().y + 0.02;
            } else if (hitResult instanceof EntityHitResult entityHit) {
                // Under the entity we hit (centered on them)
                Entity target = entityHit.getEntity();
                x = target.getX();
                z = target.getZ();
                y = target.getY() + 0.02;
            } else {
                // Fallback: use ray hit position slightly above
                y = hitResult.getPos().y + 0.02;
            }

            circle.refreshPositionAndAngles(x, y, z, 0, 0);
            world.spawnEntity(circle);

            world.playSound(
                    null,
                    x, y, z,
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                    SoundCategory.PLAYERS,
                    0.9f,
                    0.7f + world.random.nextFloat() * 0.3f
            );

            this.discard();
        }
    }

    @Override
    protected boolean canHit(Entity entity) {
        return entity instanceof LivingEntity;
    }

    @Override
    public void tick() {
        super.tick();

        // Keep rotation aligned with current velocity (like arrows do)
        Vec3d vel = this.getVelocity();
        if (vel.lengthSquared() > 1.0E-4) {
            double vx = vel.x;
            double vy = vel.y;
            double vz = vel.z;

            float horiz = MathHelper.sqrt((float)(vx * vx + vz * vz));

            float newYaw = (float)(MathHelper.atan2(vx, vz) * (180F / (float)Math.PI));
            float newPitch = (float)(MathHelper.atan2(vy, horiz) * (180F / (float)Math.PI));

            this.setYaw(newYaw);
            this.setPitch(newPitch);
        }

        if (!this.getWorld().isClient) {
            HitResult hit = ProjectileUtil.getCollision(this, this::canHit);
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                this.onCollision(hit);
            }
        }

        if (this.getWorld().isClient) {
            Vec3d velClient = this.getVelocity();
            double x = getX();
            double y = getY();
            double z = getZ();

            this.getWorld().addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z, 0, 0, 0);
            this.getWorld().addParticle(ParticleTypes.ASH, x, y, z, 0, 0, 0);

            int steps = 5;
            for (int i = 0; i < steps; i++) {
                double t = (i / (double) steps) * 0.7;
                double bx = x - velClient.x * t;
                double by = y - velClient.y * t;
                double bz = z - velClient.z * t;

                double spread = 0.02 + velClient.length() * 0.05;

                this.getWorld().addParticle(
                        ParticleTypes.CRIMSON_SPORE,
                        bx, by, bz,
                        (random.nextDouble() - 0.5) * spread,
                        (random.nextDouble() - 0.5) * spread,
                        (random.nextDouble() - 0.5) * spread
                );

                this.getWorld().addParticle(
                        ParticleTypes.ASH,
                        bx, by, bz,
                        (random.nextDouble() - 0.5) * spread * 0.5,
                        (random.nextDouble() - 0.5) * spread * 0.5,
                        (random.nextDouble() - 0.5) * spread * 0.5
                );
            }
        }
    }
}