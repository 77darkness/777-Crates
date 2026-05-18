package me.darkness.crates.crate.key;

import dev.darkness.utilities.item.ItemNbt;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class KeyService {

    private final NamespacedKey keyTag;

    public KeyService(Plugin plugin) {
        this.keyTag = new NamespacedKey(plugin, "777_crate_key");
    }

    public ItemStack tagKey(ItemStack item, String crateName) {
        if (item == null) return null;
        return ItemNbt.set(item, keyTag, PersistentDataType.STRING, Objects.toString(crateName, ""));
    }

    public boolean isKey(ItemStack item, String crateName) {
        if (item == null || item.getType().isAir()) return false;
        String stored = ItemNbt.get(item, keyTag, PersistentDataType.STRING);
        return stored != null && stored.equalsIgnoreCase(crateName);
    }

    public boolean tryConsumeKey(Player player, String crateName) {
        if (player == null) return false;

        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (!isKey(contents[slot], crateName)) continue;

            if (contents[slot].getAmount() <= 1) {
                contents[slot] = null;
            } else {
                contents[slot].setAmount(contents[slot].getAmount() - 1);
            }
            player.getInventory().setContents(contents);
            return true;
        }
        return false;
    }

    public int countKeys(Player player, String crateName) {
        if (player == null) return 0;

        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isKey(item, crateName)) count += item.getAmount();
        }
        return count;
    }

    public boolean takeKeys(Player player, String crateName, int amount) {
        if (player == null) return false;
        if (amount <= 0) return true;
        if (countKeys(player, crateName) < amount) return false;

        ItemStack[] contents = player.getInventory().getContents();
        int remaining = amount;

        for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
            if (!isKey(contents[slot], crateName)) continue;

            int itemAmount = contents[slot].getAmount();
            if (itemAmount <= remaining) {
                contents[slot] = null;
                remaining -= itemAmount;
            } else {
                contents[slot].setAmount(itemAmount - remaining);
                remaining = 0;
            }
        }

        player.getInventory().setContents(contents);
        return remaining == 0;
    }
}
