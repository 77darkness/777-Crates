package me.darkness.crates.crate;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.CrateConfig;
import me.darkness.crates.crate.animation.AnimationType;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.SlottedCrateReward;
import me.darkness.crates.util.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

public final class CrateLoader {

    private final CratesPlugin plugin;
    private final File cratesFolder;

    public CrateLoader(CratesPlugin plugin) {
        this.plugin = plugin;
        this.cratesFolder = new File(plugin.getDataFolder(), "crates");

        if (!this.cratesFolder.exists() && !this.cratesFolder.mkdirs()) {
            getLogger().warning("Błąd podczas tworzenia folderu z zapisem skrzynek: " + this.cratesFolder.getAbsolutePath());
        }
    }

    public int loadAll(CrateService crateService) {
        File[] files = this.cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            return 0;
        }

        int loaded = 0;
        for (File file : files) {
            try {
                CrateConfig config = this.loadCrateConfig(file);
                Crate crate = this.createCrate(config);
                crateService.registerCrate(crate);
                loaded++;
            } catch (Exception exception) {
                getLogger().log(Level.WARNING, "Błąd podczas ładowania pliku konfiguracyjnego skrzynki z pliku: " + file.getName(), exception);
            }
        }

        return loaded;
    }

    public void saveCrate(Crate crate) {
        File file = new File(this.cratesFolder, crate.getName() + ".yml");

        CrateConfig config = this.createCrateConfig(file);
        this.fillConfigFromCrate(config, crate);
        config.save();
    }

    public void deleteCrate(String name) {
        File file = new File(this.cratesFolder, name + ".yml");
        if (file.exists() && !file.delete()) {
            getLogger().warning("Błąd podczas usuwania pliku konfiguracyjnego skrzynki: " + file.getAbsolutePath());
        }
    }

    private CrateConfig loadCrateConfig(File file) {
        return ConfigManager.create(CrateConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(file);
            it.load(true);
        });
    }

    private CrateConfig createCrateConfig(File file) {
        this.fileExists(file);

        return ConfigManager.create(CrateConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(file);
            it.load(false);
        });
    }

    private void fileExists(File file) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                boolean ok = parent.mkdirs();
                if (!ok && !parent.exists()) {
                    getLogger().warning("Błąd podczas tworzenia folderu: " + parent.getAbsolutePath());
                }
            }
            if (!file.exists()) {
                boolean ok = file.createNewFile();
                if (!ok && !file.exists()) {
                    getLogger().warning("Błąd podczas tworzenia pliku: " + file.getAbsolutePath());
                }
            }
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "Błąd podczas tworzenia pliku konfiguracyjnego skrzynki: " + file.getAbsolutePath(), exception);
        }
    }

    private Crate createCrate(CrateConfig config) {
        AnimationType animationType = AnimationType.fromString(config.animationType)
            .orElse(AnimationType.ROULETTE);

        List<CrateConfig.RewardEntry> entries = new ArrayList<>(config.rewards);
        entries.sort((a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            Integer as = a.slot;
            Integer bs = b.slot;
            if (as == null && bs == null) return 0;
            if (as == null) return 1;
            if (bs == null) return -1;
            return Integer.compare(as, bs);
        });

        List<CrateReward> rewards = new ArrayList<>();
        for (CrateConfig.RewardEntry entry : entries) {
            int slot = (entry != null && entry.slot != null) ? entry.slot : -1;

            ItemStack rewardItem = null;
            List<String> commands = List.of();
            double chance = 0.0;
            boolean giveItem = true;

            if (entry != null) {
                rewardItem = ItemStackSerializer.fromBase64(entry.rewardItem);
                if (rewardItem == null) {
                    rewardItem = tryReadLegacyRewardItem(entry);
                }

                commands = entry.commands != null ? entry.commands : List.of();
                chance = entry.chance;
                giveItem = entry.giveItem;
            }

            if (slot >= 0) {
                rewards.add(new SlottedCrateReward(
                        slot,
                        rewardItem,
                        rewardItem,
                        commands,
                        chance,
                        giveItem
                ));
            } else {
                rewards.add(new CrateReward(
                        rewardItem,
                        rewardItem,
                        commands,
                        chance,
                        giveItem
                ));
            }
        }

        List<String> holoLines = (config.hologram != null && config.hologram.lines != null) ? config.hologram.lines : List.of();
        Double holoHeight = (config.hologram != null) ? config.hologram.height : 2.1;
        Boolean holoEnabled = config.hologram == null || config.hologram.enabled;

        List<Location> locations = new ArrayList<>();
        if (config.locations != null) {
            for (Location loc : config.locations) {
                if (loc != null) {
                    locations.add(loc);
                }
            }
        }

        return new Crate(
            config.name,
            config.displayName,
            animationType,
            config.key,
            config.keyCustomModelData,
            locations,
            rewards,
            new ArrayList<>(holoLines),
            holoHeight,
            holoEnabled
        );
    }

    private void fillConfigFromCrate(CrateConfig config, Crate crate) {
        config.name = crate.getName();
        config.displayName = crate.getDisplayName();
        config.animationType = crate.getAnimationType().getId();
        config.key = crate.getKey();
        config.keyCustomModelData = crate.getKeyCustomModelData();
        config.locations = crate.getLocations();

        config.rewards = new ArrayList<>();
        int slotIndex = 0;
        for (CrateReward reward : crate.getRewards()) {
            CrateConfig.RewardEntry entry = new CrateConfig.RewardEntry();
            if (reward instanceof SlottedCrateReward slotted) {
                entry.slot = slotted.getSlot();
            } else {
                entry.slot = slotIndex;
            }
            slotIndex++;

            ItemStack itemToSave = reward.getRewardItem() != null ? reward.getRewardItem() : reward.getDisplayItem();
            entry.rewardItem = ItemStackSerializer.toBase64(itemToSave);

            entry.commands = reward.getCommands();
            entry.chance = reward.getChance();
            entry.giveItem = reward.shouldGiveItem();
            config.rewards.add(entry);
        }

        if (config.hologram == null) {
            config.hologram = new CrateConfig.Hologram();
        }
        config.hologram.enabled = crate.isHologramEnabled() == null || crate.isHologramEnabled();
        config.hologram.height = crate.getHologramHeight() == null ? 2.1 : crate.getHologramHeight();
        config.hologram.lines = crate.getHologramLines();
    }

    private ItemStack tryReadLegacyRewardItem(CrateConfig.RewardEntry entry) {
        if (entry == null) {
            return null;
        }

        String base64 = entry.rewardItem;
        if (base64 == null || base64.isBlank()) {
            return null;
        }

        return ItemStackSerializer.fromBase64(base64);
    }

    public Crate createDefaultCrate(String crateName, Location location) {
        File file = new File(this.cratesFolder, crateName + ".yml");
        this.fileExists(file);

        CrateConfig config = ConfigManager.create(CrateConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(file);
            it.load(false);
        });

        CrateConfig defaults = CrateConfig.createDefault(this.plugin, crateName, location);

        config.name = defaults.name;
        config.displayName = defaults.displayName;
        config.animationType = defaults.animationType;
        config.key = defaults.key;
        config.locations = defaults.locations;
        config.rewards = defaults.rewards;
        config.hologram = defaults.hologram;

        config.save();

        return this.createCrate(config);
    }
}
