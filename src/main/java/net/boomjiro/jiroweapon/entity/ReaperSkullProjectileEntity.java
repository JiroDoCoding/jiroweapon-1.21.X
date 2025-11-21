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
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

public class ReaperSkullProjectileEntity extends ThrownItemEntity {

    private float initialYaw = 0f;
    private float initialPitch = 0f;

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

            circle.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0, 0);
            world.spawnEntity(circle);

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

        // Extra collision check
        if (!this.getWorld().isClient) {
            HitResult hit = ProjectileUtil.getCollision(this, this::canHit);
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                this.onCollision(hit);
            }
        }

        // Particles
        if (this.getWorld().isClient) {
            Vec3d vel = this.getVelocity();
            double x = getX();
            double y = getY();
            double z = getZ();

            // Core particles
            this.getWorld().addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z, 0, 0, 0);
            this.getWorld().addParticle(ParticleTypes.ASH,          x, y, z, 0, 0, 0);

            // Trailing wisps
            int steps = 5;
            for (int i = 0; i < steps; i++) {
                double t = (i / (double) steps) * 0.7;
                double bx = x - vel.x * t;
                double by = y - vel.y * t;
                double bz = z - vel.z * t;

                double spread = 0.02 + vel.length() * 0.05;

                this.getWorld().addParticle(ParticleTypes.CRIMSON_SPORE,
                        bx, by, bz,
                        (random.nextDouble() - 0.5) * spread,
                        (random.nextDouble() - 0.5) * spread,
                        (random.nextDouble() - 0.5) * spread);

                this.getWorld().addParticle(ParticleTypes.ASH,
                        bx, by, bz,
                        (random.nextDouble() - 0.5) * spread * 0.5,
                        (random.nextDouble() - 0.5) * spread * 0.5,
                        (random.nextDouble() - 0.5) * spread * 0.5);
            }
        }
    }
}