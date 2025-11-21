package net.boomjiro.jiroweapon;

import org.joml.Matrix4f;
import net.boomjiro.jiroweapon.entity.ModEntities;
import net.boomjiro.jiroweapon.entity.ReaperSkullProjectileEntity;
import net.boomjiro.jiroweapon.entity.RitualCircleEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class JiroWeaponModClient implements ClientModInitializer {

    private static final Identifier RITUAL_TEXTURE =
            Identifier.of(JiroWeapon.MOD_ID, "textures/entity/reaper_circle.png");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.REAPER_SKULL, ReaperSkullRenderer::new);
        EntityRendererRegistry.register(ModEntities.RITUAL_CIRCLE, RitualCircleRenderer::new);
    }

    // ==========================================================
    //  REAPER SKULL RENDERER (WORKING VERSION)
    // ==========================================================
    public static class ReaperSkullRenderer extends EntityRenderer<ReaperSkullProjectileEntity> {

        private final ItemRenderer itemRenderer;

        protected ReaperSkullRenderer(EntityRendererFactory.Context ctx) {
            super(ctx);
            this.itemRenderer = ctx.getItemRenderer();
        }

        @Override
        public Identifier getTexture(ReaperSkullProjectileEntity entity) {
            return Identifier.of("minecraft", "textures/entity/skeleton/skeleton.png");
        }

        @Override
        public void render(ReaperSkullProjectileEntity entity, float yaw, float tickDelta,
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

            matrices.push();

            Vec3d vel = entity.getVelocity();

            //--------------------------------------------
            // Correct rotation (vanilla projectile logic)
            //--------------------------------------------
            if (vel.lengthSquared() > 1.0E-4) {

                float yawVel =
                        (float) Math.toDegrees(Math.atan2(vel.x, vel.z));

                float pitchVel =
                        (float) Math.toDegrees(Math.atan2(vel.y, vel.horizontalLength()));

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yawVel));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitchVel));
            }

            // Bobbing
            float age = entity.age + tickDelta;
            float bob = (float) Math.sin(age * 0.2f) * 0.1f;
            matrices.translate(0.0, 0.1 + bob, 0.0);

            matrices.scale(1.3f, 1.3f, 1.3f);

            ItemStack skull = new ItemStack(Items.SKELETON_SKULL);

            itemRenderer.renderItem(
                    skull,
                    ModelTransformationMode.GROUND,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    0
            );

            matrices.pop();
        }
    }

    // ==========================================================
    //  RITUAL CIRCLE RENDERER (WORKING VERSION)
    // ==========================================================
    public static class RitualCircleRenderer extends EntityRenderer<RitualCircleEntity> {

        private static final float RADIUS = 8.0F;

        protected RitualCircleRenderer(EntityRendererFactory.Context ctx) {
            super(ctx);
        }

        @Override
        public void render(RitualCircleEntity entity, float yaw, float tickDelta,
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

            matrices.push();

            float age = entity.age + tickDelta;

            // Correct ground orientation
            matrices.translate(0.0, 0.02, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-180f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 2f));

            float size = RADIUS * 2f;
            matrices.scale(size, size, size);

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            VertexConsumer vc =
                    vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(RITUAL_TEXTURE));

            int alpha = 240;

            vc.vertex(matrix, -0.5f, 0.0f, -0.5f)
                    .color(255, 255, 255, alpha)
                    .texture(0.0f, 0.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(0f, 1f, 0f);

            vc.vertex(matrix, 0.5f, 0.0f, -0.5f)
                    .color(255, 255, 255, alpha)
                    .texture(1.0f, 0.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(0f, 1f, 0f);

            vc.vertex(matrix, 0.5f, 0.0f, 0.5f)
                    .color(255, 255, 255, alpha)
                    .texture(1.0f, 1.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(0f, 1f, 0f);

            vc.vertex(matrix, -0.5f, 0.0f, 0.5f)
                    .color(255, 255, 255, alpha)
                    .texture(0.0f, 1.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(0f, 1f, 0f);

            matrices.pop();
        }

        @Override
        public Identifier getTexture(RitualCircleEntity entity) {
            return RITUAL_TEXTURE;
        }
    }
}
