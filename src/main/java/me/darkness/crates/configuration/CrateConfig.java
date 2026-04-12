package me.darkness.crates.configuration;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CrateConfig extends OkaeriConfig {

    public String name;
    public String displayName;
    public String animationType;

    public KeyItem key = new KeyItem();

    public Integer keyCustomModelData = null;
    public Hologram hologram = new Hologram();

    public boolean rewardBroadcastEnabled = true;
    public double rewardBroadcastMaxChance = 100.0;
    public Lang.MessageEntry rewardBroadcast = Lang.MessageEntry.chat(
            "&8» &fGracz &#00FF00{player} &fwylosował &#FFFF00{item} &fze skrzynki &#00FF00{crate}&f!"
    );

    public List<Location> locations = new ArrayList<>();
    public List<RewardEntry> rewards = new ArrayList<>();

    public static class KeyItem extends OkaeriConfig {
        public String material;
        public String name;
        public List<String> lore;
        public Integer customModelData = null;

        public KeyItem() {
            this.lore = new ArrayList<>(List.of(
                "&8ɪɴꜰᴏʀᴍᴀᴄᴊᴇ",
                "&f",
                "&8× &fᴋʟᴜᴄᴢ ᴢᴀᴋᴜᴘɪꜱᴢ ɴᴀ &#00FF00www.777code.pl",
                "&f",
                "&#00FF00→ /warp skrzynki"
            ));
        }
    }

    public static class Hologram extends OkaeriConfig {
        public boolean enabled;
        public double height;
        public List<String> lines;
        public Hologram() {}
    }

    public static class RewardEntry extends OkaeriConfig {
        public Integer slot = null;
        public String rewardItem;
        public List<String> commands = new ArrayList<>();
        public double chance;
        public boolean giveItem = true;
    }

    public static CrateConfig createDefault(String crateName, Location location) {
        CrateConfig config = new CrateConfig();
        config.name = crateName;
        config.displayName = "&#00FF00" + crateName;
        config.animationType = "roulette";

        config.locations = new ArrayList<>();
        if (location != null) {
            config.locations.add(location);
        }

        config.key = new KeyItem();
        config.key.material = "TRIPWIRE_HOOK";
        config.key.name = "&8» &fKlucz do skrzynki &#00FF00" + crateName;
        config.key.lore = new ArrayList<>(List.of(
            "&8ɪɴꜰᴏʀᴍᴀᴄᴊᴇ",
            "&f",
            "&8× &fᴋʟᴜᴄᴢ ᴢᴀᴋᴜᴘɪꜱᴢ ɴᴀ &#00FF00www.777code.pl",
            "&f",
            "&#00FF00→ /warp skrzynki"
        ));
        config.key.customModelData = config.keyCustomModelData;

        config.hologram = new Hologram();
        config.hologram.enabled = true;
        config.hologram.height = 2.1;
        config.hologram.lines = new ArrayList<>(List.of(
            "&#00FF00&l" + crateName,
            "&#00FF00* &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć",
            "&#00FF00ᴋᴜᴘ ᴋʟᴜᴄᴢᴇ: /ᴘᴏʀᴛꜰᴇʟ"
        ));

        config.rewards = new ArrayList<>();
        return config;
    }
}
