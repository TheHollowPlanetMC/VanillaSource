package thpmc.vanilla_source.api.entity.tick;

import thpmc.vanilla_source.api.entity.EngineEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class TickRunnerPool {
    
    private final List<TickRunner> asyncTickRunnerList;
    
    public TickRunnerPool(int poolSize){
        asyncTickRunnerList = new ArrayList<>();
        for(int i = 0; i < poolSize; i++){
            asyncTickRunnerList.add(new TickRunner(i));
        }
    }
    
    public void startAll(){
        asyncTickRunnerList.forEach(TickRunner::start);
    }
    
    public void cancelAll(){
        asyncTickRunnerList.forEach(TickRunner::cancel);
    }
    
    public List<TickRunner> getAsyncTickRunnerList() {return asyncTickRunnerList;}
    
    private final ReentrantLock LOCK = new ReentrantLock(true);
    
    private int index = 0;
    
    public void addEntity(EngineEntity npc){
        try{
            LOCK.lock();
            asyncTickRunnerList.get(index % asyncTickRunnerList.size()).addEntity(npc);
            index++;
        }finally {
            LOCK.unlock();
        }
    }
    
    public void spawn(Consumer<TickRunner> spawnConsumer){
        try{
            LOCK.lock();
            spawnConsumer.accept(asyncTickRunnerList.get(index % asyncTickRunnerList.size()));
            index++;
        }finally {
            LOCK.unlock();
        }
    }
    
    public int getPoolSize(){return asyncTickRunnerList.size();}
    
}
