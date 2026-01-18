package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditInvConfig extends OkaeriConfig {

    public String title = "&0ᴇᴅʏᴄᴊᴀ ꜱᴋʀᴢʏɴᴋɪ {crate}";
    public int rows = 6;
    public Map<String, GuiItem> items = this.createDefaultItems();


    public List<String> rewardEditLore = List.of(
        "&8",
        "&#F6D365• &fꜱᴢᴀɴꜱᴀ ɴᴀ ᴅʀᴏᴘ: &#F6D365{chance}%",
        "&8» &fᴋʟɪᴋɴɪᴊ &#F6D365ᴘᴘᴍ&f, ᴀʙʏ ᴢᴍɪᴇɴɪᴄ ꜱᴢᴀɴꜱᴇ",
        "&8",
        "&#F6D365• &fᴋᴏᴍᴇɴᴅᴀ: &#F6D365{command}",
        "&8» &fᴋʟɪᴋɴɪᴊ &#F6D365ꜱʜɪꜰᴛ+ᴘᴘᴍ&f, ᴀʙʏ ᴢᴍɪᴇɴɪᴄ ᴋᴏᴍᴇɴᴅᴇ",
        "&8» &fᴋʟɪᴋɴɪᴊ &#F6D365ꜰ&f, ᴀʙʏ ᴘʀᴢᴇʟᴀᴄᴢʏᴄ ᴅʀᴏᴘ ɪᴛᴇᴍᴜ"
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
