package dev.monarkhēs.myron.impl.client;

import dev.monarkhēs.myron.impl.client.obj.ObjLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Myron implements ClientModInitializer {
    public static final String MOD_ID = "myron";
    public static final Logger LOGGER = LogManager.getLogger("Myron");

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(ObjLoader::new);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(ObjLoader::new);
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            manager.findResources("models", path -> path.endsWith(".obj")).forEach(out);
        });

        LOGGER.info("Myron Initialized!");
    }
}
