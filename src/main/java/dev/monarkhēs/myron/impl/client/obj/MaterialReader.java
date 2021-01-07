package dev.monarkhēs.myron.impl.client.obj;

import dev.monarkhēs.myron.impl.client.model.MyronMaterial;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class MaterialReader {
    private static final Map<String, Option> OPTIONS = new HashMap<>();
    private static final Option NONE = (tokenizer, line, token, material) -> {};

    static {
        register((tokenizer, line, key, material) ->
                material.setTexture(new Identifier(line.substring(key.length()).trim())),
                "map_Kd", "texture");

        register((tokenizer, line, key, material) ->
                        material.setColor(
                                parseFloat(tokenizer.nextToken()),
                                parseFloat(tokenizer.nextToken()),
                                parseFloat(tokenizer.nextToken())
                        ),
                "Kd");

        register((tokenizer, line, key, material) ->
                        material.setBlendMode(BlendMode.valueOf(tokenizer.nextToken().toUpperCase(Locale.ROOT))),
                "blend_mode");

        register((tokenizer, line, key, material) ->
                        material.setColorIndex(parseBoolean(tokenizer.nextToken())),
                "tintindex");

        register((tokenizer, line, key, material) ->
                        material.setDiffuseShading(parseBoolean(tokenizer.nextToken())),
                "diffuse_shading");

        register((tokenizer, line, key, material) ->
                        material.setAmbientOcclusion(parseBoolean(tokenizer.nextToken())),
                "ambient_occlusion", "ao");

        register((tokenizer, line, key, material) ->
                        material.setColor(parseInt(tokenizer.nextToken(), 16)),
                "diffuse_color", "color");

        register((tokenizer, line, key, material) ->
                        material.setEmissive(parseBoolean(tokenizer.nextToken())),
                "emission", "emissive");

        register((tokenizer, line, key, material) ->
                        material.lockUv(parseBoolean(tokenizer.nextToken())),
                "uvlock");

        register((tokenizer, line, key, material) ->
                        material.setTintIndex(parseInt(tokenizer.nextToken(), 10)),
                "tint_index", "tintindex");

        register((tokenizer, line, key, material) ->
                        material.cull(Direction.valueOf(tokenizer.nextToken().toUpperCase(Locale.ROOT))),
                "cull", "cullface");
    }

    private MaterialReader() {
    }

    public static void register(Option option, String... tokens) {
        for (String key : tokens) {
            OPTIONS.putIfAbsent(key.toLowerCase(Locale.ROOT), option);
        }
    }

    public static List<MyronMaterial> read(BufferedReader reader) throws IOException {
        List<MyronMaterial> materials = new ArrayList<>();

        MyronMaterial currentMaterial = null;

        for (String line = reader.readLine(); line != null; line = next(reader)) {
            int comment = line.indexOf('#');

            if (comment > 0) {
                line = line.substring(0, comment);
            }

            StringTokenizer tokenizer = new StringTokenizer(line);

            if (!tokenizer.hasMoreTokens()) continue;

            String token = tokenizer.nextToken().toLowerCase(Locale.ROOT);

            if (token.equals("newmtl")) {
                String name = line.substring("newmtl".length()).trim();
                currentMaterial = new MyronMaterial(name);
                materials.add(currentMaterial);
            }

            if (currentMaterial != null) {
                OPTIONS.getOrDefault(token, NONE).parse(tokenizer, line, token, currentMaterial);
            }
        }

        return materials;
    }

    private static String next(BufferedReader reader) throws IOException {
        String line = trim(reader.readLine());

        if (line == null) return null;

        StringBuilder result = new StringBuilder(line);

        while (line != null && line.endsWith("\\")) {
            line = trim(reader.readLine());

            if (line != null) {
                result.append(" ").append(line);
            }
        }

        return result.toString();
    }

    private static String trim(String line) {
        return line == null ? null : line.trim();
    }

    /**
     * Parse a float from the given string, wrapping number format
     * exceptions into an IOException
     *
     * @param s The string
     * @return The float
     * @throws IOException If the string does not contain a valid float value
     */
    private static float parseFloat(String s) throws IOException
    {
        try
        {
            return Float.parseFloat(s);
        }
        catch (NumberFormatException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Parse an int from the given string, wrapping number format
     * exceptions into an IOException
     *
     * @param s The string
     * @param radix the radix to be used while parsing {@code s}.
     * @return The int
     * @throws IOException If the string does not contain a valid float value
     */
    private static int parseInt(String s, int radix) throws IOException
    {
        if (radix == 16 && s.startsWith("0x")) {
            s = s.substring(2);
        }

        try
        {
            return Integer.parseInt(s, radix);
        }
        catch (NumberFormatException e)
        {
            throw new IOException(e);
        }
    }

    private static boolean parseBoolean(String s) {
        return s.equalsIgnoreCase("true");
    }

    @FunctionalInterface
    public interface Option {
        void parse(StringTokenizer tokenizer, String line, String key, MyronMaterial material) throws IOException;
    }
}
