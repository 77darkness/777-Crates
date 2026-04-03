package me.darkness.crates.crate.reward;

import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RewardExecutor {

    private final CratesPlugin plugin;

    public RewardExecutor(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveReward(Player player, String crateName, CrateReward reward) {
        if (reward == null || player == null) return;

        if (reward.shouldGiveItem() && reward.getRewardItem() != null) {
            giveItem(player, reward.getRewardItem());
        }

        if (!reward.getCommands().isEmpty()) {
            executeCommands(player, reward.getCommands());
        }

        Lang lang = this.plugin.getConfigService().getLangConfig();
        if (lang == null) return;

        String itemName = getItemName(reward);
        lang.rewardWon.send(player, Map.of("item", itemName));
        broadcastReward(lang, player, crateName, reward, itemName);
    }

    private void broadcastReward(Lang lang, Player player, String crateName, CrateReward reward, String itemName) {
        if (!lang.rewardBroadcastEnabled || reward.getChance() > lang.rewardBroadcastMaxChance) return;

        Map<String, String> placeholders = Map.of(
                "player", player.getName(),
                "item", itemName,
                "crate", crateName == null ? "" : crateName,
                "chance", String.valueOf(reward.getChance())
        );

        Bukkit.getOnlinePlayers().forEach(p -> lang.rewardBroadcast.send(p, placeholders));
    }

    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        leftover.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
    }

    private void executeCommands(Player player, List<String> commands) {
        Map<String, String> placeholders = Map.of("player", player.getName());
        commands.stream()
                .map(cmd -> TextUtil.applyPlaceholders(cmd, placeholders))
                .forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    private String getItemName(CrateReward reward) {
        ItemStack item = reward.getRewardItem() != null ? reward.getRewardItem() : reward.getDisplayItem();
        if (item == null) return "";

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.displayName() != null) {
            return LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName());
        }

        return capitalizeWords(item.getType().name().toLowerCase(Locale.ROOT).replace('_', ' '));
    }

    private String capitalizeWords(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
