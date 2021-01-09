package dev.monarkhes.myron.api;

import dev.monarkhes.myron.impl.mixin.BakedModelManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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
}
