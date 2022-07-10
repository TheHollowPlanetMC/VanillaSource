package thpmc.vanilla_source.nms.v1_15_R1.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.nms.entity.NMSItemEntityController;

public class EntityManager {
    
    public static <T> NMSEntityController createNMSEntityController(World world, double x, double y, double z, EntityType type, @Nullable T data) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
    
        switch (type){
            case PLAYER: {
                return new ImplEntityControllerPlayer(MinecraftServer.getServer(), worldServer, (GameProfile) data, new PlayerInteractManager(worldServer));
            }
            
            case ARMOR_STAND: {
                return new ImplEntityControllerArmorStand(worldServer, x, y, z);
            }
            
            case DROPPED_ITEM: {
                net.minecraft.server.v1_15_R1.ItemStack itemStack = CraftItemStack.asNMSCopy((ItemStack) data);
                return new ImplEntityControllerItem(worldServer, x, y, z, itemStack);
            }
        }
    
        throw new IllegalArgumentException("Entity type " + type + " is not supported.");
    }
    
}
