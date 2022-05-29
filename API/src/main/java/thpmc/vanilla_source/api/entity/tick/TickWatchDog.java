package thpmc.vanilla_source.api.entity.tick;

import thpmc.vanilla_source.api.VanillaSourceAPI;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TickWatchDog implements Runnable{
    
    private final TickThreadPool tickThreadPool;
    
    private boolean isStopped = false;
    
    public TickWatchDog(TickThreadPool tickThreadPool){
        this.tickThreadPool = tickThreadPool;
    }
    
    @Override
    public void run() {
        if(isStopped) return;
        
        long currentTime = System.currentTimeMillis();
        for (TickThread tickThread : tickThreadPool.getAsyncTickRunnerList()) {
            if (tickThread.getLastTickMS() + 30000 > currentTime) continue;
            
            Logger log = VanillaSourceAPI.getInstance().getPlugin().getLogger();
            log.log(Level.SEVERE, "--- THREAD " + tickThread.getRunnerID() + " HAD NO RESPONSE FOR 30 SECONDS! ---");
            log.log(Level.SEVERE, "It will output a thread dump, but if you are still unable to identify the cause after reading it, please contact support.");
            log.log(Level.SEVERE, "");
            log.log(Level.SEVERE, "----- THREAD DUMP FOR " + tickThread.getRunnerID() + " -----");
            
            ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(tickThread.getCurrentThread().getId(), Integer.MAX_VALUE);
            
            log.log(Level.SEVERE, "Thread: " + threadInfo.getThreadName());
            log.log(Level.SEVERE, "PID: " + threadInfo.getThreadId() + " | Suspended: " + threadInfo.isSuspended()
                    + " | Native: " + threadInfo.isInNative() + " | State: " + threadInfo.getThreadState());
            if (threadInfo.getLockedMonitors().length != 0) {
                for (MonitorInfo monitor : threadInfo.getLockedMonitors()) {
                    log.log(Level.SEVERE, "Locked on:" + monitor.getLockedStackFrame());
                }
            }
            log.log(Level.SEVERE, "Stack: ");
            
            for (StackTraceElement stack : threadInfo.getStackTrace()) {
                log.log(Level.SEVERE, "\t" + stack);
            }
            
            log.log(Level.SEVERE, "------------------------------");
        }
        
    }
    
    public void cancel() {isStopped = true;}
}
