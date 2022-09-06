package net.pl3x.map.addon.heightmaps;

import net.pl3x.map.Key;
import net.pl3x.map.Pl3xMap;
import net.pl3x.map.addon.Addon;
import net.pl3x.map.addon.heightmaps.heightmap.EvenOddLowContrastHeightmap;
import net.pl3x.map.addon.heightmaps.heightmap.EvenOddModernHeightmap;
import net.pl3x.map.addon.heightmaps.heightmap.EvenOddOldSchoolHeightmap;
import net.pl3x.map.addon.heightmaps.heightmap.LowContrastHeightmap;
import net.pl3x.map.heightmap.HeightmapRegistry;

public class HeightmapsAddon extends Addon {
    public static final Key EVEN_ODD_LOW_CONTRAST = new Key("even_odd_low_contrast_heightmap");
    public static final Key EVEN_ODD_MODERN = new Key("even_odd_modern_heightmap");
    public static final Key EVEN_ODD_OLD_SCHOOL = new Key("even_odd_old_school_heightmap");
    public static final Key LOW_CONTRAST = new Key("low_contrast_heightmap");

    @Override
    public void onEnable() {
        // register our custom heightmaps with Pl3xMap
        HeightmapRegistry registry = Pl3xMap.api().getHeightmapRegistry();
        registry.register(EVEN_ODD_LOW_CONTRAST, new EvenOddLowContrastHeightmap());
        registry.register(EVEN_ODD_MODERN, new EvenOddModernHeightmap());
        registry.register(EVEN_ODD_OLD_SCHOOL, new EvenOddOldSchoolHeightmap());
        registry.register(LOW_CONTRAST, new LowContrastHeightmap());
    }

    @Override
    public void onDisable() {
        // unregister our custom heightmaps from Pl3xMap
        HeightmapRegistry registry = Pl3xMap.api().getHeightmapRegistry();
        registry.unregister(EVEN_ODD_LOW_CONTRAST);
        registry.unregister(EVEN_ODD_MODERN);
        registry.unregister(EVEN_ODD_OLD_SCHOOL);
        registry.unregister(LOW_CONTRAST);
    }
}
