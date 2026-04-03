package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Lang;
import me.darkness.crates.crate.edit.EditSession;
import me.darkness.crates.inv.EditInv;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class ChatInputListener implements Listener {

    private final CratesPlugin plugin;

    public ChatInputListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        EditSession.Session session = plugin.getEditSessionManager().get(player.getUniqueId());
        if (session == null) return;
        if (session.getEditingChanceSlot() == null && session.getEditingCommandSlot() == null) return;

        event.setCancelled(true);
        String msg = event.getMessage().trim();

        Bukkit.getScheduler().runTask(plugin, () -> {
            UUID uuid = player.getUniqueId();
            EditSession.Session s = plugin.getEditSessionManager().get(uuid);
            if (s == null) return;

            Integer chanceSlot = s.getEditingChanceSlot();
            Integer commandSlot = s.getEditingCommandSlot();
            if (chanceSlot == null && commandSlot == null) return;

            if (msg.equalsIgnoreCase("anuluj")) {
                if (chanceSlot != null) {
                    plugin.getEditSessionManager().endChanceEdit(uuid);
                    lang().chanceEditCancelled.send(player);
                }
                if (commandSlot != null) {
                    plugin.getEditSessionManager().endCommandEdit(uuid);
                    lang().commandEditCancelled.send(player);
                }
                openEditInv(player, s);
                return;
            }

            if (chanceSlot != null) {
                handleChanceEdit(player, uuid, chanceSlot, msg);
            } else {
                handleCommandEdit(player, uuid, commandSlot, msg);
            }
        });
    }

    private void handleChanceEdit(Player player, UUID uuid, int slot, String msg) {
        Lang lang = lang();
        double chance;
        try {
            chance = Double.parseDouble(msg.replace(',', '.'));
        } catch (NumberFormatException ex) {
            lang.chanceEditInvalidNumber.send(player);
            return;
        }

        if (chance < 1.0 || chance > 100.0) {
            lang.chanceEditOutOfRange.send(player);
            return;
        }

        EditSession.RewardSettings current = plugin.getEditSessionManager().getRewardSettings(uuid, slot);
        plugin.getEditSessionManager().putRewardSettings(uuid, slot, chance,
                current != null ? current.commands : List.of(),
                current == null || current.giveItem);

        plugin.getEditSessionManager().endChanceEdit(uuid);
        lang.chanceEditSuccess.send(player, Map.of("chance", format(chance)));

        EditSession.Session session = plugin.getEditSessionManager().get(uuid);
        if (session != null) openEditInv(player, session);
    }

    private void handleCommandEdit(Player player, UUID uuid, int slot, String msg) {
        String input = msg == null ? "" : msg.trim();
        List<String> commands = input.isEmpty()
                || input.equalsIgnoreCase("brak")
                ? List.of() : List.of(input);

        EditSession.RewardSettings current = plugin.getEditSessionManager().getRewardSettings(uuid, slot);
        plugin.getEditSessionManager().putRewardSettings(uuid, slot,
                current != null ? current.chance : 100.0,
                commands,
                current == null || current.giveItem);

        plugin.getEditSessionManager().endCommandEdit(uuid);
        lang().commandEditSuccess.send(player);

        EditSession.Session session = plugin.getEditSessionManager().get(uuid);
        if (session != null) openEditInv(player, session);
    }

    private void openEditInv(Player player, EditSession.Session session) {
        session.setReopening(true);
        new EditInv(plugin, plugin.getCrateService(), plugin.getCrateLoader()).open(player, session.crate());
    }

    private Lang lang() {
        return plugin.getConfigService().getLangConfig();
    }

    private String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }
}