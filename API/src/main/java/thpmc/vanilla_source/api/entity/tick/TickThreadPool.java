package thpmc.vanilla_source.api.entity.tick;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TickThreadPool {
    
    private final List<TickThread> asyncTickThreadList;
    
    private final TickThread utilityThread;
    
    public TickThreadPool(int poolSize){
        asyncTickThreadList = new ArrayList<>();
        for(int i = 0; i < poolSize; i++){
            asyncTickThreadList.add(new TickThread(i));
        }
        
        this.utilityThread = new TickThread(poolSize);
    }
    
    public void startAll(){
        asyncTickThreadList.forEach(TickThread::start);
    }
    
    public void cancelAll(){
        asyncTickThreadList.forEach(TickThread::cancel);
    }
    
    public List<TickThread> getAsyncTickRunnerList() {return asyncTickThreadList;}
    
    private final ReentrantLock LOCK = new ReentrantLock(true);
    
    private final AtomicInteger index = new AtomicInteger();
    
    public TickThread getNextTickThread() {
        return asyncTickThreadList.get(index.getAndAdd(1) % asyncTickThreadList.size());
    }
    
    public TickThread getTickThread(int threadNumber) {
        return asyncTickThreadList.get(threadNumber);
    }
    
    public TickThread getUtilityThread() {return utilityThread;}
    
    public int getPoolSize(){return asyncTickThreadList.size();}
    
}
