package dev.monarkhēs.myron.impl.client.obj;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import dev.monarkhēs.myron.impl.client.Myron;
import dev.monarkhēs.myron.impl.client.model.MyronMaterial;
import dev.monarkhēs.myron.impl.client.model.MyronUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
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
    protected @Nullable UnbakedModel loadModel(ResourceManager resourceManager, Identifier identifier, ModelTransformation transformation, boolean isSideLit) {
        if (identifier.getPath().endsWith(".obj")) {
            try {
                if (!identifier.getPath().startsWith("models/")) {
                    identifier = new Identifier(identifier.getNamespace(), "models/" + identifier.getPath());
                }

                InputStream inputStream = resourceManager.getResource(identifier).getInputStream();
                Obj obj = ObjReader.read(inputStream);
                Map<String, MyronMaterial> materials = new LinkedHashMap<>();

                for (String s : obj.getMtlFileNames()) {
                    String path = identifier.getPath();
                    path = path.substring(0, path.lastIndexOf('/') + 1) + s;
                    Identifier resource = new Identifier(identifier.getNamespace(), path);

                    if (resourceManager.containsResource(resource)) {
                        MaterialReader.read(new BufferedReader(new InputStreamReader(resourceManager.getResource(resource).getInputStream()))).forEach(material -> {
                            materials.put(material.name, material);
                        });
                    } else {
                        Myron.LOGGER.warn("Texture does not exist: {}", resource);
                    }
                }

                return new MyronUnbakedModel(ObjUtils.triangulate(obj), materials, transformation, isSideLit);
            } catch (IOException e) {
                Myron.LOGGER.warn("Failed to load model {}:\n{}", identifier, e.getMessage());
            }
        }

        return null;
    }
}
