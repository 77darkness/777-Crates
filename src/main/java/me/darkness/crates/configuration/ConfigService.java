package me.darkness.crates.configuration;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.*;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ConfigService {

    private final Plugin plugin;

    private PluginConfig pluginConfig;
    private LangConfig langConfig;
    private PreviewInvConfig previewInvConfig;
    private EditInvConfig editInvConfig;
    private RouletteInvConfig rouletteInvConfig;
    private WinInvConfig winInvConfig;
    private MassOpenInvConfig massOpenInvConfig;
    private SelectInvConfig selectInvConfig;
    private AmountInvConfig amountInvConfig;
    private ConfirmInvConfig confirmInvConfig;
    private BattleInvConfig battleInvConfig;

    public ConfigService(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    private <T extends OkaeriConfig> T load(Class<T> type, String path) {
        File file = new File(plugin.getDataFolder(), path);
        try {
            Files.createDirectories(file.getParentFile().toPath());
        } catch (IOException e) {
            plugin.getLogger().warning("Nie można utworzyć folderu konfigguracji dla: " + path);
        }

        return ConfigManager.create(type, it -> {
            it.withConfigurer(new YamlBukkitConfigurer());
            it.withSerdesPack(registry -> registry.register(new SerdesBukkit()));
            it.withBindFile(file.toPath());
            if (!file.exists()) it.saveDefaults();
            it.load(false);
        });
    }

    private <T extends OkaeriConfig> T gui(Class<T> type, String name) {
        return load(type, "gui/" + name);
    }

    public void load() {
        this.pluginConfig = load(PluginConfig.class, "config.yml");
        this.langConfig = load(LangConfig.class, "messages.yml");

        this.previewInvConfig = gui(PreviewInvConfig.class, "preview.yml");
        this.editInvConfig = gui(EditInvConfig.class, "edit.yml");
        this.rouletteInvConfig = gui(RouletteInvConfig.class, "roulette.yml");
        this.winInvConfig = gui(WinInvConfig.class, "win.yml");
        this.massOpenInvConfig = gui(MassOpenInvConfig.class, "mass-open.yml");
        this.selectInvConfig = gui(SelectInvConfig.class, "select.yml");
        this.amountInvConfig = gui(AmountInvConfig.class, "amount.yml");
        this.confirmInvConfig = gui(ConfirmInvConfig.class, "confirm.yml");
        this.battleInvConfig = gui(BattleInvConfig.class, "open-battle.yml");
    }

    public void reload() { load(); }

    public PluginConfig config() { return pluginConfig; }
    public LangConfig lang() { return langConfig; }
    public PreviewInvConfig previewInv() { return previewInvConfig; }
    public EditInvConfig editInv() { return editInvConfig; }
    public RouletteInvConfig rouletteInv() { return rouletteInvConfig; }
    public WinInvConfig winInv() { return winInvConfig; }
    public MassOpenInvConfig massOpenInv() { return massOpenInvConfig; }
    public SelectInvConfig selectInv() { return selectInvConfig; }
    public AmountInvConfig amountInv() { return amountInvConfig; }
    public ConfirmInvConfig confirmInv() { return confirmInvConfig; }
    public BattleInvConfig openBattleInv() { return battleInvConfig; }
}
