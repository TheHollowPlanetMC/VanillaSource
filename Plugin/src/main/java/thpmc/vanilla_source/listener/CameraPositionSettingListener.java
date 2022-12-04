package thpmc.vanilla_source.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import thpmc.vanilla_source.camera.CameraEditor;

public class CameraPositionSettingListener implements Listener {
    
    @EventHandler
    public void onClickTripwireHook(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (player.getInventory().getItemInMainHand().equals(CameraEditor.setter)) {
            CameraEditor.onSet(player);
        }
    }
    
}
