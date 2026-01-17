package me.darkness.crates.configuration;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.EditInvConfig;
import me.darkness.crates.configuration.Inv.PreviewInvConfig;
import me.darkness.crates.configuration.Inv.RouletteInvConfig;
import me.darkness.crates.configuration.Inv.WinInvConfig;
import me.darkness.crates.configuration.Inv.MassOpenInvConfig;
import me.darkness.crates.configuration.Inv.AmountInvConfig;
import me.darkness.crates.configuration.Inv.ConfirmInvConfig;
import me.darkness.crates.configuration.Inv.SelectInvConfig;

import java.io.File;

public final class ConfigService {

    private final CratesPlugin plugin;

    private Config config;
    private Lang lang;
    private PreviewInvConfig previewInvConfig;
    private EditInvConfig editInvConfig;
    private RouletteInvConfig rouletteInvConfig;
    private WinInvConfig winInvConfig;
    private MassOpenInvConfig massOpenInvConfig;
    private SelectInvConfig selectInvConfig;
    private AmountInvConfig amountInvConfig;
    private ConfirmInvConfig confirmInvConfig;

    public ConfigService(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.config = this.createConfig(Config.class, "config.yml");
        this.lang = this.createConfig(Lang.class, "messages.yml");

        this.getGuiFolder();
        this.previewInvConfig = this.createConfig(PreviewInvConfig.class, "gui" + File.separator + "preview.yml");
        this.editInvConfig = this.createConfig(EditInvConfig.class, "gui" + File.separator + "edit.yml");
        this.rouletteInvConfig = this.createConfig(RouletteInvConfig.class, "gui" + File.separator + "roulette.yml");
        this.winInvConfig = this.createConfig(WinInvConfig.class, "gui" + File.separator + "win.yml");
        this.massOpenInvConfig = this.createConfig(MassOpenInvConfig.class, "gui" + File.separator + "mass-open.yml");
        this.selectInvConfig = this.createConfig(SelectInvConfig.class, "gui" + File.separator + "select.yml");
        this.amountInvConfig = this.createConfig(AmountInvConfig.class, "gui" + File.separator + "amount.yml");
        this.confirmInvConfig = this.createConfig(ConfirmInvConfig.class, "gui" + File.separator + "confirm.yml");
    }

    public void reload() {
        this.config.load();
        this.lang.load();
        this.previewInvConfig.load();
        this.editInvConfig.load();
        this.rouletteInvConfig.load();
        this.winInvConfig.load();
        this.massOpenInvConfig.load();
        this.selectInvConfig.load();
        this.amountInvConfig.load();
        this.confirmInvConfig.load();
    }

    private void getGuiFolder() {
        File guiFolder = new File(this.plugin.getDataFolder(), "gui");
        if (!guiFolder.exists()) {
            boolean ok = guiFolder.mkdirs();
            if (!ok && !guiFolder.exists()) {
                this.plugin.getLogger().warning("Nie udało się utworzyć folderu konfiguracji GUI: " + guiFolder.getAbsolutePath());
            }
        }
    }

    private <T extends eu.okaeri.configs.OkaeriConfig> T createConfig(Class<T> configClass, String path) {
        return ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(new File(this.plugin.getDataFolder(), path));
            it.saveDefaults();
            it.load(true);
        });
    }

    public Config getCrateConfig() {
        return this.config;
    }

    public Lang getLangConfig() {
        return this.lang;
    }

    public PreviewInvConfig getPreviewInv() {
        return this.previewInvConfig;
    }

    public EditInvConfig getEditInv() {
        return this.editInvConfig;
    }

    public RouletteInvConfig getRouletteInv() {
        return this.rouletteInvConfig;
    }

    public WinInvConfig getWinInv() {
        return this.winInvConfig;
    }

    public MassOpenInvConfig getMassOpenInv() {
        return this.massOpenInvConfig;
    }

    public SelectInvConfig getBattleCrateSelectInv() {
        return this.selectInvConfig;
    }

    public AmountInvConfig getBattleAmountInv() {
        return this.amountInvConfig;
    }

    public ConfirmInvConfig getBattleConfirmInv() {
        return this.confirmInvConfig;
    }
}
