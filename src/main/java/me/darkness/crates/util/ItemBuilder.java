package me.darkness.crates.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemBuilder {

    private final ItemStack item;
    private Map<String, String> placeholders = Collections.emptyMap();

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
    }

    public ItemBuilder(Material material, int amount, short durability) {
        this.item = new ItemStack(material, amount, durability);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack != null ? itemStack.clone() : new ItemStack(Material.AIR);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(127, amount)));
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && name != null) {
            String processed = TextUtil.applyPlaceholders(name, this.placeholders);
            meta.setDisplayName(TextUtil.colorize(processed));
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder placeholders(Map<String, String> placeholders) {
        this.placeholders = placeholders != null ? placeholders : Collections.emptyMap();
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && lines != null) {
            List<String> processed = TextUtil.applyPlaceholders(lines, this.placeholders);
            meta.setLore(TextUtil.colorizeList(processed));
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        List<String> lore = (meta.hasLore() && meta.getLore() != null) ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String processed = TextUtil.applyPlaceholders(line, this.placeholders);
        lore.add(TextUtil.colorize(processed));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLoreLine(String line, int position) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        List<String> lore = (meta.hasLore() && meta.getLore() != null) ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        String processed = TextUtil.applyPlaceholders(line, this.placeholders);

        if (position < 0 || position > lore.size()) {
            lore.add(TextUtil.colorize(processed));
        } else {
            lore.add(position, TextUtil.colorize(processed));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return this;

        List<String> lore = new ArrayList<>(meta.getLore());
        if (index >= 0 && index < lore.size()) {
            lore.remove(index);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder removeLastLoreLine() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return this;

        List<String> lore = new ArrayList<>(meta.getLore());
        if (!lore.isEmpty()) {
            lore.remove(lore.size() - 1);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(null);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder unsafeEnchant(Enchantment enchantment, int level) {
        item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder flag(ItemFlag... flags) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder hideAllFlags() {
        return flag(ItemFlag.values());
    }

    public ItemBuilder glow() {
        return unsafeEnchant(Enchantment.LUCK_OF_THE_SEA, 1).flag(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder customModelData(int data) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(data);
            item.setItemMeta(meta);
        }
        return this;
    }

    public boolean hasLore() {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasLore() && meta.getLore() != null && !meta.getLore().isEmpty();
    }

    public ItemBuilder loreAppend(List<String> extraLore) {
        if (extraLore == null || extraLore.isEmpty()) {
            return this;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }

        List<String> lore = (meta.hasLore() && meta.getLore() != null)
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();

        List<String> processed = TextUtil.applyPlaceholders(extraLore, this.placeholders);
        lore.addAll(TextUtil.colorizeList(processed));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder replacePlaceholder(String key, String value) {
        if (key == null) {
            return this;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null) {
            return this;
        }

        Map<String, String> map = Collections.singletonMap(key, value == null ? "" : value);
        List<String> replaced = TextUtil.applyPlaceholders(meta.getLore(), map);
        meta.setLore(replaced);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder tagString(NamespacedKey key, String value) {
        return tag(key, PersistentDataType.STRING, value == null ? "" : value);
    }

    public ItemBuilder tagInt(NamespacedKey key, int value) {
        return tag(key, PersistentDataType.INTEGER, value);
    }

    public ItemBuilder tagBoolean(NamespacedKey key, boolean value) {
        return tag(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    public <T, Z> ItemBuilder tag(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (key == null || type == null) {
            return this;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, type, value);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeTag(NamespacedKey key) {
        if (key == null) {
            return this;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }

        meta.getPersistentDataContainer().remove(key);
        item.setItemMeta(meta);
        return this;
    }

    public boolean hasTag(NamespacedKey key, PersistentDataType<?, ?> type) {
        if (key == null || type == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(key, type);
    }

    public <T, Z> Z getTag(NamespacedKey key, PersistentDataType<T, Z> type) {
        if (key == null || type == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(key, type);
    }

    public String getString(NamespacedKey key) {
        return getTag(key, PersistentDataType.STRING);
    }

    public Integer getInt(NamespacedKey key) {
        return getTag(key, PersistentDataType.INTEGER);
    }

    public Boolean getBoolean(NamespacedKey key) {
        Byte b = getTag(key, PersistentDataType.BYTE);
        if (b == null) {
            return null;
        }
        return b != 0;
    }

    public static NamespacedKey key(Plugin plugin, String key) {
        if (plugin == null || key == null) {
            throw new IllegalArgumentException("nbt is null");
        }
        return new NamespacedKey(plugin, key);
    }

    public int getIntOrDefault(NamespacedKey key, int def) {
        Integer v = getInt(key);
        return v != null ? v : def;
    }

    public ItemBuilder removeIntTag(NamespacedKey key) {
        return removeTag(key);
    }

    public ItemStack build() {
        return item.clone();
    }

    public ItemStack toItemStack() {
        return item;
    }
}