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
import net.minecraft.util.math.RotationAxis;

public class JiroWeaponModClient implements ClientModInitializer {

    private static final Identifier RITUAL_TEXTURE =
            Identifier.of(JiroWeapon.MOD_ID, "textures/entity/reaper_circle.png");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.REAPER_SKULL, ReaperSkullRenderer::new);
        EntityRendererRegistry.register(ModEntities.RITUAL_CIRCLE, RitualCircleRenderer::new);
    }

    // ==========================================================
    //  REAPER SKULL RENDERER (SNAPPED 8-DIRECTION SYSTEM)
    // ==========================================================
    public static class ReaperSkullRenderer extends EntityRenderer<ReaperSkullProjectileEntity> {

        private final ItemRenderer itemRenderer;

        protected ReaperSkullRenderer(EntityRendererFactory.Context ctx) {
            super(ctx);
            this.itemRenderer = ctx.getItemRenderer();
        }

        @Override
        public Identifier getTexture(ReaperSkullProjectileEntity entity) {
            // Not actually used for item rendering, but required by superclass
            return Identifier.of("minecraft", "textures/entity/skeleton/skeleton.png");
        }

        @Override
        public void render(ReaperSkullProjectileEntity entity, float yaw, float tickDelta,
                           MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

            matrices.push();

            // ------------------------------------------------------
            // Grab entity yaw/pitch
            // ------------------------------------------------------
            float rawYaw   = entity.getYaw();   // Minecraft yaw (weird, can be negative)
            float pitchDeg = entity.getPitch(); // standard pitch

            // Normalize yaw to [0, 360)
            float yaw360 = (rawYaw % 360f + 360f) % 360f;

            // Snap to nearest 45° (N, NE, E, SE, S, SW, W, NW)
            float snappedYaw = Math.round(yaw360 / 45f) * 45f;

            // ------------------------------------------------------
            // Fix skull item base orientation
            // (skull item lies on its back and faces odd direction)
            // ------------------------------------------------------
            // ======== BASE ITEM FIX (100% accurate to placed skull) ========
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90f));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-snappedYaw));

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitchDeg));

            // Small bobbing effect so it feels "alive"
            float age = entity.age + tickDelta;
            float bob = (float) Math.sin(age * 0.2f) * 0.1f;
            matrices.translate(0.0, 0.1 + bob, 0.0);

            // Scale a bit bigger than a normal item
            matrices.scale(1.3f, 1.3f, 1.3f);

            ItemStack skull = new ItemStack(Items.SKELETON_SKULL);

            // Use FIXED so we don't get weird hand/ground transforms
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

    // ==========================================================
    //  RITUAL CIRCLE RENDERER (your version, kept intact)
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

            // Your current rotation setup
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
