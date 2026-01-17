package me.darkness.crates.inv;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.darkness.crates.CratesPlugin;
import me.darkness.crates.configuration.Inv.PreviewInvConfig;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.animation.impl.WithoutAnimation;
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

public final class PreviewInv {

    private final CratesPlugin plugin;
    private final KeyService keyService;

    public PreviewInv(CratesPlugin plugin) {
        this.plugin = plugin;
        this.keyService = plugin.getKeyServiceProvider().get();
    }

    public void open(Player player, Crate crate) {
        final PreviewInvConfig cfg = this.plugin.getConfigService().getPreviewInv();

        final int rows = Math.max(1, Math.min(6, cfg.rows));

        final Gui gui = Gui.gui()
                .title(Component.text(TextUtil.color(cfg.title.replace("{crate}", crate.getDisplayName()))))
                .rows(rows)
                .disableAllInteractions()
                .create();

        int size = rows * 9;

        if (cfg.rewardPreviewLore != null) {
            int index = 0;
            for (CrateReward reward : crate.getRewards()) {
                if (reward == null || reward.getDisplayItem() == null) continue;

                while (index < size && gui.getGuiItem(index) != null) {
                    index++;
                }
                if (index >= size) break;

                ItemStack display = reward.getDisplayItem().clone();
                display = ItemBuilder.of(display)
                        .placeholders(Map.of(
                                "chance", String.valueOf(reward.getChance()),
                                "crate", crate.getDisplayName()
                        ))
                        .loreAppend(cfg.rewardPreviewLore)
                        .build();

                gui.setItem(index, new GuiItem(display, e -> e.setCancelled(true)));
                index++;
            }
        }

        if (cfg.items != null) {
            cfg.items.values().forEach(item -> {
                if (item == null) return;
                if (item.slot < 0 || item.slot >= size) return;

                ItemStack stack = createItem(item, crate);
                String action = item.action == null ? "NONE" : item.action.toUpperCase(Locale.ROOT);

                gui.setItem(item.slot, new GuiItem(stack, e -> {
                    e.setCancelled(true);
                    handleAction(player, crate, action);
                }));
            });
        }

        gui.open(player);
    }

    private void handleAction(Player player, Crate crate, String action) {
        switch (action) {
            case "OPEN_ANIMATED" -> openAnimated(player, crate);
            case "OPEN_WITHOUT_ANIMATION" -> WithoutAnimation.openWithoutAnimation(this.plugin, player, crate);
            case "MASS_OPEN" -> new MassOpenInv(this.plugin).open(player, crate);
            case "EDIT" -> new EditInv(this.plugin, this.plugin.getCrateService(), this.plugin.getCrateLoader()).open(player, crate);
            case "BACK" -> player.closeInventory();
            default -> {
            }
        }
    }

    private void openAnimated(Player player, Crate crate) {
        if (crate.getRewards().isEmpty()) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                    this.plugin.getConfigService().getLangConfig().crateNoRewards,
                    Map.of("crate", crate.getDisplayName())
            );
            player.closeInventory();
            return;
        }

        if (this.plugin.getAnimationService().hasActiveAnimation(player)) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (keyService.tryConsumeKey(player, crate.getName())) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                    this.plugin.getConfigService().getLangConfig().noKeyInHand,
                    Map.of(
                            "crate", crate.getDisplayName(),
                            "need", "1"
                    )
            );
            return;
        }

        CrateReward reward = RewardRoller.roll(crate.getRewards());
        if (reward == null) {
            TextUtil.send(this.plugin.getConfigService().getLangConfig(), this.plugin, player,
                    this.plugin.getConfigService().getLangConfig().crateNoRewards,
                    Map.of("crate", crate.getDisplayName())
            );
            player.closeInventory();
            return;
        }

        player.closeInventory();
        this.plugin.getAnimationService().startAnimation(player, crate, reward);
    }

    private ItemStack createItem(PreviewInvConfig.GuiItem item, Crate crate) {
        return ItemBuilder.of(item.material != null ? item.material : Material.BARRIER)
                .placeholders(Map.of("crate", crate.getDisplayName()))
                .name(item.name)
                .lore(item.lore)
                .build();
    }
}
