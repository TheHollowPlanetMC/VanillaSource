package thpmc.vanilla_source.api.contan;

import org.bukkit.Bukkit;
import org.contan_lang.ContanEngine;
import org.contan_lang.thread.ContanTickBasedThread;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.util.ThreadUtil;

import java.util.concurrent.*;

public class MainTickThread extends TickThread implements ContanTickBasedThread {
    
    public MainTickThread(int ID) {
        super(ID);
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable, long l) {
        FutureTask<T> future = new FutureTask<>(callable);
        ThreadUtil.runAtMainThreadLater(future, l);
    }
    
    @Override
    public <T> T runTaskImmediately(Callable<T> callable) throws ExecutionException, InterruptedException {
        if (ThreadUtil.isMainThread()) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new IllegalStateException("", e);
            }
        } else {
            FutureTask<T> future = new FutureTask<>(callable);
            ThreadUtil.runAtMainThread(future);
            return future.get();
        }
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<>(callable);
        ThreadUtil.runAtMainThread(future);
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
