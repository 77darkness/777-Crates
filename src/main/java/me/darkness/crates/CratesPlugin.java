package me.darkness.crates;

import dev.darkness.utilities.misc.Logger;
import dev.darkness.utilities.misc.UpdateChecker;
import me.darkness.crates.commands.handlers.MessageHandler;
import me.darkness.crates.commands.resolvers.CrateArgumentResolver;
import me.darkness.crates.commands.resolvers.PlayerTargetResolver;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.edit.EditSessionManager;
import me.darkness.crates.crate.key.KeyService;
import dev.darkness.commands.CommandRegistry;
import me.darkness.crates.commands.AdminCratesCommand;
import me.darkness.crates.commands.BattlePlayerCommand;
import me.darkness.crates.configuration.ConfigService;
import me.darkness.crates.crate.CrateLoader;
import me.darkness.crates.crate.CrateService;
import me.darkness.crates.crate.animation.AnimationService;
import me.darkness.crates.crate.reward.RewardExecutor;
import me.darkness.crates.hooks.HologramHook;
import me.darkness.crates.inventories.MassOpenInv;
import me.darkness.crates.inventories.PreviewInv;
import me.darkness.crates.listeners.ChatInputListener;
import me.darkness.crates.listeners.PlayerInteractionListener;
import me.darkness.crates.listeners.PlayerQuitListener;
import me.darkness.crates.metrics.Metrics;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CratesPlugin extends JavaPlugin {
    private Logger logger;
    private ConfigService configService;
    private CrateService crateService;
    private CrateLoader crateLoader;
    private AnimationService animationService;
    private RewardExecutor rewardExecutor;
    private HologramHook hologramHook;
    private BattleService battleService;
    private KeyService keyService;
    private EditSessionManager editSessionManager;
    private PreviewInv previewInv;
    private MassOpenInv massOpenInv;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        new Metrics(this, 30565);

        try {
            logger = new Logger(this);
            loadConfiguration();
            initializeServices();
            checkUpdates();
            loadCrates();
            registerHooks();
            registerListeners();
            registerCommands();

            long loadTime = System.currentTimeMillis() - startTime;
            logger.logStartup(loadTime);

        } catch (Exception exception) {
            logger.error("Blad uruchomienia pluginu", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (animationService != null) animationService.cancelAll();
        if (battleService != null) battleService.shutdown();
        if (hologramHook != null) hologramHook.removeAll();
        if (logger != null) logger.logShutdown();
    }

    private void registerCommands() {
        MessageHandler messageHandler = new MessageHandler(configService);
        new CommandRegistry(this, messageHandler)
                .resolver(new CrateArgumentResolver(crateService))
                .resolver(new PlayerTargetResolver())
                .register(new AdminCratesCommand(this, crateService, crateLoader))
                .register(new BattlePlayerCommand(this, battleService))
                .sync();

        logger.info("&fZarejestrowano komendy.");
    }

    private void initializeServices() {
        keyService = new KeyService(this);
        editSessionManager = new EditSessionManager();
        crateService = new CrateService();
        crateLoader = new CrateLoader(this);
        animationService = new AnimationService(this);
        rewardExecutor = new RewardExecutor(this);
        battleService = new BattleService(this, crateService, keyService);
        massOpenInv = new MassOpenInv(this);
        previewInv = new PreviewInv(this, massOpenInv);
    }

    private void loadConfiguration() {
        configService = new ConfigService(this);
        configService.load();

        logger.success("&fZaładowano konfigurację.");
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(
                this,
                "https://raw.githubusercontent.com/77darkness/777-Crates/main/version.txt",
                configService.config().updateChecker
        );

        updateChecker.checkOnStartup();
        updateChecker.registerJoinNotify("777crates.admin");
    }

    private void loadCrates() {
        int loaded = crateLoader.loadAll(crateService);
        logger.success("&fZaładowano &6" + loaded + " &fskrzynek.");
    }

    private void registerHooks() {
        hologramHook = new HologramHook(this);

        if (hologramHook.isEnabled()) {
            hologramHook.createHolograms(crateService.getAllCrates());
        }
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        PlayerInteractionListener playerInteractionListener = new PlayerInteractionListener(this, crateService, previewInv);
        pm.registerEvents(playerInteractionListener, this);
        pm.registerEvents(new ChatInputListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this, playerInteractionListener), this);
    }

    public PreviewInv getPreviewInv() {
        return previewInv;
    }

    public MassOpenInv getMassOpenInv() {
        return massOpenInv;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public CrateService getCrateService() {
        return crateService;
    }

    public CrateLoader getCrateLoader() {
        return crateLoader;
    }

    public AnimationService getAnimationService() {
        return animationService;
    }

    public RewardExecutor getRewardExecutor() {
        return rewardExecutor;
    }

    public HologramHook getHologramHook() {
        return hologramHook;
    }

    public BattleService getBattleService() {
        return battleService;
    }

    public KeyService getKeyService() {
        return keyService;
    }

    public EditSessionManager getEditSessionManager() {
        return editSessionManager;
    }
}
