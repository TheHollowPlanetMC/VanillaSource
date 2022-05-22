package thpmc.vanilla_source.api.entity.tick;

import thpmc.vanilla_source.api.VanillaSourceAPI;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TickWatchDog implements Runnable{
    
    private final TickRunnerPool tickRunnerPool;
    
    private boolean isStopped = false;
    
    public TickWatchDog(TickRunnerPool tickRunnerPool){
        this.tickRunnerPool = tickRunnerPool;
    }
    
    @Override
    public void run() {
        if(isStopped) return;
        
        long currentTime = System.currentTimeMillis();
        for (TickRunner tickRunner : tickRunnerPool.getAsyncTickRunnerList()) {
            if (tickRunner.getLastTickMS() + 30000 > currentTime) continue;
            
            Logger log = VanillaSourceAPI.getInstance().getPlugin().getLogger();
            log.log(Level.SEVERE, "--- THREAD " + tickRunner.getRunnerID() + " HAD NO RESPONSE FOR 30 SECONDS! ---");
            log.log(Level.SEVERE, "It will output a thread dump, but if you are still unable to identify the cause after reading it, please contact support.");
            log.log(Level.SEVERE, "");
            log.log(Level.SEVERE, "----- THREAD DUMP FOR " + tickRunner.getRunnerID() + " -----");
            
            ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(tickRunner.getCurrentThread().getId(), Integer.MAX_VALUE);
            
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
