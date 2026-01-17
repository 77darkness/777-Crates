package me.darkness.crates.util;

import me.darkness.crates.configuration.Lang;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final Map<UUID, BossBar> BOSS_BARS = new HashMap<>();

    private TextUtil() {}

    public static void send(Plugin plugin, Player player, String path) {
        send(plugin, player, path, Collections.emptyMap());
    }

    public static void send(Plugin plugin, Player player, String path, Map<String, String> placeholders) {
        String basePath = "messages." + path;
        MessageType type = MessageType.from(plugin.getConfig().getString(basePath + ".type", "CHAT"));

        Object messageObj = plugin.getConfig().get(basePath + ".message");
        List<String> lines = getMessageLines(messageObj);
        if (lines.isEmpty()) return;

        List<String> processed = colorizeList(applyPlaceholders(lines, placeholders));

        switch (type) {
            case CHAT -> processed.forEach(player::sendMessage);
            case ACTIONBAR -> {
                String text = String.join(" ", processed);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
            }
            case TITLE -> player.sendTitle(first(processed), "", 10, 70, 20);
            case SUBTITLE -> player.sendTitle("", first(processed), 10, 70, 20);
            case TITLE_SUBTITLE -> player.sendTitle(first(processed), second(processed), 10, 70, 20);
            case BOSSBAR -> showBossBar(plugin, player, String.join(" ", processed));
        }
    }

    public static void send(Lang lang, Plugin plugin, CommandSender sender, Lang.MessageEntry entry) {
        send(lang, plugin, sender, entry, Collections.emptyMap());
    }

    public static void send(Lang lang, Plugin plugin, CommandSender sender, Lang.MessageEntry entry, Map<String, String> placeholders) {
        if (entry == null || sender == null) return;

        Map<String, String> merged = new HashMap<>(placeholders);
        merged.putIfAbsent("prefix", lang.prefix);

        List<String> lines = getMessageLines(entry.message);
        if (lines.isEmpty()) return;

        List<String> processed = colorizeList(applyPlaceholders(lines, merged));

        if (sender instanceof Player player) {
            send(lang, plugin, player, entry, placeholders);
            return;
        }

        processed.forEach(sender::sendMessage);
    }

    public static void send(Lang lang, Plugin plugin, Player player, Lang.MessageEntry entry) {
        send(lang, plugin, player, entry, Collections.emptyMap());
    }

    public static void send(Lang lang, Plugin plugin, Player player, Lang.MessageEntry entry, Map<String, String> placeholders) {
        if (entry == null) return;

        Map<String, String> merged = new HashMap<>(placeholders);
        merged.putIfAbsent("prefix", lang.prefix);

        MessageType type = MessageType.from(entry.type);
        List<String> lines = getMessageLines(entry.message);
        if (lines.isEmpty()) return;

        List<String> processed = colorizeList(applyPlaceholders(lines, merged));

        switch (type) {
            case CHAT -> processed.forEach(player::sendMessage);
            case ACTIONBAR -> {
                String text = String.join(" ", processed);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
            }
            case TITLE -> player.sendTitle(first(processed), "", 10, 70, 20);
            case SUBTITLE -> player.sendTitle("", first(processed), 10, 70, 20);
            case TITLE_SUBTITLE -> player.sendTitle(first(processed), second(processed), 10, 70, 20);
            case BOSSBAR -> showBossBar(plugin, player, String.join(" ", processed));
        }
    }

    public static void sendClickableMsg(
            Lang lang,
            Plugin plugin,
            Player player,
            Lang.MessageEntry entry,
            Map<String, String> placeholders,
            String command
    ) {
        if (player == null || entry == null) {
            return;
        }

        if (command == null || command.isBlank()) {
            send(lang, plugin, player, entry, placeholders);
            return;
        }

        Map<String, String> merged = new HashMap<>(placeholders == null ? Map.of() : placeholders);
        if (lang != null) {
            merged.putIfAbsent("prefix", lang.prefix);
        }

        MessageType type = MessageType.from(entry.type);
        if (type != MessageType.CHAT) {
            send(lang, plugin, player, entry, placeholders);
            return;
        }

        List<String> lines = getMessageLines(entry.message);
        if (lines.isEmpty()) {
            return;
        }

        List<String> processed = colorizeList(applyPlaceholders(lines, merged));

        for (String line : processed) {

            BaseComponent[] components = TextComponent.fromLegacyText(line);

            for (BaseComponent component : components) {
                component.setClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
                );
            }

            player.spigot().sendMessage(components);
        }

    }


    public static String applyPlaceholders(String text, Map<String, String> placeholders) {
        if (text == null) {
            return "";
        }
        if (placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        String out = text;
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            out = out.replace("{" + e.getKey() + "}", e.getValue());
            out = out.replace("%" + e.getKey() + "%", e.getValue());
        }
        return out;
    }

    public static List<String> applyPlaceholders(List<String> lines, Map<String, String> placeholders) {
        if (lines == null) {
            return Collections.emptyList();
        }
        if (placeholders == null || placeholders.isEmpty()) {
            return lines;
        }
        List<String> processed = new ArrayList<>();
        for (String line : lines) {
            processed.add(applyPlaceholders(line, placeholders));
        }
        return processed;
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return "";

        String colored = text.replaceAll("&([0-9a-fk-or])", "§$1");

        StringBuilder buffer = new StringBuilder();
        Matcher matcher = Pattern.compile("(?i)&?#([0-9a-f]{6})").matcher(colored);

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(Character.toLowerCase(c));
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String color(String text) {
        return colorize(text);
    }

    public static List<String> colorizeList(List<String> lines) {
        if (lines == null) return Collections.emptyList();
        List<String> colored = new ArrayList<>();
        for (String line : lines) {
            colored.add(colorize(line));
        }
        return colored;
    }

    private static List<String> getMessageLines(Object obj) {
        List<String> lines = new ArrayList<>();

        if (obj instanceof String single) {
            lines.add(single);
        } else if (obj instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof String s) lines.add(s);
            }
        }

        return lines;
    }

    private static void showBossBar(Plugin plugin, Player player, String message) {
        UUID uuid = player.getUniqueId();
        BossBar oldBar = BOSS_BARS.remove(uuid);
        if (oldBar != null) oldBar.removeAll();

        BossBar bar = Bukkit.createBossBar(message, BarColor.GREEN, BarStyle.SOLID);
        bar.addPlayer(player);
        bar.setProgress(1.0);
        BOSS_BARS.put(uuid, bar);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            bar.removeAll();
            BOSS_BARS.remove(uuid);
        }, 5 * 20L);
    }

    private static String first(List<String> list) {
        return list.isEmpty() ? "" : list.get(0);
    }

    private static String second(List<String> list) {
        return list.size() >= 2 ? list.get(1) : "";
    }

    private enum MessageType {
        CHAT, ACTIONBAR, TITLE, SUBTITLE, TITLE_SUBTITLE, BOSSBAR;

        static MessageType from(String value) {
            try {
                return valueOf(value.toUpperCase(Locale.ROOT));
            } catch (Exception e) {
                return CHAT;
            }
        }
    }
}
