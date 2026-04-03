package me.darkness.crates.inv;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.AmountInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.BattleSession;
import me.darkness.crates.crate.key.KeyService;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class AmountInv {

    private final CratesPlugin plugin;
    private final BattleService service;

    public AmountInv(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void open(Player challenger) {
        BattleSession session = service.getSession(challenger.getUniqueId());
        if (session == null || session.getCrateName() == null) return;

        Crate crate = service.findCrate(session.getCrateName()).orElse(null);
        if (crate == null) return;

        AmountInvConfig cfg = plugin.getConfigService().getBattleAmountInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(cfg.title.replace("{crate}", crate.getDisplayName())))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            Map<String, String> ph = Map.of("crate", crate.getDisplayName(), "opponent", String.valueOf(session.getOpponent()));
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                gui.setItem(item.slot, new GuiItem(toItemStack(item, ph), e -> {
                    e.setCancelled(true);
                    handleAction(challenger, crate, session, action);
                }));
            });
        }

        gui.open(challenger);
    }

    private void handleAction(Player challenger, Crate crate, BattleSession session, String action) {
        if ("BACK".equals(action)) {
            Player target = plugin.getServer().getPlayer(session.getOpponent());
            if (target != null) new SelectInv(plugin, service).open(challenger, target);
            else challenger.closeInventory();
            return;
        }

        if (!action.startsWith("AMOUNT_")) return;

        try {
            int amount = Math.max(1, Math.min(6, Integer.parseInt(action.substring(7))));
            KeyService ks = plugin.getKeyService();
            int have = ks.countKeys(challenger, crate.getName());

            if (have < amount) {
                service.noKeys(challenger, have, amount);
                challenger.playSound(challenger.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            challenger.playSound(challenger.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            session.setAmount(amount);
            new ConfirmInv(plugin, service).openForChallenge(challenger);
        } catch (NumberFormatException ignored) {}
    }

    private ItemStack toItemStack(AmountInvConfig.GuiItem item, Map<String, String> placeholders) {
        return ItemBuilder.of(Objects.requireNonNullElse(item.material, Material.BARRIER))
                .amount(item.amount)
                .placeholders(placeholders)
                .name(item.name)
                .lore(item.lore != null ? item.lore : List.of())
                .build();
    }
}
