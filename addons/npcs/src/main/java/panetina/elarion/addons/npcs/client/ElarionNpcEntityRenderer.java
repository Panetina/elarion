package panetina.elarion.addons.npcs.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntity;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;

public final class ElarionNpcEntityRenderer
        extends LivingEntityRenderer<ElarionNpcEntity, PlayerEntityModel<ElarionNpcEntity>> {
    private static final Identifier DEFAULT_TEXTURE =
            Identifier.of("elarion", "textures/entity/npc/worldheart_banker.png");

    private final PlayerEntityModel<ElarionNpcEntity> normalModel;
    private final PlayerEntityModel<ElarionNpcEntity> slimModel;

    public ElarionNpcEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.5F);
        normalModel = this.model;
        slimModel = new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(
            ElarionNpcEntity entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertices,
            int light
    ) {
        SkinTextures skin = resolvedPlayerSkin(entity);
        this.model = skin != null && skin.model() == SkinTextures.Model.SLIM ? slimModel : normalModel;
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(ElarionNpcEntity entity) {
        SkinTextures skin = resolvedPlayerSkin(entity);
        if (skin != null) return skin.texture();
        return visual(entity)
                .flatMap(entry -> NpcSkinResolver.texture(entry.skinTexture())
                        .or(() -> "texture".equalsIgnoreCase(entry.skinFallbackType())
                                ? NpcSkinResolver.texture(entry.skinFallbackTexture())
                                : java.util.Optional.empty()))
                .orElse(DEFAULT_TEXTURE);
    }

    @Override
    protected void scale(ElarionNpcEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(0.9375F, 0.9375F, 0.9375F);
    }

    private SkinTextures resolvedPlayerSkin(ElarionNpcEntity entity) {
        NpcVisualSyncPayload.Entry visual = visual(entity).orElse(null);
        if (visual == null || !"player_body".equalsIgnoreCase(visual.skinType())
                || visual.skinPlayerName().isBlank()) {
            return null;
        }
        return NpcSkinResolver.playerSkin(visual.skinPlayerName()).orElse(null);
    }

    private java.util.Optional<NpcVisualSyncPayload.Entry> visual(ElarionNpcEntity entity) {
        return NpcClientVisuals.findByEntity(entity.getUuid())
                .or(() -> entity.placedNpcId().flatMap(NpcClientVisuals::findByNpc));
    }
}
