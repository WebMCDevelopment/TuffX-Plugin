package net.potato.tuff;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class LegacyBlockIdManager {

    private static final short[] IC = new short[Material.values().length];
    private static final ObjectOpenHashSet<String> UB = new ObjectOpenHashSet<>();
    private static boolean I = false;

    public static void initialize(Plugin p) {
        if (I) return;

        for (Material m : Material.values()) {
            if (!m.isBlock()) {
                IC[m.ordinal()] = 0;
                continue;
            }

            String bn = m.name().toLowerCase();
            int id = LegacyBlockIds.BLOCK_ID_MAP.getOrDefault(bn, -1);
            int mt = LegacyBlockIds.BLOCK_META_MAP.getOrDefault(bn, 0);

            if (id == -1) {
                id = 1; 
                if (UB.add(bn)) {
                    p.getLogger().warning("Unmapped block: " + bn + ". Defaulting to stone (ID=1).");
                }
            }
            
            IC[m.ordinal()] = (short) ((id & 0xFFF) | ((mt & 0xF) << 12));
        }
        
        I = true;
        p.getLogger().info("Legacy Block ID cache initialized successfully.");
    }

    public static short getLegacyShort(Material m) {
        return IC[m.ordinal()];
    }
}
