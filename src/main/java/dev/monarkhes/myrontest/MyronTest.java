package dev.monarkhes.myrontest;

import dev.monarkhēs.myron.impl.client.Myron;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Random;

public class MyronTest implements ModInitializer, ClientModInitializer {
    private static final Random RANDOM = new Random();

    public static final Block CRYSTALS = new CrystalBlock(FabricBlockSettings.of(Material.STONE).hardness(1.0f).nonOpaque());
    public static final Block CONE_BLOCK = new PointerBlock(FabricBlockSettings.of(Material.STONE).hardness(1F).nonOpaque());
    public static final Block CRYSTAL_ORE = new Block(FabricBlockSettings.of(Material.STONE));
    public static final Block ANIM_TEST = new AnimTestBlock(FabricBlockSettings.of(Material.METAL));
    public static final Block CHEF_TEST = new ChefTestBlock(FabricBlockSettings.of(Material.AGGREGATE));
    public static final Block TORUS = new TaurusBlock(FabricBlockSettings.of(Material.METAL).nonOpaque());

    public static final BlockEntityType<TorusBlockEntity> TORUS_BLOCK_ENTITY = BlockEntityType.Builder.create(TorusBlockEntity::new, TORUS).build(null);

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "crystals"), CRYSTALS);
            Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "crystals"), new BlockItem(CRYSTALS, new Item.Settings()));
            Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "cone"), CONE_BLOCK);
            Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "cone"), new BlockItem(CONE_BLOCK, new Item.Settings()));
            Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "crystal_ore"), CRYSTAL_ORE);
            Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "crystal_ore"), new BlockItem(CRYSTAL_ORE, new Item.Settings()));
            Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "anim_test"), ANIM_TEST);
            Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "anim_test"), new BlockItem(ANIM_TEST, new Item.Settings()));
            Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "chef_test"), CHEF_TEST);
            Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "chef_test"), new BlockItem(CHEF_TEST, new Item.Settings()));
            Registry.register(Registry.BLOCK, new Identifier(Myron.MOD_ID, "torus"), TORUS);
            Registry.register(Registry.ITEM, new Identifier(Myron.MOD_ID, "torus"), new BlockItem(TORUS, new Item.Settings()));

            Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(Myron.MOD_ID, "torus"), TORUS_BLOCK_ENTITY);
        }
    }

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            BlockEntityRendererRegistry.INSTANCE.register(TORUS_BLOCK_ENTITY, TorusBlockEntityRenderer::new);

            ColorProviderRegistry.BLOCK.register(((blockState, blockRenderView, blockPos, i) -> {
                if (blockPos == null) return -1;

                RANDOM.setSeed(blockPos.asLong());

                return (RANDOM.nextInt(0x80) + 0x80) << 16
                        | (RANDOM.nextInt(0x80) + 0x80) << 8
                        | (RANDOM.nextInt(0x80) + 0x80);
            }), CRYSTALS);
        }
    }
}
