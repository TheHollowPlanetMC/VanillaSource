package thpmc.engine.nms.v1_16_R3.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import thpmc.engine.api.nms.entity.NMSEntity;

public class EntityManager {
    
    public static <T> NMSEntity createNMSEntity(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        
        switch (type){
            case PLAYER:{
                return new ImplEntityPlayer(MinecraftServer.getServer(), worldServer, (GameProfile) data, new PlayerInteractManager(worldServer));
            }
        }
        
        throw new IllegalArgumentException("Entity type " + type + " is not supported.");
    }
    
}
