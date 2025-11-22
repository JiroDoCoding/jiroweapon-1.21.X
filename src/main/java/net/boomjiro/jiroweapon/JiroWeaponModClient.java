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
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class JiroWeaponModClient implements ClientModInitializer {

    private static final Identifier RITUAL_TEXTURE =
            Identifier.of(JiroWeapon.MOD_ID, "textures/entity/reaper_circle.png");

    private static final Identifier REAPER_CROSS_TEXTURE =
            Identifier.of(JiroWeapon.MOD_ID, "textures/entity/reaper_cross.png");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.REAPER_SKULL, ReaperSkullRenderer::new);
        EntityRendererRegistry.register(ModEntities.RITUAL_CIRCLE, RitualCircleRenderer::new);
    }

    // ==========================================================
    //  REAPER SKULL RENDERER – follow entity's yaw/pitch
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
        public void render(ReaperSkullProjectileEntity entity, float unusedYaw, float tickDelta,
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

            matrices.push();

            // Interpolated rotation from the entity
            float yaw   = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
            float pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

            // Small bobbing
            float age = entity.age + tickDelta;
            float bob = (float) Math.sin(age * 0.2f) * 0.1f;
            matrices.translate(0.0, 0.1 + bob, 0.0);

            // Horizontal orientation (this already works for N/E/S/W)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw + 180.0f));

            // Vertical orientation: flip sign so looking up makes it face away
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

            matrices.scale(1.3f, 1.3f, 1.3f);

            ItemStack skull = new ItemStack(Items.SKELETON_SKULL);
            itemRenderer.renderItem(
                    skull,
                    ModelTransformationMode.FIXED,
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

    public static class RitualCircleRenderer extends EntityRenderer<RitualCircleEntity> {

        private static final float RADIUS = 8.0F;

        protected RitualCircleRenderer(EntityRendererFactory.Context ctx) {
            super(ctx);
        }

        @Override
        public void render(RitualCircleEntity entity, float yaw, float tickDelta,
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

            float age = entity.age + tickDelta;
            int glowLight = 0xF000F0;

            int total = RitualCircleEntity.DURATION_TICKS;
            int fadeTicks = 20;
            float fadeFactor = 1.0f;
            float timeLeft = total - age;

            if (timeLeft <= fadeTicks) {
                fadeFactor = Math.max(0.0f, timeLeft / fadeTicks);
            }

            int crossBaseAlpha = 255;
            int circleBaseAlpha = 240;

            int crossAlpha = (int) (crossBaseAlpha * fadeFactor);
            int circleAlpha = (int) (circleBaseAlpha * fadeFactor);

            if (crossAlpha <= 0 && circleAlpha <= 0) {
                return;
            }

            matrices.push();

            float crossHeight = 6.0f;
            float crossWidth = 3.0f;

            matrices.translate(0.0f, 5.0f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 1.5f));
            matrices.scale(crossWidth, crossHeight, 1.0f);

            MatrixStack.Entry crossEntry = matrices.peek();
            Matrix4f crossMatrix = crossEntry.getPositionMatrix();

            VertexConsumer crossVc =
                    vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(REAPER_CROSS_TEXTURE));

            crossVc.vertex(crossMatrix, -0.5f, -0.5f, 0.0f)
                    .color(255, 255, 255, crossAlpha)
                    .texture(0.0f, 0.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 0f, -1f);

            crossVc.vertex(crossMatrix, 0.5f, -0.5f, 0.0f)
                    .color(255, 255, 255, crossAlpha)
                    .texture(1.0f, 0.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 0f, -1f);

            crossVc.vertex(crossMatrix, 0.5f, 0.5f, 0.0f)
                    .color(255, 255, 255, crossAlpha)
                    .texture(1.0f, 1.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 0f, -1f);

            crossVc.vertex(crossMatrix, -0.5f, 0.5f, 0.0f)
                    .color(255, 255, 255, crossAlpha)
                    .texture(0.0f, 1.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 0f, -1f);

            matrices.pop();

            matrices.push();

            matrices.translate(0.0, 0.02, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-180f));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 2f));

            float size = RADIUS * 2f;
            matrices.scale(size, size, size);

            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();

            VertexConsumer vc =
                    vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(RITUAL_TEXTURE));

            vc.vertex(matrix, -0.5f, 0.0f, -0.5f)
                    .color(255, 255, 255, circleAlpha)
                    .texture(0.0f, 0.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 1f, 0f);

            vc.vertex(matrix, 0.5f, 0.0f, -0.5f)
                    .color(255, 255, 255, circleAlpha)
                    .texture(1.0f, 0.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 1f, 0f);

            vc.vertex(matrix, 0.5f, 0.0f, 0.5f)
                    .color(255, 255, 255, circleAlpha)
                    .texture(1.0f, 1.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 1f, 0f);

            vc.vertex(matrix, -0.5f, 0.0f, 0.5f)
                    .color(255, 255, 255, circleAlpha)
                    .texture(0.0f, 1.0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(glowLight)
                    .normal(0f, 1f, 0f);

            matrices.pop();
        }

        @Override
        public Identifier getTexture(RitualCircleEntity entity) {
            return RITUAL_TEXTURE;
        }
    }
}
