package thpmc.vanilla_source.api.util;

import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SectionTypeArray {

    //Copy on write
    private Short2ObjectArrayMap<Object> arrayMap = new Short2ObjectArrayMap<>();

    private final ReentrantLock LOCK = new ReentrantLock(true);
    
    public ReentrantLock getLOCK() {return LOCK;}
    
    public int getSize(){
        return arrayMap.size();
    }

    public void setType(int sectionX, int sectionY, int sectionZ, @Nullable Object iBlockData){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        try {
            LOCK.lock();
            Short2ObjectArrayMap<Object> copy = arrayMap.clone();

            if(iBlockData != null){
                copy.put(serialIndex, iBlockData);
            }else{
                copy.remove(serialIndex);
            }

            arrayMap = copy;
        }finally {
            LOCK.unlock();
        }
    }

    public @Nullable Object getType(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        return arrayMap.get(serialIndex);
    }
    
    public boolean contains(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);
        return arrayMap.containsKey(serialIndex);
    }
    
    public void remove(int sectionX, int sectionY, int sectionZ){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);

        try {
            LOCK.lock();

            Short2ObjectArrayMap<Object> copy = arrayMap.clone();
            copy.remove(serialIndex);
            arrayMap = copy;
        } finally {
            LOCK.unlock();
        }
    }
    
    public boolean threadsafeIteration(ThreadsafeIteration<Object> iteration) {
        
        boolean notEmpty;

        for (Map.Entry<Short, Object> entry : arrayMap.entrySet()) {
            short serialIndex = entry.getKey();
            Object iBlockData = entry.getValue();

            int x = serialIndex & 0xF;
            int y = (serialIndex >> 8) & 0xF;
            int z = (serialIndex >> 4) & 0xF;

            iteration.accept(x, y, z, iBlockData);
        }
        notEmpty = arrayMap.size() != 0;
        
        return notEmpty;
    }

    private short getSerialIndex(int x, int y, int z){return (short) (y << 8 | z << 4 | x);}

}
