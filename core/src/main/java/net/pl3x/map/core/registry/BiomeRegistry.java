package net.pl3x.map.core.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.management.openmbean.KeyAlreadyExistsException;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.Biome;
import net.pl3x.map.core.world.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BiomeRegistry extends Registry<Biome> {
    private static final Gson GSON = new GsonBuilder()
            //.setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .setLenient()
            .create();

    @NonNull
    public Biome register(@NonNull String id, int color, int foliage, int grass, int water, Biome.@NonNull GrassModifier grassModifier) {
        if (has(id)) {
            throw new KeyAlreadyExistsException("Biome already registered: " + id);
        }
        return register(id, new Biome(size(), id, color, foliage, grass, water, grassModifier));
    }

    @Override
    @NonNull
    public Biome get(@NonNull String id) {
        return getOrDefault(id, Biome.DEFAULT);
    }

    public void saveToDisk(@NonNull World world) {
        Map<Integer, String> map = new HashMap<>();
        values().forEach(biome -> map.put(biome.index(), biome.id()));
        try {
            FileUtil.saveGzip(GSON.toJson(map), world.getTilesDirectory().resolve("biomes.gz"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}