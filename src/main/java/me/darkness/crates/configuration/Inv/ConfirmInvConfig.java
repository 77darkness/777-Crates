package me.darkness.crates.configuration.Inv;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfirmInvConfig extends OkaeriConfig {

    public String titleChallenge = "&0ᴘᴏᴛᴡɪᴇʀᴅᴢ ᴡʏꜱʟᴀɴɪᴇ ʙɪᴛᴡʏ";
    public String titleAccept = "&0ᴘᴏᴛᴡɪᴇʀᴅᴢ ᴀᴋᴄᴇᴘᴛᴀᴄᴊᴇ ʙɪᴛᴡʏ";
    public int rows = 1;

    public GuiItem infoItem = new GuiItem(Material.CHEST, 4, 1, "&8» &e{crate}", List.of(
            "&#F6D365• &fSkrzynka: &e{crate}",
            "&#F6D365• &fKlucze: &#F6D365{amount}",
            "&#F6D365• &fPrzeciwnik: &6{opponent}"
    ), "NONE");

    public Map<String, GuiItem> items = this.createDefaultItems();

    private Map<String, GuiItem> createDefaultItems() {
        Map<String, GuiItem> m = new LinkedHashMap<>();

        m.put("accept", new GuiItem(
            Material.LIME_DYE,
            2,
            1,
            "&#0EFF00Potwierdź",
            List.of(
                " &8» &7ᴋʟɪᴋɴɪᴊ, ᴀʙʏ",
                " &8» &7ᴘᴏᴛᴡɪᴇʀᴅᴢɪć"
            ),
            "ACCEPT"
        ));

        m.put("decline", new GuiItem(
            Material.RED_DYE,
            6,
            1,
            "&#FF0000Anuluj",
            List.of(
                " &8» &7ᴋʟɪᴋɴɪᴊ, ᴀʙʏ",
                " &8» &7ᴀɴᴜʟᴏᴡᴀć."
            ),
            "DECLINE"
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
