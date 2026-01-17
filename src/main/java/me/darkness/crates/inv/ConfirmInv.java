package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.ConfirmInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.Challenge;
import me.darkness.crates.crate.battle.BattleSession;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ConfirmInv {

    private final CratesPlugin plugin;
    private final BattleService service;

    public ConfirmInv(CratesPlugin plugin, BattleService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void openForChallenge(Player challenger) {
        BattleSession battleSession = service.getSession(challenger.getUniqueId());
        if (battleSession == null) return;

        Player target = plugin.getServer().getPlayer(battleSession.getOpponent());
        if (target == null) {
            challenger.closeInventory();
            return;
        }

        Crate crate = service.findCrate(battleSession.getCrateName()).orElse(null);
        if (crate == null) {
            challenger.closeInventory();
            return;
        }

        openInternal(challenger, target, crate, battleSession.getAmount(), true, null);
    }

    public void openForAccept(Player target, Player challenger, Challenge challenge) {
        Crate crate = service.findCrate(challenge.getCrateName()).orElse(null);
        if (crate == null) {
            target.closeInventory();
            return;
        }

        openInternal(target, challenger, crate, challenge.getAmount(), false, challenge);
    }

    private void openInternal(Player viewer, Player opponent, Crate crate, int amount, boolean isSend, Challenge challenge) {
        final ConfirmInvConfig cfg = this.plugin.getConfigService().getBattleConfirmInv();
        final int rows = Math.max(1, Math.min(6, cfg.rows));
        final int size = rows * 9;

        String title = isSend ? cfg.titleChallenge : cfg.titleAccept;

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(title)))
                .rows(rows)
                .disableAllInteractions()
                .create();

        Map<String, String> ph = new HashMap<>();
        ph.put("crate", crate.getDisplayName());
        ph.put("amount", String.valueOf(amount));
        ph.put("opponent", opponent.getName());
        ph.put("need", String.valueOf(amount));

        if (cfg.infoItem != null) {
            int slot = cfg.infoItem.slot;
            if (slot >= 0 && slot < size) {
                ItemStack icon = ItemBuilder.of(cfg.infoItem.material != null ? cfg.infoItem.material : Material.CHEST)
                        .amount(cfg.infoItem.amount)
                        .placeholders(ph)
                        .name(cfg.infoItem.name)
                        .lore(cfg.infoItem.lore)
                        .build();

                gui.setItem(slot, new GuiItem(icon, e -> e.setCancelled(true)));
            }
        }

        if (cfg.items != null) {
            for (ConfirmInvConfig.GuiItem item : cfg.items.values()) {
                if (item == null) continue;
                if (item.slot < 0 || item.slot >= size) continue;

                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);

                ItemStack stack = ItemBuilder.of(item.material != null ? item.material : Material.BARRIER)
                        .amount(item.amount)
                        .placeholders(ph)
                        .name(item.name)
                        .lore(item.lore)
                        .build();

                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);

                    switch (action) {
                        case "NONE" -> {
                        }
                        case "DECLINE" -> viewer.closeInventory();
                        case "ACCEPT" -> {
                            viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);

                            if (isSend) {
                                boolean ok = service.createChallenge(viewer, opponent, crate.getName(), amount);
                                if (!ok) {
                                    TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, viewer,
                                            this.plugin.getConfigService().getLangConfig().battleChallengeFailed
                                    );
                                    viewer.closeInventory();
                                    return;
                                }

                                TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, viewer,
                                        this.plugin.getConfigService().getLangConfig().battleChallengeSent,
                                        Map.of("player", opponent.getName())
                                );
                                TextUtil.sendClickableMsg(
                                        this.plugin.getConfigService().getLangConfig(),
                                        this.plugin,
                                        opponent,
                                        this.plugin.getConfigService().getLangConfig().battleChallengeReceived,
                                        Map.of("player", viewer.getName()),
                                        "/bitwa akceptuj " + viewer.getName()
                                );
                                viewer.closeInventory();
                                return;
                            }

                            if (challenge == null) {
                                viewer.closeInventory();
                                return;
                            }

                            if (service.isExpired(challenge)) {
                                service.cancelChallenge(challenge, Challenge.Status.EXPIRED);
                                TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, viewer,
                                        this.plugin.getConfigService().getLangConfig().battleChallengeExpired
                                );
                                viewer.closeInventory();
                                return;
                            }

                            KeyService ks = this.plugin.getKeyServiceProvider().get();
                            int have = ks.countKeys(viewer, crate.getName());
                            if (have < amount) {
                                service.noKeys(viewer, have, amount);
                                viewer.playSound(viewer.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                                return;
                            }

                            challenge.setStatus();
                            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, viewer,
                                    this.plugin.getConfigService().getLangConfig().battleAccepted,
                                    Map.of("player", opponent.getName())
                            );
                            viewer.closeInventory();

                            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.service.startMatch(challenge));
                        }
                    }

                }));
            }
        }

        gui.open(viewer);
    }
}
