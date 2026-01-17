package me.darkness.crates;

import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.key.KeyServiceProvider;
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
import me.darkness.crates.updater.UpdateChecker;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CratesPlugin extends JavaPlugin {

    private ConfigService configService;
    private CrateService crateService;
    private CrateLoader crateLoader;
    private AnimationService animationService;
    private RewardExecutor rewardExecutor;
    private HologramHook hologramHook;
    private BattleService battleService;
    private KeyServiceProvider keyServiceProvider;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();

        try {
            this.initializeServices();
            this.loadConfiguration();
            UpdateChecker.bootstrap(this);
            this.loadCrates();
            this.registerHooks();
            this.registerListeners();
            this.registerCmds();

            long loadTime = System.currentTimeMillis() - startTime;
            getServer().getConsoleSender().sendMessage("§8[§a§l777-Crates§8] §fUruchomiono plugin! §7(Wczytano w " + loadTime + "ms)§r");

        } catch (Exception exception) {
            getServer().getConsoleSender().sendMessage("§8[§4§l777-Crates§8] §cWystąpił błąd podczas uruchamiania pluginu §8(" + exception.getMessage() + ")§r");
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {

        if (this.animationService != null) {
            this.animationService.cancelAll();
        }

        if (this.battleService != null) {
            this.battleService.shutdown();
        }

        if (this.hologramHook != null) {
            this.hologramHook.removeAll();
        }

        getServer().getConsoleSender().sendMessage("§8[§4§l777-Crates§8] §cWyłączono plugin :C§r");
    }

    private void registerCmds() {
        new CommandManager(this, this.crateService, this.crateLoader, this.configService);
        getServer().getConsoleSender().sendMessage("§8[§a§l777-Crates§8] §fZarejestrowano komendy§r.");
    }

    private void initializeServices() {
        this.keyServiceProvider = new KeyServiceProvider(this);
        this.crateService = new CrateService();
        this.crateLoader = new CrateLoader(this);
        this.animationService = new AnimationService(this);
        this.rewardExecutor = new RewardExecutor(this);
        this.battleService = new BattleService(this, this.crateService, this.keyServiceProvider.get());
    }

    private void loadConfiguration() {
        this.configService = new ConfigService(this);
        this.configService.load();
        getServer().getConsoleSender().sendMessage("§8[§a§l777-Crates§8] §fZaładowano konfigurację§r.");
    }

    private void loadCrates() {
        int loaded = this.crateLoader.loadAll(this.crateService);
        getServer().getConsoleSender().sendMessage("§8[§a§l777-Crates§8] §fZaładowano §a" + loaded + " §fskrzynek§r.");
    }

    private void registerHooks() {
        this.hologramHook = new HologramHook(this);

        if (this.hologramHook.isEnabled()) {
            this.hologramHook.createHolograms(this.crateService.getAllCrates());
        }
    }

    private void registerListeners() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(
            new PlayerInteractionListener(this, this.crateService),
            this
        );
        pluginManager.registerEvents(
            new InventoryCloseListener(this),
            this
        );
        pluginManager.registerEvents(
            new ChatInputListener(this),
            this
        );
        pluginManager.registerEvents(
            new PlayerQuitListener(this),
            this
        );
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

    public KeyServiceProvider getKeyServiceProvider() {
        return this.keyServiceProvider;
    }
}
