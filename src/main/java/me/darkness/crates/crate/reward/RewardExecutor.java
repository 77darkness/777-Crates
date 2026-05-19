package me.darkness.crates.crate.reward;

import dev.darkness.utilities.text.TextUtil;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class RewardExecutor {

    private final CratesPlugin plugin;

    public RewardExecutor(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public int countFreeSlots(Player player) {
        int free = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) free++;
        }
        return free;
    }

    public void giveReward(Player player, Crate crate, CrateReward reward) {
        if (reward == null || player == null) return;

        if (reward.shouldGiveItem() && reward.getRewardItem() != null) {
            player.getInventory().addItem(reward.getRewardItem().clone());
        }

        if (!reward.getCommands().isEmpty()) {
            executeCommands(player, reward.getCommands());
        }

        LangConfig langConfig = this.plugin.getConfigService().lang();
        if (langConfig == null) return;

        String itemName = getItemName(reward);
        String crateName = crate != null ? crate.getDisplayName() : "";
        langConfig.rewardWon.send(player, Map.of("item", itemName, "crate", crateName));
        broadcastReward(crate, player, reward, itemName);
    }

    private void broadcastReward(Crate crate, Player player, CrateReward reward, String itemName) {
        if (crate == null) return;
        if (!crate.isRewardBroadcastEnabled() || reward.getChance() > crate.getRewardBroadcastMaxChance()) return;
        LangConfig.MessageEntry broadcastEntry = crate.getRewardBroadcast();
        if (broadcastEntry == null) return;

        Map<String, String> placeholders = Map.of(
                "player", player.getName(),
                "item", itemName,
                "crate", crate.getDisplayName(),
                "chance", String.valueOf(reward.getChance())
        );

        Bukkit.getOnlinePlayers().forEach(p -> broadcastEntry.send(p, placeholders));
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
            return LegacyComponentSerializer.legacyAmpersand().serialize(Objects.requireNonNull(meta.displayName()));
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
