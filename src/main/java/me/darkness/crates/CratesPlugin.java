package me.darkness.crates;

import dev.darkness.utilities.misc.Logger;
import dev.darkness.utilities.misc.UpdateChecker;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.edit.EditSessionManager;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.command.CommandManager;
import me.darkness.crates.configuration.ConfigService;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.animation.AnimationService;
import me.darkness.crates.crate.reward.RewardExecutor;
import me.darkness.crates.hook.HologramHook;
import me.darkness.crates.listener.ChatInputListener;
import me.darkness.crates.listener.InventoryCloseListener;
import me.darkness.crates.listener.PlayerInteractionListener;
import me.darkness.crates.listener.PlayerQuitListener;
import me.darkness.crates.metrics.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CratesPlugin extends JavaPlugin {
    private Logger logger;
    private Metrics metrics;
    private UpdateChecker updateChecker;
    private ConfigService configService;
    private CrateService crateService;
    private CrateLoader crateLoader;
    private AnimationService animationService;
    private RewardExecutor rewardExecutor;
    private HologramHook hologramHook;
    private BattleService battleService;
    private KeyService keyService;
    private EditSessionManager editSessionManager;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        this.metrics = new Metrics(this, 29297);

        try {
            this.logger = new Logger(this);
            this.loadConfiguration();
            this.initializeServices();
            this.checkUpdates();
            this.loadCrates();
            this.registerHooks();
            this.registerListeners();
            this.registerCmds();

            long loadTime = System.currentTimeMillis() - startTime;
            logger.logStartup(loadTime);

        } catch (Exception exception) {
            logger.error("Blad uruchomienia pluginu", exception);
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (this.animationService != null) this.animationService.cancelAll();
        if (this.battleService != null) this.battleService.shutdown();
        if (this.hologramHook != null) this.hologramHook.removeAll();
        if (this.logger != null) this.logger.logShutdown();
    }

    private void registerCmds() {
        new CommandManager(this, this.crateService, this.crateLoader);
        logger.info("Zarejestrowano komendy.");
    }

    private void initializeServices() {
        this.keyService = new KeyService(this);
        this.editSessionManager = new EditSessionManager();
        this.crateService = new CrateService();
        this.crateLoader = new CrateLoader(this);
        this.animationService = new AnimationService(this);
        this.rewardExecutor = new RewardExecutor(this);
        this.battleService = new BattleService(this, this.crateService, this.keyService);
    }

    private void loadConfiguration() {
        this.configService = new ConfigService(this);
        this.configService.load();
        logger.success("Załadowano konfigurację.");
    }

    private void checkUpdates() {

        this.updateChecker = new UpdateChecker(
            this,
            "https://raw.githubusercontent.com/77darkness/777-Crates/main/version.txt",
            this.configService.getCrateConfig().updateChecker
        );
        this.updateChecker.checkOnStartup();
        this.updateChecker.registerJoinNotify("777crates.admin");
    }

    private void loadCrates() {
        int loaded = this.crateLoader.loadAll(this.crateService);
        logger.success("Załadowano &6" + loaded + " &fskrzynek.");
    }

    private void registerHooks() {
        this.hologramHook = new HologramHook(this);

        if (this.hologramHook.isEnabled()) {
            this.hologramHook.createHolograms(this.crateService.getAllCrates());
        }
    }

    private void registerListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new PlayerInteractionListener(this, this.crateService), this);
        pm.registerEvents(new InventoryCloseListener(this), this);
        pm.registerEvents(new ChatInputListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
    }

    public ConfigService getConfigService() {
        return this.configService;
    }

    public CrateService getCrateService() {
        return this.crateService;
    }

    public CrateLoader getCrateLoader() {
        return this.crateLoader;
    }

    public AnimationService getAnimationService() {
        return this.animationService;
    }

    public RewardExecutor getRewardExecutor() {
        return this.rewardExecutor;
    }

    public HologramHook getHologramHook() {
        return this.hologramHook;
    }

    public BattleService getBattleService() {
        return this.battleService;
    }

    public KeyService getKeyService() {
        return this.keyService;
    }

    public EditSessionManager getEditSessionManager() {
        return this.editSessionManager;
    }
}
