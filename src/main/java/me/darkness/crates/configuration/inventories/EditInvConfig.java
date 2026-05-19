package me.darkness.crates.configuration.inventories;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditInvConfig extends OkaeriConfig {

    public String title = "&8ᴇᴅʏᴄᴊᴀ ꜱᴋʀᴢʏɴᴋɪ {crate}";
    public int rows = 6;
    public Map<String, GuiItem> items = this.createDefaultItems();


    public List<String> rewardEditLore = List.of(
        "&8",
        "&8* &fꜱᴢᴀɴꜱᴀ ɴᴀ ᴅʀᴏᴘ: &#FFFF00{chance}%",
        " &8» &fᴋʟɪᴋɴɪᴊ &#00FF00ᴘᴘᴍ&f, ᴀʙʏ ᴢᴍɪᴇɴɪć ꜱᴢᴀɴꜱᴇ",
        "&8",
        "&8* &fᴋᴏᴍᴇɴᴅᴀ: &#00FF00{command}",
        " &8» &fᴋʟɪᴋɴɪᴊ &#FFFF00ꜱʜɪꜰᴛ+ᴘᴘᴍ&f, ᴀʙʏ ᴢᴍɪᴇɴɪć ᴋᴏᴍᴇɴᴅᴇ",
        " &8» &fᴋʟɪᴋɴɪᴊ &#FF7C00ꜰ&f, ᴀʙʏ ᴘʀᴢᴇʟᴀᴄᴢʏć ᴅʀᴏᴘ &8({mode})"
    );

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("close", new GuiItem(
            Material.BARRIER,
            49,
            "&#FF0000&lᴢᴀᴍᴋɴɪᴊ",
            List.of("&8» &fKliknij, aby zamknąć."),
            "CLOSE"
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
