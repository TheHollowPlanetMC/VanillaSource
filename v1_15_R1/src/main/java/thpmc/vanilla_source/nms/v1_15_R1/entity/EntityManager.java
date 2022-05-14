package thpmc.vanilla_source.nms.v1_15_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.nms.entity.NMSEntity;

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
