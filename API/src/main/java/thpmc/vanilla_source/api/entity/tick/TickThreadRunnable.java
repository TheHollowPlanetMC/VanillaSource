package thpmc.vanilla_source.api.entity.tick;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import thpmc.vanilla_source.api.VanillaSourceAPI;

public abstract class TickThreadRunnable implements Runnable {
    
    private final TickThread tickThread;
    
    public TickThreadRunnable(TickThread tickThread) {
        this.tickThread = tickThread;
    }
    
    
    private boolean isCanceled = false;
    
    public void cancel() {this.isCanceled = true;}
    
    public void runTask() {
        tickThread.scheduleTask(() -> {
            run();
            return null;
        });
    }
    
    public void runTaskLater(long delay) {
        Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), () -> {
            tickThread.scheduleTask(() -> {
                TickThreadRunnable.this.run();
                return null;
            });
        }, delay);
    }
    
    public void runTaskTimer(long delay, long period) {
        TickThreadRunnable task = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (task.isCanceled) {
                    cancel();
                }
                task.run();
            }
        }.runTaskTimer(VanillaSourceAPI.getInstance().getPlugin(), delay, period);
    }
    
}
