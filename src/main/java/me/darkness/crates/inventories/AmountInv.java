package me.darkness.crates.inventories;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.task.SchedulerUtil;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.AmountInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.BattleSession;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

public final class AmountInv {

    private final CratesPlugin plugin;
    private final BattleService service;

    public AmountInv(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void open(Player challenger) {
        openInternal(challenger, false);
    }

    public void openForOpen(Player creator) {
        openInternal(creator, true);
    }

    private void openInternal(Player challenger, boolean isOpen) {
        BattleSession session = service.getSession(challenger.getUniqueId());
        if (session == null || session.getCrateName() == null) return;

        Crate crate = plugin.getCrateService().getCrate(session.getCrateName()).orElse(null);
        if (crate == null) return;

        AmountInvConfig cfg = plugin.getConfigService().amountInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title.replace("{crate}", crate.getDisplayName())))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            Map<String, String> ph = Map.of("crate", crate.getDisplayName(), "opponent", "");
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                gui.setItem(item.slot, new GuiItem(toItemStack(item, ph)));
                gui.addSlotAction(item.slot, event -> handleAction(challenger, session, action, isOpen));
            });
        }

        gui.open(challenger);
    }

    private void handleAction(Player challenger, BattleSession session, String action, boolean isOpen) {
        switch (action) {
            case "NONE" -> {}
            case "BACK" -> {
                if (isOpen) {
                    SchedulerUtil.run(plugin, () -> new SelectInv(plugin, service).openForOpen(challenger));
                } else {
                    Player target = plugin.getServer().getPlayer(session.getOpponent());
                    if (target != null) new SelectInv(plugin, service).open(challenger, target);
                    else challenger.closeInventory();
                }
            }
            default -> {
                if (!action.startsWith("AMOUNT_")) return;
                try {
                    int amount = Math.max(1, Math.min(6, Integer.parseInt(action.substring(7))));
                    challenger.playSound(challenger.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                    session.setAmount(amount);
                    if (isOpen) new ConfirmInv(plugin, service).openForCreateOpen(challenger);
                    else new ConfirmInv(plugin, service).openForChallenge(challenger);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private ItemStack toItemStack(AmountInvConfig.GuiItem item, Map<String, String> placeholders) {
        return ItemBuilder.of(item.material)
                .amount(item.amount)
                .placeholders(placeholders)
                .name(item.name)
                .lore(item.lore)
                .build();
    }
}
