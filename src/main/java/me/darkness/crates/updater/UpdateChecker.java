package me.darkness.crates.updater;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public final class UpdateChecker {

    private static final String VERSION_URL =
            "https://raw.githubusercontent.com/77darkness/777-Crates/main/version.txt";

    private final Plugin plugin;
    private final boolean enabled;

    private String latestVersion;
    private boolean updateAvailable;

    public UpdateChecker(final Plugin plugin, final boolean enabled) {
        this.plugin = plugin;
        this.enabled = enabled;
    }

    public void checkOnStartup() {
        if (!enabled) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                HttpURLConnection connection =
                        (HttpURLConnection) new URL(VERSION_URL).openConnection();

                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != 200) {
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {

                    String response = reader.readLine();
                    if (response == null) {
                        return;
                    }

                    String currentVersion = plugin.getDescription().getVersion();
                    latestVersion = response.trim();
                    updateAvailable = !latestVersion.equalsIgnoreCase(currentVersion);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (updateAvailable) {
                            Bukkit.getConsoleSender().sendMessage(
                                    TextUtil.colorize(
                                            "&8[&c777-Crates&8] &cDostępna jest nowa wersja pluginu! "
                                                    + "&8(&4" + latestVersion + "&8) "
                                                    + "&8| &cTwoja wersja: &4" + currentVersion
                                    )
                            );
                        } else {
                            Bukkit.getConsoleSender().sendMessage(
                                    TextUtil.colorize(
                                            "&8[&a777-Crates&8] &fPosiadasz aktualną wersję pluginu "
                                                    + "&8(&a" + currentVersion + "&8)"
                                    )
                            );
                        }
                    });
                }
            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        Bukkit.getConsoleSender().sendMessage(
                                TextUtil.colorize(
                                        "&8[&4777-Crate&8] &cNie udało się sprawdzić aktualizacji: &4"
                                                + ex.getMessage()
                                )
                        )
                );
            }
        });
    }

    public static void bootstrap(CratesPlugin plugin) {
        boolean enabled = true;

        if (plugin != null) {
            var cfgService = plugin.getConfigService();
            if (cfgService != null && cfgService.getCrateConfig() != null) {
                enabled = cfgService.getCrateConfig().updateChecker;
            }
        }

        new UpdateChecker(plugin, enabled).checkOnStartup();
    }
}
