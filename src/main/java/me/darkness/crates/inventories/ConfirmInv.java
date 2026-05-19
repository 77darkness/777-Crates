package me.darkness.crates.inventories;

import dev.darkness.utilities.item.ItemBuilder;
import dev.darkness.utilities.task.SchedulerUtil;
import dev.darkness.utilities.text.TextUtil;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.inventories.ConfirmInvConfig;
import me.darkness.crates.configuration.LangConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.battle.BattleService;
import me.darkness.crates.crate.battle.BattleSession;
import me.darkness.crates.crate.battle.Challenge;
import me.darkness.crates.crate.battle.OpenChallenge;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
        BattleSession session = service.getSession(challenger.getUniqueId());
        if (session == null) return;

        Player target = plugin.getServer().getPlayer(session.getOpponent());
        if (target == null) { challenger.closeInventory(); return; }

        Crate crate = plugin.getCrateService().getCrate(session.getCrateName()).orElse(null);
        if (crate == null) { challenger.closeInventory(); return; }

        openInternal(challenger, target, crate, session.getAmount(), true, null);
    }

    public void openForAccept(Player target, Player challenger, Challenge challenge) {
        Crate crate = plugin.getCrateService().getCrate(challenge.getCrateName()).orElse(null);
        if (crate == null) { target.closeInventory(); return; }

        openInternal(target, challenger, crate, challenge.getAmount(), false, challenge);
    }

    public void openForCreateOpen(Player creator) {
        BattleSession session = service.getSession(creator.getUniqueId());
        if (session == null) return;

        Crate crate = plugin.getCrateService().getCrate(session.getCrateName()).orElse(null);
        if (crate == null) { creator.closeInventory(); return; }

        openInternalOpen(creator, crate, session.getAmount(), true, null);
    }

    public void openForJoinOpen(Player joiner, Crate crate, OpenChallenge battle) {
        openInternalOpen(joiner, crate, battle.getAmount(), false, battle);
    }

    private void openInternalOpen(Player viewer, Crate crate, int amount, boolean isCreate, OpenChallenge battle) {
        ConfirmInvConfig cfg = this.plugin.getConfigService().confirmInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Map<String, String> ph = Map.of(
                "crate", crate.getDisplayName(),
                "amount", String.valueOf(amount),
                "opponent", getOpponentName(isCreate, battle),
                "need", String.valueOf(amount)
        );

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(isCreate ? cfg.titleChallenge : cfg.titleAccept))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.infoItem != null && cfg.infoItem.slot >= 0 && cfg.infoItem.slot < size) {
            ItemStack icon = ItemBuilder.of(cfg.infoItem.material)
                    .amount(cfg.infoItem.amount)
                    .placeholders(ph)
                    .name(cfg.infoItem.name)
                    .lore(cfg.infoItem.lore)
                    .build();
            gui.setItem(cfg.infoItem.slot, new GuiItem(icon));
        }

        if (cfg.items != null) {
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                ItemStack stack = ItemBuilder.of(item.material)
                        .amount(item.amount)
                        .placeholders(ph)
                        .name(item.name)
                        .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                        .build();
                gui.setItem(item.slot, new GuiItem(stack));
                gui.addSlotAction(item.slot, event -> handleOpenAction(viewer, crate, amount, isCreate, battle, action));
            });
        }

        gui.open(viewer);
    }

    private void handleOpenAction(Player viewer, Crate crate, int amount, boolean isCreate,
                                   OpenChallenge battle, String action) {
        if ("NONE".equals(action)) return;
        if ("DECLINE".equals(action)) {
            viewer.closeInventory();
            return;
        }
        if (!"ACCEPT".equals(action)) return;

        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);

        if (isCreate) {
            int have = plugin.getKeyService().countKeys(viewer, crate.getName());
            if (have < amount) {
                service.noKeys(viewer, have, amount);
                viewer.closeInventory();
                return;
            }
            if (plugin.getRewardExecutor().countFreeSlots(viewer) < amount) {
                lang().inventoryFull.send(viewer);
                viewer.closeInventory();
                return;
            }
            OpenChallenge open = new OpenChallenge(viewer.getUniqueId(), crate.getName(), amount);
            service.addOpenChallenge(open);
            lang().openBattleCreated.send(viewer, Map.of("crate", crate.getDisplayName(), "amount", String.valueOf(amount)));
            viewer.closeInventory();
        } else {
            if (battle == null) { viewer.closeInventory(); return; }
            Player creator = plugin.getServer().getPlayer(battle.getCreator());
            if (creator == null) {
                service.removeOpenChallenge(battle.getId());
                lang().openBattleNoLongerAvailable.send(viewer);
                viewer.closeInventory();
                return;
            }
            viewer.closeInventory();
            SchedulerUtil.run(plugin, () ->
                    service.startMatchFromOpen(battle, viewer));
        }
    }

    private void openInternal(Player viewer, Player opponent, Crate crate, int amount, boolean isSend, Challenge challenge) {
        ConfirmInvConfig cfg = this.plugin.getConfigService().confirmInv();
        int rows = Math.max(1, Math.min(6, cfg.rows));
        int size = rows * 9;

        Map<String, String> ph = Map.of(
                "crate", crate.getDisplayName(),
                "amount", String.valueOf(amount),
                "opponent", opponent.getName(),
                "need", String.valueOf(amount)
        );

        Gui gui = Gui.gui()
                .title(TextUtil.toComponent(isSend ? cfg.titleChallenge : cfg.titleAccept))
                .rows(rows)
                .disableAllInteractions()
                .create();

        if (cfg.infoItem != null && cfg.infoItem.slot >= 0 && cfg.infoItem.slot < size) {
            ItemStack icon = ItemBuilder.of(cfg.infoItem.material)
                    .amount(cfg.infoItem.amount)
                    .placeholders(ph)
                    .name(cfg.infoItem.name)
                    .lore(cfg.infoItem.lore)
                    .build();
            gui.setItem(cfg.infoItem.slot, new GuiItem(icon));
        }

        if (cfg.items != null) {
            cfg.items.values().forEach(item -> {
                if (item == null || item.slot < 0 || item.slot >= size) return;
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);
                ItemStack stack = ItemBuilder.of(item.material)
                        .amount(item.amount)
                        .placeholders(ph)
                        .name(item.name)
                        .lore(item.lore != null ? new ArrayList<>(item.lore) : new ArrayList<>())
                        .build();
                gui.setItem(item.slot, new GuiItem(stack));
                gui.addSlotAction(item.slot, event -> handleAction(viewer, opponent, crate, amount, isSend, challenge, action));
            });
        }

        gui.open(viewer);
    }

    private void handleAction(Player viewer, Player opponent, Crate crate, int amount,
                              boolean isSend, Challenge challenge, String action) {
        if ("NONE".equals(action)) return;
        if ("DECLINE".equals(action)) {
            viewer.closeInventory();
            return;
        }

        if (!"ACCEPT".equals(action)) return;

        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);

        if (isSend) {
            handleSend(viewer, opponent, crate, amount);
        } else {
            handleAccept(viewer, opponent, challenge);
        }
    }

    private void handleSend(Player viewer, Player opponent, Crate crate, int amount) {
        LangConfig langConfig = lang();
        if (!service.createChallenge(viewer, opponent, crate.getName(), amount)) {
            langConfig.battleChallengeFailed.send(viewer);
            viewer.closeInventory();
            return;
        }

        langConfig.battleChallengeSent.send(viewer, Map.of("player", opponent.getName()));
        TextUtil.sendClickableMsg(opponent,
                TextUtil.applyPlaceholders(langConfig.battleChallengeReceived.message.toString(),
                        Map.of("player", viewer.getName())),
                "/bitwa akceptuj " + viewer.getName()
        );
        viewer.closeInventory();
    }

    private void handleAccept(Player viewer, Player opponent, Challenge challenge) {
        LangConfig langConfig = lang();
        
        if (service.isExpired(challenge)) {
            service.cancelChallenge(challenge, Challenge.Status.EXPIRED);
            langConfig.battleChallengeExpired.send(viewer);
            viewer.closeInventory();
            return;
        }


        challenge.setStatus(Challenge.Status.ACCEPTED);
        langConfig.battleAccepted.send(viewer, Map.of("player", opponent.getName()));
        viewer.closeInventory();
        SchedulerUtil.run(plugin, () -> service.startCountdown(challenge));
    }

    private String getOpponentName(boolean isCreate, OpenChallenge battle) {
        if (isCreate) return "Otwarta bitwa";
        if (battle == null) return "?";

        String name = plugin.getServer().getOfflinePlayer(battle.getCreator()).getName();
        return (name != null) ? name : "?";
    }

    private LangConfig lang() {
        return plugin.getConfigService().lang();
    }
}

