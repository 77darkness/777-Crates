package me.darkness.crates.listeners;

import dev.darkness.utilities.math.NumberUtil;
import dev.darkness.utilities.task.SchedulerUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.edit.EditSession;
import me.darkness.crates.inventories.EditInv;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ChatInputListener implements Listener {

    private final CratesPlugin plugin;

    public ChatInputListener(CratesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (msg.equalsIgnoreCase("anuluj") && plugin.getBattleService() != null) {
            var countdown = plugin.getBattleService().getCountdown(uuid);
            if (countdown != null) {
                event.setCancelled(true);
                SchedulerUtil.run(plugin, () -> countdown.tryCancel(uuid));
                return;
            }
        }

        EditSession.Session session = plugin.getEditSessionManager().get(uuid);
        if (session == null || session.getActiveEdit() == null) return;

        event.setCancelled(true);

        SchedulerUtil.run(plugin, () -> {
            EditSession.Session s = plugin.getEditSessionManager().get(uuid);
            if (s == null || s.getActiveEdit() == null) return;

            EditSession.EditTarget target = s.getActiveEdit();

            if (msg.equalsIgnoreCase("anuluj")) {
                s.endEdit();
                if (target.type() == EditSession.EditType.CHANCE) {
                    lang().chanceEditCancelled.send(player);
                } else {
                    lang().commandEditCancelled.send(player);
                }
                reopenEditInv(player, s);
                return;
            }

            if (target.type() == EditSession.EditType.CHANCE) {
                handleChanceEdit(player, s, target.slot(), msg);
            } else {
                handleCommandEdit(player, s, target.slot(), msg);
            }
        });
    }

    private void handleChanceEdit(Player player, EditSession.Session session, int slot, String msg) {
        LangConfig langConfig = lang();
        double chance;
        try {
            chance = Double.parseDouble(msg.replace(',', '.'));
        } catch (NumberFormatException ex) {
            langConfig.chanceEditInvalidNumber.send(player);
            return;
        }

        if (chance <= 0.0) {
            langConfig.chanceEditOutOfRange.send(player);
            return;
        }

        EditSession.RewardSettings current = session.getRewardSettings(slot);
        session.putRewardSettings(slot, chance,
                current != null ? current.commands : List.of(),
                current == null || current.giveItem);
        session.endEdit();
        langConfig.chanceEditSuccess.send(player, Map.of("chance", NumberUtil.formatCompact(chance)));
        reopenEditInv(player, session);
    }

    private void handleCommandEdit(Player player, EditSession.Session session, int slot, String msg) {
        String input = msg == null ? "" : msg.trim();
        List<String> commands = input.isEmpty() || input.equalsIgnoreCase("brak")
                ? List.of() : List.of(input);

        EditSession.RewardSettings current = session.getRewardSettings(slot);
        session.putRewardSettings(slot,
                current != null ? current.chance : 100.0,
                commands,
                current == null || current.giveItem);
        session.endEdit();
        lang().commandEditSuccess.send(player);
        reopenEditInv(player, session);
    }

    private void reopenEditInv(Player player, EditSession.Session session) {
        session.setReopening(true);
        new EditInv(plugin, plugin.getCrateService(), plugin.getCrateLoader()).open(player, session.crate());
    }

    private LangConfig lang() {
        return plugin.getConfigService().lang();
    }
}