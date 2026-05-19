package me.darkness.crates.configuration.inventories;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RouletteInvConfig extends OkaeriConfig {

    public String title = "&8ʟᴏꜱᴏᴡᴀɴɪᴇ ɴᴀɢʀᴏᴅʏ...";
    public String winnerTitle = "&8ʟᴏꜱᴏᴡᴀɴɪᴇ ᴢᴡʏᴄɪᴇᴢᴄʏ...";
    public int rows = 3;

    public List<Integer> displaySlots = List.of(10, 11, 12, 13, 14, 15, 16);

    public Map<String, GuiItem> items = this.createDefaultItems();

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("win1", new GuiItem(
            Material.SPECTRAL_ARROW,
            4,
            "&#FFE300&l↓↓↓ ᴡʏɢʀᴀɴᴀ ↓↓↓",
            List.of(),
            "NONE"
        ));

        m.put("win2", new GuiItem(
            Material.SPECTRAL_ARROW,
            22,
            "&#FFE300&l↑↑↑ ᴡʏɢʀᴀɴᴀ ↑↑↑",
            List.of(),
            "NONE"
        ));

        Set<Integer> reserved = Set.of(4, 22);
        for (int i = 0; i < 27; i++) {
            if (reserved.contains(i)) {
                continue;
            }
            m.put("szklo" + i, new GuiItem(
                Material.GRAY_STAINED_GLASS_PANE,
                i,
                " ",
                List.of(),
                "NONE"
            ));
        }

        return m;
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
