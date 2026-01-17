package me.darkness.crates.crate.key;

import me.darkness.crates.util.ItemBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class KeyService {

    private static final String KEY_TAG = "777_case_key";
    private final NamespacedKey keyTag;

    public KeyService(Plugin plugin) {
        this.keyTag = new NamespacedKey(plugin, KEY_TAG);
    }

    public ItemStack tagKey(ItemStack item, String crateName) {
        if (item == null) {
            return null;
        }

        return ItemBuilder.of(item)
                .tagString(keyTag, Objects.toString(crateName, ""))
                .build();
    }

    public boolean isKey(ItemStack item, String crateName) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        String stored = ItemBuilder.of(item).getString(keyTag);
        return stored != null && stored.equalsIgnoreCase(crateName);
    }

    public boolean tryConsumeKey(Player player, String crateName) {
        if (player == null) {
            return true;
        }

        ItemStack[] contents = player.getInventory().getContents();

        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];

            if (!isKey(item, crateName)) {
                continue;
            }

            int amount = item.getAmount();
            if (amount <= 1) {
                contents[slot] = null;
            } else {
                item.setAmount(amount - 1);
            }

            player.getInventory().setContents(contents);
            return false;
        }

        return true;
    }

    public int countKeys(Player player, String crateName) {
        if (player == null) {
            return 0;
        }

        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKey(item, crateName)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public boolean takeKeys(Player player, String crateName, int amount) {
        if (player == null) {
            return false;
        }
        if (amount <= 0) {
            return true;
        }

        if (countKeys(player, crateName) < amount) {
            return false;
        }

        ItemStack[] contents = player.getInventory().getContents();
        int remaining = amount;

        for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
            ItemStack item = contents[slot];
            if (!isKey(item, crateName)) {
                continue;
            }

            int itemAmount = item.getAmount();
            if (itemAmount <= remaining) {
                contents[slot] = null;
                remaining -= itemAmount;
            } else {
                item.setAmount(itemAmount - remaining);
                remaining = 0;
            }
        }

        player.getInventory().setContents(contents);
        return remaining == 0;
    }
}
