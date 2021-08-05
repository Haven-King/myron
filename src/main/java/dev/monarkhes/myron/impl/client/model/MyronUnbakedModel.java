package dev.monarkhes.myron.impl.client.model;

import com.mojang.datafixers.util.Pair;
import de.javagl.obj.Obj;
import dev.monarkhes.myron.impl.client.Myron;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MyronUnbakedModel implements UnbakedModel {
    private final Obj obj;
    private final Map<String, MyronMaterial> materials;
    private final Collection<SpriteIdentifier> textureDependencies;
    private final SpriteIdentifier sprite;
    private final ModelTransformation transform;
    private final boolean isSideLit;

    public MyronUnbakedModel(@Nullable Obj obj, @Nullable Map<String, MyronMaterial> materials, Collection<SpriteIdentifier> textureDependencies, SpriteIdentifier sprite, ModelTransformation modelTransformation, boolean isSideLit) {
        this.obj = obj;
        this.materials = materials;
        this.textureDependencies = textureDependencies;
        this.sprite = sprite;
        this.transform = modelTransformation;
        this.isSideLit = isSideLit;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return this.textureDependencies;
    }

    @Override
    public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings bakeSettings, Identifier modelId) {
        Mesh mesh;
        boolean isBlock = modelId.getPath().startsWith("block");

        if (obj == null)
            // Try to load the obj (previous behavior)
            mesh = Myron.load(modelId, textureGetter, bakeSettings, isBlock);
        else
            // We already loaded the obj earlier in AbstractObjLoader. Don't use the external utility to re-load the obj
            // (it works only on absolute identifiers, not ModelIdentifiers like 'myron:torus#inventory')
            mesh = Myron.build(obj, materials, textureGetter, bakeSettings, isBlock);

        Myron.MESHES.put(modelId, mesh);

        return new MyronBakedModel(mesh, this.transform, textureGetter.apply(this.sprite), this.isSideLit);
    }
}
