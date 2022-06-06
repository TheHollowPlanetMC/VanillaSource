package thpmc.vanilla_source.api.util;

import it.unimi.dsi.fastutil.shorts.Short2ByteArrayMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SectionLevelArray {

    //Copy on write
    private Short2ByteArrayMap arrayMap = new Short2ByteArrayMap();
    
    private final ReentrantLock LOCK = new ReentrantLock(true);
    
    
    public int getSize(){
        return arrayMap.size();
    }
    
    public void setLevel(int sectionX, int sectionY, int sectionZ, byte level){
        short serialIndex = getSerialIndex(sectionX, sectionY, sectionZ);

        try {
            LOCK.lock();

            Short2ByteArrayMap copy = arrayMap.clone();
            copy.put(serialIndex, level);
            arrayMap = copy;
        } finally {
            LOCK.unlock();
        }
    }
    
    public byte getLevel(int sectionX, int sectionY, int sectionZ){
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

            Short2ByteArrayMap copy = arrayMap.clone();
            copy.remove(serialIndex);
            arrayMap = copy;
        } finally {
            LOCK.unlock();
        }
    }
    
    public boolean threadsafeIteration(ThreadsafeIteration<Byte> iteration) {
        
        boolean notEmpty;

        for (Map.Entry<Short, Byte> entry : arrayMap.entrySet()) {
            short serialIndex = entry.getKey();
            byte level = entry.getValue();

            int x = serialIndex & 0xF;
            int y = (serialIndex >> 8) & 0xF;
            int z = (serialIndex >> 4) & 0xF;

            iteration.accept(x, y, z, level);
        }
        notEmpty = arrayMap.size() != 0;
        
        return notEmpty;
    }
    
    private short getSerialIndex(int x, int y, int z){return (short) (y << 8 | z << 4 | x);}
    
}

