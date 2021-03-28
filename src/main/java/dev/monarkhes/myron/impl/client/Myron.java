package dev.monarkhes.myron.impl.client;

import de.javagl.obj.*;
import dev.monarkhes.myron.impl.client.model.MyronMaterial;
import dev.monarkhes.myron.impl.client.obj.MaterialReader;
import dev.monarkhes.myron.impl.client.obj.ObjLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class Myron implements ClientModInitializer {
    private final static Vector3f NONE = new Vector3f();
    private final static Vector3f BLOCKS = new Vector3f(0.5F, 0.5F, 0.5F);

    public static final String MOD_ID = "myron";
    public static final Logger LOGGER = LogManager.getLogger("Myron");
    public static final Map<Identifier, Mesh> MESHES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(ObjLoader::new);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(ObjLoader::new);
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            Collection<Identifier> ids = new HashSet<>();

            Collection<Identifier> candidates = new ArrayList<>();
            candidates.addAll(manager.findResources("models/block", path -> true));
            candidates.addAll(manager.findResources("models/item", path -> true));
            candidates.addAll(manager.findResources("models/misc", path -> true));

            for (Identifier id : candidates) {
                if (id.getPath().endsWith(".obj")) {
                    ids.add(id);
                    ids.add(new Identifier(id.getNamespace(), id.getPath().substring(0, id.getPath().indexOf(".obj"))));
                } else {
                    Identifier test = new Identifier(id.getNamespace(), id.getPath() + ".obj");

                    if (manager.containsResource(test)) {
                        ids.add(id);
                    }
                }
            }

            ids.forEach(
                id -> {
                    String path = id.getPath();

                    if (path.startsWith("models/")) {
                        out.accept(new Identifier(id.getNamespace(), path.substring("models/".length())));
                    }

                    out.accept(id);
                }
            );
        });

        LOGGER.info("Myron Initialized!");
    }

    public static @Nullable Mesh load(Identifier modelPath, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings bakeSettings, boolean isBlock) {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();

        if (!modelPath.getPath().endsWith(".obj")) {
            modelPath = new Identifier(modelPath.getNamespace(), modelPath.getPath() + ".obj");
        }

        if (!modelPath.getPath().startsWith("models/")) {
            modelPath = new Identifier(modelPath.getNamespace(), "models/" + modelPath.getPath());
        }

        if (resourceManager.containsResource(modelPath)) {
            try {
                InputStream inputStream = resourceManager.getResource(modelPath).getInputStream();
                Obj obj = ObjReader.read(inputStream);

                Map<String, MyronMaterial> materials = getMaterials(resourceManager, modelPath, obj);
                return build(obj, materials, textureGetter, bakeSettings, isBlock);
            } catch (IOException e) {
                Myron.LOGGER.warn("Failed to load model {}:\n{}", modelPath, e.getMessage());
            }
        }

        return null;
    }

    private static Map<String, MyronMaterial> getMaterials(ResourceManager resourceManager, Identifier identifier, Obj obj) throws IOException {
        Map<String, MyronMaterial> materials = new LinkedHashMap<>();

        for (String s : obj.getMtlFileNames()) {
            String path = identifier.getPath();
            path = path.substring(0, path.lastIndexOf('/') + 1) + s;
            Identifier resource = new Identifier(identifier.getNamespace(), path);

            if (resourceManager.containsResource(resource)) {
                MaterialReader.read(new BufferedReader(
                        new InputStreamReader(resourceManager.getResource(resource).getInputStream())))
                        .forEach(material -> materials.put(material.name, material));
            } else {
                Myron.LOGGER.warn("Texture does not exist: {}", resource);
            }
        }

        return materials;
    }

    public static @Nullable Mesh build(Obj obj, Map<String, MyronMaterial> materials, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings bakeSettings, boolean isBlock) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        if (renderer == null) return null;

        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        for (Map.Entry<String, Obj> entry : ObjSplitting.splitByMaterialGroups(obj).entrySet()) {
            Obj group = entry.getValue();
            MyronMaterial material = materials.get(entry.getKey());

            if (material == null) {
                material = MyronMaterial.DEFAULT;
            }

            int materialColor = material.getColor();
            Sprite sprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, material.getTexture()));

            for (int faceIndex = 0; faceIndex < group.getNumFaces(); ++faceIndex) {
                face(renderer, emitter, group, group.getFace(faceIndex), material, materialColor, sprite, bakeSettings, isBlock);
            }
        }

        return builder.build();
    }

    private static void face(Renderer renderer, QuadEmitter emitter, Obj group, ObjFace face, MyronMaterial material, int materialColor, Sprite sprite, ModelBakeSettings settings, boolean isBlock) {
        if (face.getNumVertices() <= 4) {
            for (int vertex = 0; vertex < face.getNumVertices(); ++vertex) {
                vertex(emitter, group, face, vertex, settings, isBlock);
            }

            emit(renderer, emitter, material, materialColor, sprite, settings);
        } else {
            final int vertices = face.getNumVertices();

            FloatTuple textureCoords = face.containsTexCoordIndices()
                    ? group.getTexCoord(face.getTexCoordIndex(0))
                    : null;

            Vector3f pos = of(group.getVertex(face.getVertexIndex(0)));
            pos.add(isBlock ? BLOCKS : NONE);
            Vector3f normal = of(group.getNormal(face.getNormalIndex(0)));

            rotate(settings, pos, normal);

            final Vertex start = new Vertex(
                    pos,
                    normal,
                    textureCoords == null ? 0 : textureCoords.getX(),
                    textureCoords == null ? 0 : textureCoords.getY()
            );

            for (int vertex = 1; vertex < vertices - 1; ++vertex) {
                vertex(emitter, 0, start.pos, start.normal, start.u, start.v);

                textureCoords = face.containsTexCoordIndices()
                        ? group.getTexCoord(face.getTexCoordIndex(vertex))
                        : null;

                pos = of(group.getVertex(face.getVertexIndex(vertex)));
                pos.add(isBlock ? BLOCKS : NONE);
                normal = of(group.getNormal(face.getNormalIndex(vertex)));

                rotate(settings, pos, normal);

                vertex(emitter, 1,
                        pos,
                        normal,
                        textureCoords == null ? 0 : textureCoords.getX(),
                        textureCoords == null ? 0 : textureCoords.getY()
                );

                textureCoords = face.containsTexCoordIndices()
                        ? group.getTexCoord(face.getTexCoordIndex(vertex + 1))
                        : null;

                pos = of(group.getVertex(face.getVertexIndex(vertex + 1)));
                pos.add(isBlock ? BLOCKS : NONE);
                normal = of(group.getNormal(face.getNormalIndex(vertex + 1)));

                rotate(settings, pos, normal);


                vertex(emitter, 2,
                        pos,
                        normal,
                        textureCoords == null ? 0 : textureCoords.getX(),
                        textureCoords == null ? 0 : textureCoords.getY()
                );

                vertex(emitter, 3,
                        pos,
                        normal,
                        textureCoords == null ? 0 : textureCoords.getX(),
                        textureCoords == null ? 0 : textureCoords.getY()
                );

                emit(renderer, emitter, material, materialColor, sprite, settings);
            }
        }
    }

    private static void emit(Renderer renderer, QuadEmitter emitter, MyronMaterial material, int materialColor, Sprite sprite, ModelBakeSettings settings) {
        emitter.material(material.getMaterial(renderer));
        emitter.spriteColor(0, materialColor, materialColor, materialColor, materialColor);
        emitter.colorIndex(material.getTintIndex());
        emitter.nominalFace(emitter.lightFace());

        if (material.getCullDirection() != null) {
            emitter.cullFace(material.getCullDirection());
        }

        boolean bl = settings.isShaded() || material.isUvLocked();
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED | (bl ? MutableQuadView.BAKE_LOCK_UV : 0));
        emitter.emit();
    }

    private static void vertex(QuadEmitter emitter, Obj group, ObjFace face, int vertex, ModelBakeSettings settings, boolean isBlock) {
        Vector3f pos = of(group.getVertex(face.getVertexIndex(vertex)));

        // Used to offset blocks
        pos.add(isBlock ? BLOCKS : NONE);

        Vector3f normal = face.containsNormalIndices()
                ? of(group.getNormal(face.getNormalIndex(vertex)))
                : calculateNormal(group, face);

        float u = 0, v = 0;
        if (face.containsTexCoordIndices()) {
            FloatTuple textureCoords = group.getTexCoord(face.getTexCoordIndex(vertex));
            u = textureCoords.getX();
            v = 1F - textureCoords.getY();

            u = u > 1 || u < 0 ? (((u % 1F) + 1F) % 1F) : u;
            v = v > 1 || v < 0 ? (((v % 1F) + 1F) % 1F) : v;
        }

        rotate(settings, pos, normal);

        vertex(emitter, vertex, pos, normal, u, v);

        if (face.getNumVertices() == 3) {
            vertex(emitter, vertex + 1, pos, normal, u, v);
        }
    }

    private static Vector3f calculateNormal(Obj group, ObjFace face) {
        Vector3f p1 = of(group.getVertex(face.getVertexIndex(0)));
        Vector3f v1 = of(group.getVertex(face.getVertexIndex(1)));
        Vector3f v2 = of(group.getVertex(face.getVertexIndex(2)));

        v1.subtract(p1);
        v2.subtract(p1);

        return new Vector3f(
                v1.getY() * v2.getZ() - v1.getZ() * v2.getY(),
                v1.getZ() * v2.getX() - v1.getX() * v2.getZ(),
                v1.getX() * v2.getY() - v1.getY() * v2.getX()
        );
    }

    private static void rotate(ModelBakeSettings settings, Vector3f pos, Vector3f normal) {
        if (settings.getRotation() != AffineTransformation.identity()) {
            pos.add(-0.5F, -0.5F, -0.5F);
            pos.rotate(settings.getRotation().getRotation2());
            pos.add(0.5f, 0.5f, 0.5f);

            normal.rotate(settings.getRotation().getRotation2());
        }
    }

    private static void vertex(QuadEmitter emitter, int vertex, Vector3f pos, Vector3f normal, float u, float v) {
        emitter.pos(vertex, pos);
        emitter.normal(vertex, normal);
        emitter.sprite(vertex, 0, u, v);
    }

    private static Vector3f of(FloatTuple tuple) {
        return new Vector3f(tuple.getX(), tuple.getY(), tuple.getZ());
    }

    private static class Vertex {
        public final Vector3f pos;
        public final Vector3f normal;
        public final float u;
        public final float v;

        private Vertex(Vector3f pos, Vector3f normal, float u, float v) {
            this.pos = pos;
            this.normal = normal;
            this.u = u;
            this.v = v;
        }
    }
}
