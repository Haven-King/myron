package dev.monarkhēs.myron.impl.client.obj;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import dev.monarkhēs.myron.impl.client.Myron;
import dev.monarkhēs.myron.impl.client.model.MyronMaterial;
import dev.monarkhēs.myron.impl.client.model.MyronUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbstractObjLoader {
    protected final static Vector3f ORIGIN = new Vector3f();
    protected final static Vector3f BLOCKS = new Vector3f(0.5F, 0.5F, 0.5F);

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

                return new MyronUnbakedModel(obj, materials, transformation, isSideLit,
                        identifier.getPath().startsWith("models/block")
                                ? BLOCKS
                                : ORIGIN);
            } catch (IOException e) {
                Myron.LOGGER.warn("Failed to load model {}:\n{}", identifier, e.getMessage());
            }
        }

        return null;
    }
}
