package thpmc.vanilla_source.api.nms.entity;

import org.bukkit.entity.Entity;
import thpmc.vanilla_source.api.entity.controller.EntityController;

/**
 * NMS entity controller.
 * It is used as an interface for NMS entities.
 * Implementing classes always extend NMS entities.
 */
public interface NMSEntityController extends EntityController {
    
    /**
     * Get bukkit entity instance.
     * Note that most of Bukkit's methods are not thread-safe.
     * @return Bukkit entity
     */
    Entity getBukkitEntity();
    
}
