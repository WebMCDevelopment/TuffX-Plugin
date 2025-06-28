package net.potato.tuff;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class LegacyBlockIdManager {

    private static final short[] ID_CACHE = new short[Material.values().length];
    private static final Set<String> unmappedBlocks = new HashSet<>();
    private static boolean initialized = false;

    public static void initialize(Plugin plugin) {
        if (initialized) return;

        for (Material material : Material.values()) {
            if (!material.isBlock()) {
                ID_CACHE[material.ordinal()] = 0; // Air
                continue;
            }

            String blockName = material.name().toLowerCase();
            int id = LegacyBlockIds.BLOCK_ID_MAP.getOrDefault(blockName, -1);
            int meta = LegacyBlockIds.BLOCK_META_MAP.getOrDefault(blockName, 0);

            if (id == -1) {
                id = 1; 
                if (unmappedBlocks.add(blockName)) {
                    plugin.getLogger().warning("Unmapped block: " + blockName + ". Defaulting to stone (ID=1).");
                }
            }
            
            ID_CACHE[material.ordinal()] = (short) ((id & 0xFFF) | ((meta & 0xF) << 12));
        }
        
        initialized = true;
        plugin.getLogger().info("Legacy Block ID cache initialized successfully.");
    }

    public static short getLegacyShort(Material material) {
        return ID_CACHE[material.ordinal()];
    }
}