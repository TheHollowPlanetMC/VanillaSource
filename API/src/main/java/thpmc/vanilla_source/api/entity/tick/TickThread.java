package thpmc.vanilla_source.api.entity.tick;

import org.bukkit.Bukkit;
import org.contan_lang.ContanEngine;
import org.contan_lang.thread.ContanTickBasedThread;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.TickBase;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.world.cache.local.ThreadLocalCache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class TickThread implements Runnable, ContanTickBasedThread {
    
    private static final Set<TickThread> TICK_THREADS = ConcurrentHashMap.newKeySet();
    
    public static void removeTrackers(EnginePlayer enginePlayer){
        for(TickThread tickThread : TICK_THREADS){
            tickThread.removeTracker(enginePlayer);
        }
    }
    
    
    public static final int TPS = 20;
    public static final long TIME = 1000 / TPS;

    //tick executor
    private final ExecutorService tickExecutor = Executors.newSingleThreadExecutor();

    private final Set<EngineEntity> entities = new HashSet<>();
    
    private final Set<TickBase> tickOnlyEntities = new HashSet<>();
    
    private final Set<TickBase> addEntities = new HashSet<>();
    
    private final ReentrantLock ADD_LOCK = new ReentrantLock();
    
    private final ThreadLocalCache threadLocalCache = new ThreadLocalCache(this);
    
    private boolean isStopped = false;
    
    private final int ID;
    
    public TickThread(int ID){this.ID = ID;}
    
    public int getRunnerID() {return ID;}
    
    /**
     * Get cache of thread-local worlds.
     * @return {@link ThreadLocalCache}
     */
    public ThreadLocalCache getThreadLocalCache() {return threadLocalCache;}
    
    
    public void addEntity(TickBase tickBaseEntity){
        try {
            ADD_LOCK.lock();
            this.addEntities.add(tickBaseEntity);
        }finally {
            ADD_LOCK.unlock();
        }
    }
    
    private int i = 0;
    
    private long lastTickMS = System.currentTimeMillis();
    
    private long beforeTime = System.currentTimeMillis();
    
    private double tps = TPS;
    
    public long getLastTickMS() {return lastTickMS;}
    
    public double getTPS() {return tps;}
    
    private Thread currentThread = Thread.currentThread();
    
    public void removeEngineEntityUnsafe(EngineEntity entity) {entities.remove(entity);}
    
    /**
     * Gets the current thread executing tick.
     * @return {@link Thread}
     */
    public Thread getCurrentThread() {return currentThread;}
    
    
    private final Map<EnginePlayer, EntityTracker> trackerMap = new HashMap<>();
    
    public EntityTracker getEntityTracker(EnginePlayer enginePlayer){
        return trackerMap.computeIfAbsent(enginePlayer, ep -> new EntityTracker(this, ep));
    }
    
    private void removeTracker(EnginePlayer enginePlayer){trackerMap.remove(enginePlayer);}
    
    @Override
    public void run() {
        if(beforeTime + TIME - 20 > System.currentTimeMillis()) return;
        
        if(isStopped) return;
        
        if(i % TPS == 0){
            long time = System.currentTimeMillis();
            tps = ((double)Math.round((20.0 / (((double) (time - beforeTime)) / 1000.0)) * 10))/10;
            beforeTime = time;
        }
        
        currentThread = Thread.currentThread();
        
        this.lastTickMS = System.currentTimeMillis();
        
        //Should remove check
        entities.removeIf(entity -> {
            boolean shouldRemove = entity.shouldRemove();
            if (shouldRemove) {
                for (EnginePlayer enginePlayer : EnginePlayer.getAllPlayers()) {
                    EntityTracker entityTracker = getEntityTracker(enginePlayer);
                    entityTracker.removeTrackerEntity(entity);
                    entity.hide(enginePlayer);
                }
            }
            return shouldRemove;
        });
        tickOnlyEntities.removeIf(TickBase::shouldRemove);
    
    
        boolean forceTrack = false;
        
        //Add entities
        if(addEntities.size() != 0) {
            try {
                ADD_LOCK.lock();
        
                for (TickBase entity : addEntities) {
                    if (entity instanceof EngineEntity) {
                        entities.add((EngineEntity) entity);
                        forceTrack = true;
                    } else {
                        tickOnlyEntities.add(entity);
                    }
                }
        
                addEntities.clear();
            } finally {
                ADD_LOCK.unlock();
            }
        }
        
        //tick
        tickOnlyEntities.forEach(TickBase::tick);
        entities.forEach(TickBase::tick);
    
        //Tracker
        for(EnginePlayer enginePlayer : EnginePlayer.getAllPlayers()){
            EntityTracker entityTracker = getEntityTracker(enginePlayer);
            entityTracker.tick(entities, forceTrack);
        }
        if(i % 40 == 0){
            //Remove offline player
            trackerMap.keySet().removeIf(enginePlayer -> !enginePlayer.getBukkitPlayer().isOnline());
        }
        
        entities.forEach(EngineEntity::setPreviousPosition);
        i++;
    }
    
    public void cancel() {
        isStopped = true;
        MainThreadTimer.instance.removeTickRunner(this);
        tickExecutor.shutdown();
    }

    public void start(){MainThreadTimer.instance.addTickRunner(this);}

    public void tickAtAsync(){
        tickExecutor.submit(() -> {
            try {
                this.run();
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable, long l) {
        Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), () -> tickExecutor.submit(callable), l);
    }
    
    @Override
    public <T> T runTaskImmediately(Callable<T> callable) throws ExecutionException, InterruptedException {
        return tickExecutor.submit(callable).get();
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable) {
        tickExecutor.submit(callable);
    }
    
    @Override
    public boolean shutdownWithAwait(long l, TimeUnit timeUnit) throws InterruptedException {
        return true;
    }
    
    @Override
    public ContanEngine getContanEngine() {
        return VanillaSourceAPI.getInstance().getContanEngine();
    }
}

