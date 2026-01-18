package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WinInvConfig extends OkaeriConfig {

    public String title = "&0ᴛᴡᴏᴊᴀ ᴡʏɢʀᴀɴᴀ:";
    public int rows = 3;

    public int rewardSlot = 13;

    public Map<String, GuiItem> items = createDefaultItems();

    private static Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("reopen", new GuiItem(
            Material.LIME_DYE,
            15,
            "&#00FF00&lᴏᴛᴡóʀᴢ ᴘᴏɴᴏᴡɴɪᴇ",
            List.of("&8» &fKliknij, aby otworzyć ponownie."),
            "REOPEN"
        ));

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

