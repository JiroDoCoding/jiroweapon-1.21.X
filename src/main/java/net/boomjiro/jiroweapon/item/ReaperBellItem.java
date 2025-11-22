package net.boomjiro.jiroweapon.item;

import net.boomjiro.jiroweapon.entity.ModEntities;
import net.boomjiro.jiroweapon.entity.ReaperSkullProjectileEntity;
import net.boomjiro.jiroweapon.entity.RitualCircleEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ReaperBellItem extends Item {

    private static final int MAX_USE_TIME = 60;
    private static final int CHARGE_THRESHOLD = 20;
    private static final int COOLDOWN_TICKS = 150;

    public ReaperBellItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }

        if (!world.isClient) {
            ReaperSkullProjectileEntity projectile =
                    new ReaperSkullProjectileEntity(world, user, user.getYaw(), user.getPitch());

            projectile.setVelocity(
                    user,
                    user.getPitch(),
                    user.getYaw(),
                    0.0F,
                    1.5F,
                    0.0F
            );

            world.spawnEntity(projectile);

            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_WITHER_SHOOT,
                    SoundCategory.PLAYERS,
                    0.7f,
                    1.0f
            );

            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return MAX_USE_TIME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int usedTicks = MAX_USE_TIME - remainingUseTicks;

        if (world.isClient && user.isSneaking()) {
            double x = user.getX();
            double y = user.getY();
            double z = user.getZ();

            for (int i = 0; i < 6; i++) {
                double angle = world.getRandom().nextDouble() * Math.PI * 2;
                double radius = 0.8 + world.getRandom().nextDouble() * 0.4;
                double dx = Math.cos(angle) * radius;
                double dz = Math.sin(angle) * radius;

                world.addParticle(
                        ParticleTypes.CRIMSON_SPORE,
                        x + dx,
                        y + 0.1,
                        z + dz,
                        0.0, 0.02, 0.0
                );

                world.addParticle(
                        ParticleTypes.ASH,
                        x + dx * 0.7,
                        y + 0.25,
                        z + dz * 0.7,
                        0.0, 0.01, 0.0
                );
            }

            world.addParticle(
                    ParticleTypes.SOUL,
                    x,
                    y + 0.3,
                    z,
                    0.0,
                    0.03,
                    0.0
            );
        }

        if (!world.isClient && remainingUseTicks == MAX_USE_TIME - 1 && user instanceof PlayerEntity player) {
            world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    SoundCategory.PLAYERS,
                    0.6f,
                    0.8f + world.getRandom().nextFloat() * 0.4f
            );
        }

        if (!world.isClient && user.isSneaking() && usedTicks == CHARGE_THRESHOLD && user instanceof PlayerEntity player) {
            RitualCircleEntity circle = new RitualCircleEntity(ModEntities.RITUAL_CIRCLE, world);
            circle.setOwner(player);
            circle.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0, 0);
            world.spawnEntity(circle);

            world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                    SoundCategory.PLAYERS,
                    0.7f,
                    0.6f + world.random.nextFloat() * 0.2f
            );

            player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
            player.stopUsingItem();
        }
    }
}
