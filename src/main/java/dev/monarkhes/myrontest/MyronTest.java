package dev.monarkhes.myrontest;

import dev.monarkhēs.myron.impl.client.Myron;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MyronTest implements ModInitializer, ClientModInitializer {
    public static final Block CRYSTAL_ORE = new Block(FabricBlockSettings.of(Material.STONE));
    public static final Block TORUS = new TorusBlock(FabricBlockSettings.of(Material.METAL).nonOpaque());

    public static final BlockEntityType<TorusBlockEntity> TORUS_BLOCK_ENTITY = BlockEntityType.Builder.create(TorusBlockEntity::new, TORUS).build(null);

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "crystal_ore"), CRYSTAL_ORE);
        Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "crystal_ore"), new BlockItem(CRYSTAL_ORE, new Item.Settings()));
        Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "torus"), TORUS);
        Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "torus"), new BlockItem(TORUS, new Item.Settings()));

        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Myron.MOD_ID, "torus"), TORUS_BLOCK_ENTITY);
}

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(TORUS_BLOCK_ENTITY, TorusBlockEntityRenderer::new);
    }
}
