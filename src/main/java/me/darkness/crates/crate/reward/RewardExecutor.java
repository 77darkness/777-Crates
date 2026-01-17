package me.darkness.crates.crate.reward;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RewardExecutor {

    private final CratesPlugin plugin;

    public RewardExecutor(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveReward(Player player, String crateName, CrateReward reward) {
        if (reward == null || player == null) {
            return;
        }

        if (reward.shouldGiveItem() && reward.getRewardItem() != null) {
            this.giveItem(player, reward.getRewardItem());
        }

        if (!reward.getCommands().isEmpty()) {
            this.executeCommands(player, reward.getCommands());
        }

        Lang lang = this.plugin.getConfigService().getLangConfig();
        if (lang == null) {
            return;
        }

        String itemName = this.getItemName(reward);
        TextUtil.send(lang, this.plugin, player, lang.rewardWon, Map.of("item", itemName));

        this.tryBroadcastReward(lang, player, crateName, reward, itemName);
    }

    private void tryBroadcastReward(Lang lang, Player player, String crateName, CrateReward reward, String itemName) {
        if (!lang.rewardBroadcastEnabled) {
            return;
        }

        double chance = reward != null ? reward.getChance() : 0.0;
        if (chance > lang.rewardBroadcastMaxChance) {
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("item", itemName);
        placeholders.put("crate", crateName == null ? "" : crateName);
        placeholders.put("chance", String.valueOf(chance));

        TextUtil.send(lang, this.plugin, Bukkit.getConsoleSender(), lang.rewardBroadcast, placeholders);
        for (Player online : Bukkit.getOnlinePlayers()) {
            TextUtil.send(lang, this.plugin, online, lang.rewardBroadcast, placeholders);
        }
    }

    private void giveItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

        if (!leftover.isEmpty()) {
            for (ItemStack leftoverItem : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }
        }
    }

    private void executeCommands(Player player, List<String> commands) {
        Map<String, String> placeholders = Map.of("player", player.getName());

        for (String command : commands) {
            String processedCommand = TextUtil.applyPlaceholders(command, placeholders);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    private String getItemName(CrateReward reward) {
        ItemStack item = reward.getRewardItem() != null ? reward.getRewardItem() : reward.getDisplayItem();
        if (item == null) {
            return "";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return TextUtil.colorize(meta.getDisplayName());
        }
        String mat = item.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] parts = mat.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
