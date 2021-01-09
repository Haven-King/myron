package dev.monarkhes.myron.impl.client;

import dev.monarkhes.myron.impl.client.obj.ObjLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Environment(EnvType.CLIENT)
public class Myron implements ClientModInitializer {
    public static final String MOD_ID = "myron";
    public static final Logger LOGGER = LogManager.getLogger("Myron");

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
}
