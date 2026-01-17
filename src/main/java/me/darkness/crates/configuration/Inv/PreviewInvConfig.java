package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class PreviewInvConfig extends OkaeriConfig {

    public String title = "&0ᴘᴏᴅɢʟᴀᴅ ꜱᴋʀᴢʏɴᴋɪ {crate}";
    public int rows = 6;
    public Map<String, GuiItem> items = this.createDefaultItems();

    public List<String> rewardPreviewLore = List.of(
        "&f",
        "&#F6D365• &fꜱᴢᴀɴꜱᴀ ɴᴀ ᴅʀᴏᴘ: &#F6D365{chance}%"
    );

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> defaultItems = new LinkedHashMap<>();

        defaultItems.put("open_animated", new GuiItem(
            Material.GREEN_BANNER,
            46,
            "&#00FF00&lᴏᴛᴡóʀᴢ ᴢ ᴀɴɪᴍᴀᴄᴊᴀ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "OPEN_ANIMATED"
        ));
        defaultItems.put("open_animatedd", new GuiItem(
             Material.GREEN_BANNER,
             47,
             "&#00FF00&lᴏᴛᴡóʀᴢ ᴢ ᴀɴɪᴍᴀᴄᴊᴀ",
             List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
             "OPEN_ANIMATED"
        ));

        defaultItems.put("open_without_animation", new GuiItem(
            Material.PURPLE_BANNER,
            51,
            "&#DD00FF&lᴏᴛᴡóʀᴢ ʙᴇᴢ ᴀɴɪᴍᴀᴄᴊɪ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "OPEN_WITHOUT_ANIMATION"
        ));
        defaultItems.put("open_without_animatioon", new GuiItem(
            Material.PURPLE_BANNER,
            52,
            "&#DD00FF&lᴏᴛᴡóʀᴢ ʙᴇᴢ ᴀɴɪᴍᴀᴄᴊɪ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć"),
            "OPEN_WITHOUT_ANIMATION"
        ));

        defaultItems.put("mass_open", new GuiItem(
            Material.CHEST_MINECART,
            49,
            "&#FF8600&lᴏᴛᴡᴀʀᴄɪᴇ ᴍᴀꜱᴏᴡᴇ",
            List.of("&8» &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴘʀᴢᴇᴊść"),
            "MASS_OPEN"
        ));

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
