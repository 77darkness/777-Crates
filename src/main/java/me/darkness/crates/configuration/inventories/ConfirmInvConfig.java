package me.darkness.crates.configuration.inventories;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfirmInvConfig extends OkaeriConfig {

    public String titleChallenge = "&8ᴘᴏᴛᴡɪᴇʀᴅź ᴡʏꜱʟᴀɴɪᴇ ʙɪᴛᴡʏ";
    public String titleAccept = "&8ᴘᴏᴛᴡɪᴇʀᴅź ᴀᴋᴄᴇᴘᴛᴀᴄᴊᴇ ʙɪᴛᴡʏ";
    public int rows = 1;

    public GuiItem infoItem = new GuiItem(Material.CHEST, 4, 1, "&8» &#00FF00{crate}", List.of(
            "&f",
            " &8* &fSkrzynka: &#00FF00{crate}",
            " &8* &fKlucze: &#FFFF00{amount}",
            " &8* &fPrzeciwnik: &#FF7C00{opponent}",
            "&f"
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
                " &8* &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ",
                " &8* &fᴘᴏᴛᴡɪᴇʀᴅᴢɪć"
            ),
            "ACCEPT"
        ));

        m.put("decline", new GuiItem(
            Material.RED_DYE,
            6,
            1,
            "&#FF0000Anuluj",
            List.of(
                " &8* &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ",
                " &8* &fᴀɴᴜʟᴏᴡᴀć."
            ),
            "DECLINE"
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
