package thpmc.vanilla_source.util;

@FunctionalInterface
public interface ThreadsafeIteration<V> {
    
    void accept(int x, int y, int z, V value);
    
}
