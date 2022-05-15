package thpmc.vanilla_source.api.util;

import org.bukkit.Bukkit;
import thpmc.vanilla_source.api.VanillaSourceAPI;

public class ThreadUtil {
    
    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
    
    public static void runAtMainThread(Runnable runnable) {
        Bukkit.getScheduler().runTask(VanillaSourceAPI.getInstance().getPlugin(), runnable);
    }
    
    public static void runAtMainThreadLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(VanillaSourceAPI.getInstance().getPlugin(), runnable, delay);
    }
    
}
