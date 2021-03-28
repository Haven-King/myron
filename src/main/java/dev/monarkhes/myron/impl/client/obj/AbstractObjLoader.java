package dev.monarkhes.myron.impl.client.obj;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import dev.monarkhes.myron.impl.client.Myron;
import dev.monarkhes.myron.impl.client.model.MyronMaterial;
import dev.monarkhes.myron.impl.client.model.MyronUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbstractObjLoader {
    public static final SpriteIdentifier DEFAULT_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, null);

    protected @Nullable UnbakedModel loadModel(
            ResourceManager resourceManager, Identifier identifier, ModelTransformation transformation, boolean isSideLit) {
        if (!identifier.getPath().endsWith(".obj")) {
            identifier = new Identifier(identifier.getNamespace(), identifier.getPath() + ".obj");
        }

        if (!identifier.getPath().startsWith("models/")) {
            identifier = new Identifier(identifier.getNamespace(), "models/" + identifier.getPath());
        }

        if (resourceManager.containsResource(identifier)) {
            try {

                InputStream inputStream = resourceManager.getResource(identifier).getInputStream();
                Obj obj = ObjReader.read(inputStream);
                Map<String, MyronMaterial> materials = new LinkedHashMap<>();

                for (String s : obj.getMtlFileNames()) {
                    String path = identifier.getPath();
                    path = path.substring(0, path.lastIndexOf('/') + 1) + s;
                    Identifier resource = new Identifier(identifier.getNamespace(), path);

                    if (resourceManager.containsResource(resource)) {
                        MaterialReader.read(
                                new BufferedReader(
                                    new InputStreamReader(resourceManager.getResource(resource).getInputStream())))
                            .forEach(material -> materials.put(material.name, material));
                    } else {
                        Myron.LOGGER.warn("Texture does not exist: {}", resource);
                    }
                }

                Collection<SpriteIdentifier> textureDependencies = new HashSet<>();

                for (MyronMaterial material : materials.values()) {
                    textureDependencies.add(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, material.getTexture()));
                }

                MyronMaterial material = materials.get("sprite");
                return new MyronUnbakedModel(textureDependencies, materials.size() > 0
                        ? new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, (material == null
                        ? materials.values().iterator().next()
                        : material).getTexture())
                        : DEFAULT_SPRITE, transformation, isSideLit);
            } catch (IOException e) {
                Myron.LOGGER.warn("Failed to load model {}:\n{}", identifier, e.getMessage());
            }
        }

        return null;
    }
}
