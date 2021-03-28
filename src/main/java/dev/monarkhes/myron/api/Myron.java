package dev.monarkhes.myron.api;

import dev.monarkhes.myron.impl.mixin.BakedModelManagerAccessor;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Myron {
    /**
     * Gets a baked model by its ID.
     * Useful for models that aren't associated with blocks/items, and therefore don't have a {@link ModelIdentifier}.
     * @param id the id of the model to fetch
     * @return the model itself
     */
    public static @Nullable BakedModel getModel(Identifier id) {
        return ((BakedModelManagerAccessor) MinecraftClient.getInstance().getBakedModelManager()).getModels().get(id);
    }

    public static @Nullable Mesh load(Identifier modelPath, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings bakeSettings, boolean isBlock) {
        return dev.monarkhes.myron.impl.client.Myron.load(modelPath, textureGetter, bakeSettings, isBlock);
    }
}
