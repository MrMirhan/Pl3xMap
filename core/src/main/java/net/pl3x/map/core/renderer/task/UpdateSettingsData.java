/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.pl3x.map.core.renderer.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.configuration.Config;
import net.pl3x.map.core.configuration.Lang;
import net.pl3x.map.core.configuration.WorldConfig;
import net.pl3x.map.core.image.io.IO;
import net.pl3x.map.core.log.Logger;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.scheduler.Task;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.World;
import org.jetbrains.annotations.NotNull;

public class UpdateSettingsData extends Task {
    private int fileTick;
    private final Gson gson = new GsonBuilder()
            //.setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .setLenient()
            .create();
    private int jsonHashCache = -1;
    private final ExecutorService executor;

    private CompletableFuture<Void> future;
    private boolean running;

    public UpdateSettingsData() {
        super(1, true);
        this.executor = Pl3xMap.ThreadFactory.createService("Pl3xMap-Settings");
    }

    @Override
    public void cancel() {
        super.cancel();
        if (this.future != null) {
            this.future.cancel(true);
        }
    }

    @Override
    public void run() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.future = CompletableFuture.runAsync(() -> {
            try {
                parseSettings();
            } catch (Throwable t) {
                Logger.severe("Failed to parse settings.json", t);
            }
            this.running = false;
        }, this.executor);
    }

    private @NotNull List<@NotNull Map<@NotNull String, @NotNull Object>> parseWorlds() {
        List<Map<String, Object>> worldSettings = new ArrayList<>();
        Pl3xMap.api().getWorldRegistry().entrySet().forEach(entry -> {
            World world = entry.getValue();
            if (!world.isEnabled()) {
                return;
            }

            WorldConfig config = world.getConfig();

            Map<String, Object> spawn = new LinkedHashMap<>();
            Point point = world.getSpawn();
            spawn.put("x", point.x());
            spawn.put("z", point.z());

            Map<String, Object> center = new LinkedHashMap<>();
            center.put("x", config.CENTER_X);
            center.put("z", config.CENTER_Z);

            Map<String, Object> zoom = new LinkedHashMap<>();
            zoom.put("default", config.ZOOM_DEFAULT);
            zoom.put("maxOut", config.ZOOM_MAX_OUT);
            zoom.put("maxIn", config.ZOOM_MAX_IN);

            Map<String, Object> ui = new LinkedHashMap<>();
            ui.put("link", config.UI_LINK);
            ui.put("coords", config.UI_COORDS);
            ui.put("blockinfo", config.UI_BLOCKINFO);
            ui.put("attribution", config.UI_ATTRIBUTION);

            Map<String, Object> settings = new LinkedHashMap<>();
            settings.put("name", world.getName().replace(":", "-"));
            settings.put("tileUpdateInterval", 10);
            settings.put("spawn", spawn);
            settings.put("center", center);
            settings.put("zoom", zoom);
            settings.put("ui", ui);

            FileUtil.writeJson(this.gson.toJson(settings), world.getTilesDirectory().resolve("settings.json"));

            List<Object> renderers = new ArrayList<>();
            world.getRenderers().forEach((rendererKey, builder) -> {
                String icon = world.getConfig().RENDER_RENDERERS.get(rendererKey);
                renderers.add(Map.of("label", rendererKey, "value", builder.getName(), "icon", icon));
            });

            Map<String, Object> worldsList = new LinkedHashMap<>();
            worldsList.put("name", world.getName().replace(":", "-"));
            worldsList.put("displayName", config.DISPLAY_NAME
                    .replace("<world>", world.getName()));
            worldsList.put("type", world.getType().toString());
            worldsList.put("order", config.ORDER);
            worldsList.put("renderers", renderers);
            worldSettings.add(worldsList);
        });

        // sort worlds by order, then by name
        worldSettings.sort(Comparator.<Map<String, Object>>comparingInt(w -> (int) w.get("order")).thenComparing(w -> (String) w.get("name")));

        return worldSettings;
    }

    private void parseSettings() {
        Map<String, Object> lang = new LinkedHashMap<>();
        lang.put("title", Lang.UI_TITLE);
        lang.put("langFile", Lang.UI_BLOCK_AND_BIOME_LANG_FILE);
        lang.put("blockInfo", Map.of(
                "label", Lang.UI_BLOCKINFO_LABEL,
                "value", Lang.UI_BLOCKINFO_VALUE,
                "unknown", Map.of("block", Lang.UI_BLOCKINFO_UNKNOWN_BLOCK, "biome", Lang.UI_BLOCKINFO_UNKNOWN_BIOME))
        );
        lang.put("coords", Map.of("label", Lang.UI_COORDS_LABEL, "value", Lang.UI_COORDS_VALUE));
        lang.put("layers", Map.of("label", Lang.UI_LAYERS_LABEL, "value", Lang.UI_LAYERS_VALUE));
        lang.put("link", Map.of("label", Lang.UI_LINK_LABEL, "value", Lang.UI_LINK_VALUE));
        lang.put("markers", Map.of("label", Lang.UI_MARKERS_LABEL, "value", Lang.UI_MARKERS_VALUE));
        lang.put("players", Map.of("label", Lang.UI_PLAYERS_LABEL, "value", Lang.UI_PLAYERS_VALUE));
        lang.put("worlds", Map.of("label", Lang.UI_WORLDS_LABEL, "value", Lang.UI_WORLDS_VALUE));

        Map<String, Object> zoom = new LinkedHashMap<>();
        zoom.put("snap", Config.MAP_ZOOM_SNAP);
        zoom.put("delta", Config.MAP_ZOOM_DELTA);
        zoom.put("wheel", Config.MAP_ZOOM_WHEEL);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("format", IO.get(Config.WEB_TILE_FORMAT).getKey());
        map.put("maxPlayers", Pl3xMap.api().getMaxPlayers());
        map.put("lang", lang);
        map.put("zoom", zoom);

        try {
            map.put("players", Pl3xMap.api().getPlayerRegistry().parsePlayers());
            map.put("worldSettings", parseWorlds());
        } catch (Throwable t) {
            Logger.severe("Failed to parse players and worlds settings", t);
        }

        String json = this.gson.toJson(map);

        if (jsonHashCache != json.hashCode()) {
            Pl3xMap.api().getHttpdServer().getLiveDataHandler().send("settings", json);
            jsonHashCache = json.hashCode();
        }

        if (fileTick++ >= 20) {
            fileTick = 0;
            FileUtil.writeJson(json, FileUtil.getTilesDir().resolve("settings.json"));
        }
    }
}
