package thpmc.vanilla_source.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import thpmc.vanilla_source.api.world.EngineLocation;

public class BukkitAdapter {
    
    public static Location toBukkitLocation(EngineLocation location) {
        return new Location(location.getWorld() == null ? null : Bukkit.getWorld(location.getWorld().getName()), location.getX(), location.getY(), location.getZ());
    }
    
}
