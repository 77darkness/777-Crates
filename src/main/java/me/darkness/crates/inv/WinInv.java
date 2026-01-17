package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.WinInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.key.KeyService;
import me.darkness.crates.crate.reward.CrateReward;
import me.darkness.crates.crate.reward.RewardRoller;
import me.darkness.crates.util.ItemBuilder;
import me.darkness.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Map;

public final class WinInv {

    private static final String WIN_SOUND = "ENTITY_PLAYER_LEVELUP";

    private final CratesPlugin plugin;
    private final KeyService keyService;

    public WinInv(CratesPlugin plugin, KeyService keyService) {
        this.plugin = plugin;
        this.keyService = keyService;
    }

    public void open(Player player, Crate crate, CrateReward reward) {
        WinInvConfig cfg =
                this.plugin.getConfigService().getWinInv();

        final var config = plugin.getConfigService().getWinInv();
        final int rows = Math.max(1, Math.min(6, config.rows));
        int rewardSlot = getRewardSlot(cfg);

        Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(config.title)))
                .rows(rows)
                .disableAllInteractions()
                .create();

        placeReward(gui, reward, rewardSlot);
        placeGuiItems(gui, cfg, player, crate);

        this.plugin.getRewardExecutor().giveReward(player, crate != null ? crate.getName() : "", reward);
        playWinSound(player);

        gui.open(player);
    }

    private void placeReward(Gui gui, CrateReward reward, int slot) {
        if (reward.getDisplayItem() == null) {
            return;
        }

        if (slot < 0 || slot >= gui.getRows() * 9) {
            return;
        }

        gui.setItem(slot, new GuiItem(reward.getDisplayItem().clone()));
    }

    private void placeGuiItems(
            Gui gui,
            WinInvConfig cfg,
            Player player,
            Crate crate
    ) {
        if (cfg == null || cfg.items == null) {
            return;
        }

        int size = gui.getRows() * 9;

        cfg.items.values().forEach(item -> {
            if (item == null) {
                return;
            }
            if (item.slot < 0 || item.slot >= size) {
                return;
            }

            ItemStack stack = createItem(item);
            String action = item.action == null
                    ? "NONE"
                    : item.action.toUpperCase(Locale.ROOT);

            gui.setItem(item.slot, new GuiItem(stack, event -> {
                event.setCancelled(true);
                handleAction(action, player, crate);
            }));
        });
    }

    private ItemStack createItem(
            WinInvConfig.GuiItem item
    ) {
        return ItemBuilder.of(item.material != null ? item.material : Material.BARRIER)
                .name(item.name)
                .lore(item.lore)
                .build();
    }

    private void handleAction(String action, Player player, Crate crate) {
        switch (action) {
            case "CLOSE" -> player.closeInventory();

            case "REOPEN" -> {
                if (keyService.tryConsumeKey(player, crate.getName())) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    TextUtil.send(
                            this.plugin.getConfigService().getLangConfig(),
                            this.plugin,
                            player,
                            this.plugin.getConfigService().getLangConfig().noKeyInHand,
                            Map.of(
                                    "crate", crate.getDisplayName(),
                                    "need", "1"
                            )
                    );
                    return;
                }

                CrateReward newReward = RewardRoller.roll(crate.getRewards());
                if (newReward == null) {
                    TextUtil.send(
                        this.plugin.getConfigService().getLangConfig(),
                        this.plugin,
                        player,
                        this.plugin.getConfigService().getLangConfig().crateNoRewards,
                        Map.of("crate", crate.getDisplayName())
                    );
                    player.closeInventory();
                    return;
                }

                open(player, crate, newReward);
            }

            default -> {
            }
        }
    }

    private int getRewardSlot(
            WinInvConfig cfg
    ) {
        return cfg != null ? cfg.rewardSlot : 13;
    }

    private void playWinSound(Player player) {
        try {
            Sound sound = Sound.valueOf(WIN_SOUND.toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
