package thpmc.vanilla_source.api.contan;

import org.bukkit.Bukkit;
import org.contan_lang.ContanEngine;
import org.contan_lang.thread.ContanTickBasedThread;
import org.contan_lang.variables.primitive.ContanFunctionExpression;
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
        ThreadFutureTask<T> task = new ThreadFutureTask<>(callable);
        ThreadUtil.runAtMainThreadLater(task, l);
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
    
    public void runTaskLater(ContanFunctionExpression functionExpression, long delay) {
        Runnable task = () -> {
            functionExpression.eval(this, functionExpression.getBasedJavaObject().getFunctionName());
        };
        Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), task, delay);
    }
    
    public void runTaskTimer(ContanFunctionExpression functionExpression, long delay, long period) {
        Runnable task = () -> {
            functionExpression.eval(this, functionExpression.getBasedJavaObject().getFunctionName());
        };
        Bukkit.getScheduler().runTaskTimer(VanillaSourceAPI.getInstance().getPlugin(), task, delay, period);
    }
    
    @Override
    public <T> void scheduleTask(Callable<T> callable) {
        ThreadFutureTask<T> task = new ThreadFutureTask<>(callable);
        ThreadUtil.runAtMainThread(task);
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
