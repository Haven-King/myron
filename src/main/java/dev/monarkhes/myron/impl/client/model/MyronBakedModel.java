package dev.monarkhes.myron.impl.client.model;

import dev.monarkhes.myron.impl.client.Myron;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class MyronBakedModel implements BakedModel, FabricBakedModel {
    private final Mesh mesh;
    private final ModelTransformation transformation;
    private final Sprite sprite;
    private final boolean isSideLit;

    private List<BakedQuad> backupQuads = null;

    public MyronBakedModel(Mesh mesh, ModelTransformation transformation, Sprite sprite, boolean isSideLit) {
        this.mesh = mesh;
        this.transformation = transformation;
        this.sprite = sprite;
        this.isSideLit = isSideLit;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
        if (this.mesh != null) {
            renderContext.meshConsumer().accept(mesh);
        } else {
            Myron.LOGGER.warn("Mesh is null while emitting block quads for block {}", blockState.getBlock().getName().asString());
        }
    }

    @Override
    public void emitItemQuads(ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        if (this.mesh != null) {
            renderContext.meshConsumer().accept(mesh);
        } else {
            Myron.LOGGER.warn("Mesh is null while emitting block quads for item {}", itemStack.getItem().getName().asString());
        }
    }

    // Since FabricBakedModels defer to use `emitBlockQuads` and `emitItemQuads`, this will only be called if
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        if (this.backupQuads == null) {
            this.backupQuads = new ArrayList<>();

            mesh.forEach(quadView -> {
                this.backupQuads.add(quadView.toBakedQuad(0, this.sprite, false));
            });
        }

        return this.backupQuads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return this.isSideLit;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return this.sprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.transformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}
