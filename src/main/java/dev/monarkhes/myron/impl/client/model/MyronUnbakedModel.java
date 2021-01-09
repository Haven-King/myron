package dev.monarkhes.myron.impl.client.model;

import com.mojang.datafixers.util.Pair;
import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjSplitting;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class MyronUnbakedModel implements UnbakedModel {
    public static final SpriteIdentifier DEFAULT_SPRITE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, null);

    private final Obj obj;
    private final Map<String, MyronMaterial> materials;
    private final ModelTransformation transform;
    private final Collection<SpriteIdentifier> textureDependencies = new HashSet<>();
    private final SpriteIdentifier sprite;
    private final boolean isSideLit;
    private final Vector3f offset;

    public MyronUnbakedModel(Obj obj, Map<String, MyronMaterial> materials, ModelTransformation transform, boolean isSideLit, Vector3f offset) {
        this.obj = obj;
        this.materials = materials;
        this.transform = transform;
        this.isSideLit = isSideLit;
        this.offset = offset;

        for (MyronMaterial material : materials.values()) {
            this.textureDependencies.add(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, material.getTexture()));
        }

        MyronMaterial material = this.getMaterial("sprite");
        this.sprite = materials.size() > 0
                ? new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, (material == null
                        ? materials.values().iterator().next()
                        : material).getTexture())
                : DEFAULT_SPRITE;

    }

    private MyronMaterial getMaterial(String name) {
        return this.materials.get(name);
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
    public @Nullable BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier modelId) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        if (renderer == null) return null;

        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        for (Map.Entry<String, Obj> entry : ObjSplitting.splitByMaterialGroups(this.obj).entrySet()) {
            Obj group = entry.getValue();
            MyronMaterial material = this.getMaterial(entry.getKey());

            if (material == null) {
                material = MyronMaterial.DEFAULT;
            }

            int materialColor = material.getColor();
            Sprite sprite = textureGetter.apply(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, material.getTexture()));

            for (int faceIndex = 0; faceIndex < group.getNumFaces(); ++faceIndex) {
                face(renderer, emitter, group, group.getFace(faceIndex), material, materialColor, sprite, settings);
            }
        }

        return new MyronBakedModel(builder.build(), this.transform, textureGetter.apply(this.sprite), this.isSideLit);
    }

    private void face(Renderer renderer, QuadEmitter emitter, Obj group, ObjFace face, MyronMaterial material, int materialColor, Sprite sprite, ModelBakeSettings settings) {
        if (face.getNumVertices() <= 4) {
            for (int vertex = 0; vertex < face.getNumVertices(); ++vertex) {
                vertex(emitter, group, face, vertex, settings);
            }

            emit(renderer, emitter, material, materialColor, sprite, settings);
        } else {
            final int vertices = face.getNumVertices();

            FloatTuple textureCoords = face.containsTexCoordIndices()
                    ? group.getTexCoord(face.getTexCoordIndex(0))
                    : null;

            Vector3f pos = of(group.getVertex(face.getVertexIndex(0)));
            pos.add(this.offset);
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
                pos.add(this.offset);
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
                pos.add(this.offset);
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

    private void emit(Renderer renderer, QuadEmitter emitter, MyronMaterial material, int materialColor, Sprite sprite, ModelBakeSettings settings) {
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

    private void vertex(QuadEmitter emitter, Obj group, ObjFace face, int vertex, ModelBakeSettings settings) {
        Vector3f pos = of(group.getVertex(face.getVertexIndex(vertex)));

        // Used to offset blocks
        pos.add(this.offset);
        Vector3f normal = of(group.getNormal(face.getNormalIndex(vertex)));

        float u = 0, v = 0;
        if (face.containsTexCoordIndices()) {
            FloatTuple textureCoords = group.getTexCoord(face.getTexCoordIndex(vertex));
            u = textureCoords.getX();
            v = textureCoords.getY();
        }

        rotate(settings, pos, normal);

        vertex(emitter, vertex, pos, normal, u, v);

        if (face.getNumVertices() == 3) {
            vertex(emitter, vertex + 1, pos, normal, u, v);
        }
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
