package com.remmod.remcompanion.client;

import com.remmod.remcompanion.RemCompanion;
import com.remmod.remcompanion.entity.RemEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RemRenderer extends HumanoidMobRenderer<RemEntity, PlayerModel<RemEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RemCompanion.MOD_ID, "textures/entity/rem.png");

    public RemRenderer(EntityRendererProvider.Context ctx) {
        // Skin de brazos finos (slim)
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RemEntity entity) {
        return TEXTURE;
    }
}
