package me.darkness.crates.listener;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.inv.EditInv;
import me.darkness.crates.crate.edit.EditSession;
import me.darkness.crates.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ChatInputListener implements Listener {

    private final CratesPlugin plugin;

    public ChatInputListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        EditSession.Session session = EditSession.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        Integer chanceSlot = session.getEditingChanceSlot();
        Integer commandSlot = session.getEditingCommandSlot();
        if (chanceSlot == null && commandSlot == null) {
            return;
        }

        event.setCancelled(true);
        String msg = event.getMessage().trim();

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            EditSession.Session s = EditSession.get(player.getUniqueId());
            if (s == null) {
                return;
            }

            Integer currentChanceSlot = s.getEditingChanceSlot();
            Integer currentCommandSlot = s.getEditingCommandSlot();
            if (currentChanceSlot == null && currentCommandSlot == null) {
                return;
            }

            if (msg.equalsIgnoreCase("anuluj")) {
                if (currentChanceSlot != null) {
                    EditSession.endChanceEdit(player.getUniqueId());
                    TextUtil.send(
                            this.plugin.getConfigService().getLangConfig(),
                            this.plugin,
                            player,
                            this.plugin.getConfigService().getLangConfig().chanceEditCancelled
                    );
                }
                if (currentCommandSlot != null) {
                    EditSession.endCommandEdit(player.getUniqueId());
                    TextUtil.send(
                            this.plugin.getConfigService().getLangConfig(),
                            this.plugin,
                            player,
                            this.plugin.getConfigService().getLangConfig().commandEditCancelled
                    );
                }

                new EditInv(this.plugin, this.plugin.getCrateService(), this.plugin.getCrateLoader())
                        .open(player, s.crate());
                return;
            }

            if (currentChanceSlot != null) {
                handleChanceEdit(player, currentChanceSlot, msg);
                return;
            }

            handleCommandEdit(player, currentCommandSlot, msg);
        });
    }

    private void handleChanceEdit(Player player, int slot, String msg) {
        double chance;
        try {
            chance = Double.parseDouble(msg.replace(',', '.'));
        } catch (Exception ex) {
            TextUtil.send(
                    this.plugin.getConfigService().getLangConfig(),
                    this.plugin,
                    player,
                    this.plugin.getConfigService().getLangConfig().chanceEditInvalidNumber
            );
            return;
        }

        if (chance < 1.0 || chance > 100.0) {
            TextUtil.send(
                    this.plugin.getConfigService().getLangConfig(),
                    this.plugin,
                    player,
                    this.plugin.getConfigService().getLangConfig().chanceEditOutOfRange
            );
            return;
        }

        EditSession.RewardSettings current = EditSession.getRewardSettings(player.getUniqueId(), slot);
        List<String> commands = current != null ? current.commands : List.of();
        boolean giveItem = current == null || current.giveItem;

        EditSession.putRewardSettings(player.getUniqueId(), slot, chance, commands, giveItem);
        EditSession.endChanceEdit(player.getUniqueId());

        TextUtil.send(
                this.plugin.getConfigService().getLangConfig(),
                this.plugin,
                player,
                this.plugin.getConfigService().getLangConfig().chanceEditSuccess,
                Map.of("chance", format(chance))
        );

        EditSession.Session session = EditSession.get(player.getUniqueId());
        if (session != null) {
            new EditInv(this.plugin, this.plugin.getCrateService(), this.plugin.getCrateLoader())
                    .open(player, session.crate());
        }
    }

    private void handleCommandEdit(Player player, int slot, String msg) {
        List<String> commands;
        String input = msg == null ? "" : msg.trim();
        if (input.isEmpty() || input.equalsIgnoreCase("brak") || input.equalsIgnoreCase("none")) {
            commands = List.of();
        } else {
            commands = List.of(input);
        }

        EditSession.RewardSettings current = EditSession.getRewardSettings(player.getUniqueId(), slot);
        double chance = current != null ? current.chance : 100.0;
        boolean giveItem = current == null || current.giveItem;

        EditSession.putRewardSettings(player.getUniqueId(), slot, chance, commands, giveItem);
        EditSession.endCommandEdit(player.getUniqueId());

        TextUtil.send(
                this.plugin.getConfigService().getLangConfig(),
                this.plugin,
                player,
                this.plugin.getConfigService().getLangConfig().commandEditSuccess
        );

        EditSession.Session session = EditSession.get(player.getUniqueId());
        if (session != null) {
            new EditInv(this.plugin, this.plugin.getCrateService(), this.plugin.getCrateLoader())
                    .open(player, session.crate());
        }
    }

    private String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
