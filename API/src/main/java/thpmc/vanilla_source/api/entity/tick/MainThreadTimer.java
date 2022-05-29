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


    private final Set<TickThread> tickThreads = ConcurrentHashMap.newKeySet();

    public void addTickRunner(TickThread tickThread){this.tickThreads.add(tickThread);}

    public void removeTickRunner(TickThread tickThread){this.tickThreads.remove(tickThread);}

    @Override
    public void run() {
        tickThreads.forEach(TickThread::tickAtAsync);
    }
}
