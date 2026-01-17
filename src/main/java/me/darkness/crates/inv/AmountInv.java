package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.AmountInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.BattleSession;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
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
        BattleSession battleSession = service.getSession(challenger.getUniqueId());
        if (battleSession == null || battleSession.getCrateName() == null) {
            return;
        }

        Crate crate = service.findCrate(battleSession.getCrateName()).orElse(null);
        if (crate == null) {
            return;
        }

        final AmountInvConfig cfg = this.plugin.getConfigService().getBattleAmountInv();
        final int rows = Math.max(1, Math.min(6, cfg.rows));
        final int size = rows * 9;

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(cfg.title.replace("{crate}", crate.getDisplayName()))))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.items != null) {
            for (AmountInvConfig.GuiItem item : cfg.items.values()) {
                if (item == null) continue;
                if (item.slot < 0 || item.slot >= size) continue;

                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);

                ItemStack stack = ItemBuilder.of(item.material != null ? item.material : Material.BARRIER)
                        .amount(item.amount)
                        .placeholders(Map.of(
                                "crate", crate.getDisplayName(),
                                "opponent", String.valueOf(battleSession.getOpponent())
                        ))
                        .name(item.name)
                        .lore(item.lore)
                        .build();

                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);

                    if ("BACK".equals(action)) {
                        Player target = plugin.getServer().getPlayer(battleSession.getOpponent());
                        if (target != null) {
                            new SelectInv(plugin, service).open(challenger, target);
                        } else {
                            challenger.closeInventory();
                        }
                        return;
                    }

                    if (action.startsWith("AMOUNT_")) {
                        int amount;
                        try {
                            amount = Integer.parseInt(action.substring("AMOUNT_".length()));
                        } catch (Exception ex) {
                            return;
                        }

                        amount = Math.max(1, Math.min(6, amount));

                        KeyService ks = this.plugin.getKeyServiceProvider().get();
                        int have = ks.countKeys(challenger, crate.getName());
                        if (have < amount) {
                            service.noKeys(challenger, have, amount);
                            challenger.playSound(challenger.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            return;
                        }

                        challenger.playSound(challenger.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                        battleSession.setAmount(amount);
                        new ConfirmInv(plugin, service).openForChallenge(challenger);
                    }
                }));
            }
        }

        gui.open(challenger);
    }
}
