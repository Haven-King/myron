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
import net.minecraft.world.gen.random.AbstractRandom;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<AbstractRandom> supplier, RenderContext ctx) {
        if (this.mesh != null) {
            ctx.meshConsumer().accept(mesh);
        } else {
            Myron.LOGGER.warn("Mesh is null while emitting block quads for block {}", state.getBlock().getName().asString());
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<AbstractRandom> supplier, RenderContext ctx) {
        if (this.mesh != null) {
            ctx.meshConsumer().accept(mesh);
        } else {
            Myron.LOGGER.warn("Mesh is null while emitting block quads for item {}", stack.getItem().getName().asString());
        }
    }

    // Since FabricBakedModels defer to use `emitBlockQuads` and `emitItemQuads`, this will only be called if
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, AbstractRandom random) {
        if (this.backupQuads == null) {
            this.backupQuads = new ArrayList<>();

            mesh.forEach(quadView -> this.backupQuads.add(quadView.toBakedQuad(0, this.sprite, false)));
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
    public Sprite getParticleSprite() {
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
