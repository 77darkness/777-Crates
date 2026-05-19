package me.darkness.crates.configuration.inventories;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BattleInvConfig extends OkaeriConfig {

    public String title = "&8ʙɪᴛᴡʏ";
    public int rows = 6;

    public Map<String, GuiItem> items = this.createDefaultItems();

    public List<String> battleItemLore = List.of(
            "&8{time}",
            "&f",
            " &8⋆ &fSkrzynka: &#00FF00{crate}",
            " &8⋆ &fIlość: &#FFFF00{amount}",
            " &8⋆ &fOd: &#FF7C00{creator}",
            "&f",
            " &#00FF00&nᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴅᴏʟᴀᴄᴢʏć&f"
    );

    public List<Integer> battleSlots = List.of(
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    );

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("close", new GuiItem(
                Material.BARRIER,
                48,
                1,
                "&#FF0000&lᴢᴀᴍᴋɴɪᴊ",
                List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴡʏᴊść"),
                "CLOSE"
        ));

        m.put("create", new GuiItem(
                Material.LIME_DYE,
                50,
                1,
                "&#00FF00&lᴜᴛᴡóʀᴢ ʙɪᴛᴡᴇ",
                List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ꜱᴛᴡᴏʀᴢʏć ʙɪᴛᴡᴇ"),
                "CREATE"
        ));

        return m;
    }

    public static class GuiItem extends OkaeriConfig {
        public Material material;
        public int slot;
        public int amount;
        public String name;
        public List<String> lore;
        public String action;

        public GuiItem() {}

        public GuiItem(Material material, int slot, int amount, String name, List<String> lore, String action) {
            this.material = material;
            this.slot = slot;
            this.amount = amount;
            this.name = name;
            this.lore = lore;
            this.action = action;
        }
    }
}

