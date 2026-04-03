package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectInvConfig extends OkaeriConfig {

    public String title = "&0ᴄᴀꜱᴇʙᴀᴛᴛʟᴇ &8| &0ᴡʏʙᴇʀᴢ ꜱᴋʀᴢʏɴᴋᴇ";
    public int rows = 3;

    public Map<String, GuiItem> items = this.createDefaultItems();

    public Map<String, CrateItem> crates = new LinkedHashMap<>();

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("close", new GuiItem(
                Material.BARRIER,
                22,
                "&#FF0000&lᴢᴀᴍᴋɴɪᴊ",
                List.of("&8» &fKliknij, aby zamknąć."),
                "BACK"
        ));

        return m;
    }

    public static class GuiItem extends OkaeriConfig {
        public Material material;
        public int slot;
        public int amount = 1;
        public String name;
        public List<String> lore;
        public String action;

        public GuiItem() {
        }

        public GuiItem(Material material, int slot, String name, List<String> lore, String action) {
            this.material = material;
            this.slot = slot;
            this.amount = 1;
            this.name = name;
            this.lore = lore;
            this.action = action;
        }
    }

    public static class CrateItem extends OkaeriConfig {
        public String displayName;
        public int slot;
        public String material;
        public List<String> lore;

        public CrateItem() {
        }

        public CrateItem(String displayName, int slot, String material, List<String> lore) {
            this.displayName = displayName;
            this.slot = slot;
            this.material = material;
            this.lore = lore;
        }
    }
}
