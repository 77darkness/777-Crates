package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MassOpenInvConfig extends OkaeriConfig {

    public String title = "&0ᴏᴛᴡᴀʀᴄɪᴇ ᴍᴀꜱᴏᴡᴇ ꜱᴋʀᴢʏɴᴋɪ {crate}";
    public int rows = 3;

    public Map<String, GuiItem> items = this.createDefaultItems();

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("open_2", new GuiItem(
            Material.TRIPWIRE_HOOK,
            11,
            2,
            "&#00FF00&lᴏᴛᴡóʀᴢ 2 ꜱᴋʀᴢʏɴᴋɪ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "AMOUNT_2"
        ));

        m.put("open_3", new GuiItem(
            Material.TRIPWIRE_HOOK,
            12,
            3,
            "&#00FF00&lᴏᴛᴡóʀᴢ 3 ꜱᴋʀᴢʏɴᴋɪ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "AMOUNT_3"
        ));

        m.put("open_4", new GuiItem(
            Material.TRIPWIRE_HOOK,
            13,
            4,
            "&#00FF00&lᴏᴛᴡóʀᴢ 4 ꜱᴋʀᴢʏɴᴋɪ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "AMOUNT_4"
        ));

        m.put("open_5", new GuiItem(
            Material.TRIPWIRE_HOOK,
            14,
            5,
            "&#00FF00&lᴏᴛᴡóʀᴢ 5 ꜱᴋʀᴢʏɴᴇᴋ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "AMOUNT_5"
        ));

        m.put("open_6", new GuiItem(
            Material.TRIPWIRE_HOOK,
            15,
            6,
            "&#00FF00&lᴏᴛᴡóʀᴢ 6 ꜱᴋʀᴢʏɴᴇᴋ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "AMOUNT_6"
        ));

        m.put("back", new GuiItem(
            Material.ARROW,
            22,
            "&#FF0000&lᴡʀóᴄ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴡʀóᴄɪć"),
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

