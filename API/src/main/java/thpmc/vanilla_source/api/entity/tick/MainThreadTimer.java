package thpmc.vanilla_source.api.entity.tick;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main thread timer for async tick runner.
 */
public class MainThreadTimer extends BukkitRunnable {

    public static final MainThreadTimer instance;

    static {
        instance = new MainThreadTimer();
    }


    private final Set<TickRunner> tickRunners = ConcurrentHashMap.newKeySet();

    public void addTickRunner(TickRunner tickRunner){this.tickRunners.add(tickRunner);}

    public void removeTickRunner(TickRunner tickRunner){this.tickRunners.remove(tickRunner);}

    @Override
    public void run() {
        tickRunners.forEach(TickRunner::tickAtAsync);
    }
}
