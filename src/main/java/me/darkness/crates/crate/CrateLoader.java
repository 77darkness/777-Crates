package me.darkness.crates.crate;

import dev.darkness.utilities.item.ItemBuilder;
import eu.okaeri.configs.ConfigManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.CrateConfig;
import me.darkness.crates.crate.animation.AnimationType;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.crate.reward.RewardType;
import me.darkness.crates.crate.reward.SlottedCrateReward;
import me.darkness.crates.utils.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CrateLoader {

    private static final LegacyComponentSerializer LEGACY_HEX = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private final CratesPlugin plugin;
    private final File cratesFolder;

    public CrateLoader(CratesPlugin plugin) {
        this.plugin = plugin;
        this.cratesFolder = new File(plugin.getDataFolder(), "crates");
        if (!this.cratesFolder.exists() && !this.cratesFolder.mkdirs()) {
            plugin.getLogger().warning("Nie udało się utworzyć folderu zapisu skrzynek: " + this.cratesFolder.getAbsolutePath());
        }
    }

    public int loadAll(CrateService crateService) {
        File[] files = this.cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) return 0;

        int loaded = 0;
        for (File file : files) {
            try {
                Crate crate = fromConfig(loadCrateConfig(file));
                crateService.registerCrate(crate);
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Błąd podczas ładowania skrzynki " + file.getName(), e);
            }
        }
        return loaded;
    }

    public void saveCrate(Crate crate) {
        File file = new File(this.cratesFolder, crate.getName() + ".yml");
        ensureFileExists(file);
        CrateConfig config = createCrateConfig(file);
        fillConfig(config, crate);
        config.save();
    }

    public void deleteCrate(String name) {
        File file = new File(this.cratesFolder, name + ".yml");
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Nie udało się usunąć pliku: " + file.getAbsolutePath());
        }
    }

    public Crate createDefaultCrate(String crateName, Location location) {
        File file = new File(this.cratesFolder, crateName + ".yml");
        ensureFileExists(file);

        CrateConfig config = createCrateConfig(file);
        CrateConfig defaults = CrateConfig.createDefault(crateName, location);

        config.name = defaults.name;
        config.displayName = defaults.displayName;
        config.animationType = defaults.animationType;
        config.key = defaults.key;
        config.keyCustomModelData = defaults.keyCustomModelData;
        config.locations = new ArrayList<>(defaults.locations);
        config.rewards = new ArrayList<>(defaults.rewards);
        config.hologram = defaults.hologram;
        config.save();

        return fromConfig(config);
    }

    private Crate fromConfig(CrateConfig config) {
        AnimationType animation = AnimationType.fromString(config.animationType).orElse(AnimationType.ROULETTE);
        ItemStack keyItem = makeKey(config);
        List<CrateReward> rewards = buildRewards(config);

        List<String> holoLines = config.hologram != null && config.hologram.lines != null
                ? new ArrayList<>(config.hologram.lines) : new ArrayList<>();
        double holoHeight = config.hologram != null ? config.hologram.height : 2.1;
        boolean holoEnabled = config.hologram == null || config.hologram.enabled;
        List<Location> locations = config.locations != null ? new ArrayList<>(config.locations) : new ArrayList<>();

        return Crate.builder(config.name)
                .displayName(config.displayName)
                .animationType(animation)
                .key(keyItem)
                .keyCustomModel(config.keyCustomModelData)
                .locations(locations)
                .rewards(rewards)
                .hologramLines(holoLines)
                .hologramHeight(holoHeight)
                .hologramEnabled(holoEnabled)
                .rewardBroadcastEnabled(config.rewardBroadcastEnabled)
                .rewardBroadcastMaxChance(config.rewardBroadcastMaxChance)
                .rewardBroadcast(config.rewardBroadcast)
                .build();
    }

    private List<CrateReward> buildRewards(CrateConfig config) {
        List<CrateReward> rewards = new ArrayList<>();
        for (CrateConfig.RewardEntry entry : config.rewards) {
            if (entry == null) continue;

            ItemStack item = entry.rewardItem != null && !entry.rewardItem.isBlank()
                    ? ItemStackSerializer.fromBase64(entry.rewardItem) : null;

            List<String> commands = entry.commands != null ? new ArrayList<>(entry.commands) : new ArrayList<>();
            RewardType type = entry.giveItem ? RewardType.ITEM : RewardType.COMMAND;
            int slot = entry.slot != null ? entry.slot : -1;

            rewards.add(slot >= 0
                    ? new SlottedCrateReward(slot, item, item, commands, entry.chance, type)
                    : new CrateReward(item, item, commands, entry.chance, type));
        }
        return RewardRoller.normalizeAll(rewards);
    }

    private ItemStack makeKey(CrateConfig config) {
        if (config.key == null || config.key.material == null || config.key.material.isBlank()) return null;

        Material material;
        try {
            material = Material.valueOf(config.key.material.trim().toUpperCase());
        } catch (Exception e) {
            material = Material.TRIPWIRE_HOOK;
            plugin.getLogger().warning("Nieprawidłowy item klucza dla skrzynki " + config.name + ": " + config.key.material);
        }

        ItemBuilder builder = ItemBuilder.of(material);
        if (config.key.name != null && !config.key.name.isBlank()) builder.name(config.key.name);
        if (config.key.lore != null && !config.key.lore.isEmpty()) builder.lore(new ArrayList<>(config.key.lore));

        Integer cmd = config.key.customModelData != null ? config.key.customModelData : config.keyCustomModelData;
        if (cmd != null) builder.customModelData(cmd);

        return plugin.getKeyService().tagKey(builder.build(), config.name);
    }

    private void fillConfig(CrateConfig config, Crate crate) {
        config.name = crate.getName();
        config.displayName = crate.getDisplayName();
        config.animationType = crate.getAnimationType().getId();
        config.keyCustomModelData = crate.getKeyCustomModel();
        config.locations = crate.getLocations();

        config.key = new CrateConfig.KeyItem();
        ItemStack key = crate.getKey();
        if (key != null && !key.getType().isAir()) {
            config.key.material = key.getType().name();
            ItemMeta meta = key.getItemMeta();
            if (meta != null) {
                if (meta.displayName() != null)
                    config.key.name = LEGACY_HEX.serialize(Objects.requireNonNull(meta.displayName()));
                if (meta.lore() != null) {
                    config.key.lore = Objects.requireNonNull(meta.lore()).stream()
                            .map(LEGACY_HEX::serialize)
                            .collect(Collectors.toCollection(ArrayList::new));
                }
                if (meta.hasCustomModelData()) config.key.customModelData = meta.getCustomModelData();
            }
        }

        config.rewards = new ArrayList<>();
        int slotCounter = 0;
        for (CrateReward reward : crate.getRewards()) {
            CrateConfig.RewardEntry entry = new CrateConfig.RewardEntry();
            entry.slot = reward instanceof SlottedCrateReward s ? s.getSlot() : slotCounter++;
            ItemStack itemToSave = reward.getRewardItem() != null ? reward.getRewardItem() : reward.getDisplayItem();
            entry.rewardItem = itemToSave != null ? ItemStackSerializer.toBase64(itemToSave) : null;
            entry.commands = reward.getCommands();
            entry.chance = reward.getChance();
            entry.giveItem = reward.shouldGiveItem();
            config.rewards.add(entry);
        }

        if (config.hologram == null) config.hologram = new CrateConfig.Hologram();
        config.hologram.enabled = crate.isHologramEnabled();
        config.hologram.height = crate.getHologramHeight();
        config.hologram.lines = crate.getHologramLines();

        config.rewardBroadcastEnabled = crate.isRewardBroadcastEnabled();
        config.rewardBroadcastMaxChance = crate.getRewardBroadcastMaxChance();
        config.rewardBroadcast = crate.getRewardBroadcast();
    }

    @SuppressWarnings("deprecation")
    private CrateConfig loadCrateConfig(File file) {
        return ConfigManager.create(CrateConfig.class, it -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(file);
            it.load(true);
        });
    }

    @SuppressWarnings("deprecation")
    private CrateConfig createCrateConfig(File file) {
        ensureFileExists(file);
        return ConfigManager.create(CrateConfig.class, it -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(file);
            it.load(false);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void ensureFileExists(File file) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!file.exists()) file.createNewFile();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Błąd podczas tworzenia pliku: " + file.getAbsolutePath(), e);
        }
    }
}
