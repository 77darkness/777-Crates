package me.darkness.crates.configuration;

import eu.okaeri.configs.OkaeriConfig;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrateConfig extends OkaeriConfig {

    public String name = "skrzynka";
    public String displayName = "&eSkrzynka";
    public String animationType = "roulette";

    public ItemStack key;

    public Integer keyCustomModelData = null;
    public Hologram hologram = new Hologram();

    public List<Location> locations = new ArrayList<>();
    public List<RewardEntry> rewards = new ArrayList<>();

    public static class Hologram extends OkaeriConfig {
        public boolean enabled = true;
        public double height = 2.1;
        public List<String> lines;

        public Hologram() {
            this.lines = new ArrayList<>(List.of(
                "&#F6D365&ltest",
                "&#F6D365• &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć",
                "&#FFF073ᴋᴜᴘ ᴋʟᴜᴄᴢᴇ: /ᴘᴏʀᴛꜰᴇʟ"
            ));
        }
    }

    public static class RewardEntry extends OkaeriConfig {
        public Integer slot = null;
        public String rewardItem;
        public List<String> commands = new ArrayList<>();
        public double chance = 10.0;
        public boolean giveItem = true;
    }

    public static CrateConfig createDefault(CratesPlugin plugin, String crateName, Location location) {
        CrateConfig config = new CrateConfig();
        config.name = crateName;
        config.displayName = TextUtil.color("&#F6D365" + crateName);
        config.animationType = "roulette";

        config.locations = new ArrayList<>();
        if (location != null) {
            config.locations.add(location);
        }

        ItemBuilder keyBuilder = ItemBuilder.of(Material.TRIPWIRE_HOOK)
            .name("&#F6D365Klucz do skrzynki " + crateName)
            .lore(List.of(
                "&8ɪɴꜰᴏʀᴍᴀᴄᴊᴇ",
                "&f",
                "&8× &fTym kluczem otworzysz skrzynkę &#F6D365" + crateName,
                "&f",
                "&#F6D365→ /warp skrzynki"
            ));

        if (config.keyCustomModelData != null) {
            keyBuilder.customModelData(config.keyCustomModelData);
        }

        KeyService keyService = plugin.getKeyServiceProvider().get();
        config.key = keyService.tagKey(keyBuilder.build(), crateName);

        config.hologram = new Hologram();
        config.hologram.enabled = true;
        config.hologram.height = 2.1;
        config.hologram.lines = new ArrayList<>(List.of(
            "&#F6D365&l" + crateName,
            "&#F6D365• &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴏᴛᴡᴏʀᴢʏć",
            "&#FFF073ᴋᴜᴘ ᴋʟᴜᴄᴢᴇ: /ᴘᴏʀᴛꜰᴇʟ"
        ));

        config.rewards = new ArrayList<>();
        return config;
    }
}
