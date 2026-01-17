package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RouletteInvConfig extends OkaeriConfig {

    public String title = "&0ʟᴏꜱᴏᴡᴀɴɪᴇ ɴᴀɢʀᴏᴅʏ...";
    public int rows = 3;

    public List<Integer> displaySlots = List.of(10, 11, 12, 13, 14, 15, 16);

    public Map<String, GuiItem> items = this.createDefaultItems();

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> defaultItems = new LinkedHashMap<>();

        defaultItems.put("wygrana", new GuiItem(
            Material.SPECTRAL_ARROW,
            4,
            "&#FFE300&l↓↓↓ ᴡʏɢʀᴀɴᴀ ↓↓↓",
            List.of(),
            "NONE"
        ));

        defaultItems.put("wygranaa", new GuiItem(
            Material.SPECTRAL_ARROW,
            22,
            "&#FFE300&l↑↑↑ ᴡʏɢʀᴀɴᴀ ↑↑↑",
            List.of(),
            "NONE"
        ));

        java.util.Set<Integer> reserved = java.util.Set.of(4, 22);
        for (int i = 0; i < 27; i++) {
            if (reserved.contains(i)) {
                continue;
            }
            defaultItems.put("bg" + i, new GuiItem(
                Material.GRAY_STAINED_GLASS_PANE,
                i,
                " ",
                List.of(),
                "NONE"
            ));
        }

        return defaultItems;
    }

    public static class GuiItem extends OkaeriConfig {
        public Material material;
        public int slot;
        public String name;
        public List<String> lore;
        public String action;

        public GuiItem() {
        }

        public GuiItem(Material material, int slot, String name, List<String> lore, String action) {
            this.material = material;
            this.slot = slot;
            this.name = name;
            this.lore = lore;
            this.action = action;
        }
    }
}
