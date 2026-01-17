package me.darkness.crates.crate.animation;

import me.darkness.crates.CratesPlugin;
import me.darkness.crates.crate.Crate;
import me.darkness.crates.crate.reward.CrateReward;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public abstract class CrateAnimation {

    protected final CratesPlugin plugin;
    protected final Player player;
    protected final Crate crate;
    protected final CrateReward reward;
    protected BukkitTask task;

    private boolean finished = false;

    protected CrateAnimation(CratesPlugin plugin, Player player, Crate crate, CrateReward reward) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.reward = reward;
    }

    public abstract void start();

    public void cancel() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }

    public boolean isAnimation(org.bukkit.inventory.Inventory inventory) {
        return false;
    }

    public final void cancelAnimation() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        this.cancel();
        this.onCancel();
        this.plugin.getAnimationService().removeAnimation(this.player);
    }

    protected void finish() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        this.onFinish();
        this.plugin.getAnimationService().removeAnimation(this.player);
    }

    protected void onFinish() {
        if (this.reward != null) {
            this.plugin.getRewardExecutor().giveReward(this.player, this.crate != null ? this.crate.getName() : "", this.reward);
        }
    }

    protected void onCancel() {
        if (this.reward != null) {
            this.plugin.getRewardExecutor().giveReward(this.player, this.crate != null ? this.crate.getName() : "", this.reward);
        }
    }
}
