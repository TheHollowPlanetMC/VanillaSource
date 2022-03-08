package thpmc.engine.listener;

import be4rjp.parallel.ParallelUniverse;
import be4rjp.parallel.chiyogami.ChiyogamiManager;
import thpmc.engine.api.player.EnginePlayer;
import thpmc.engine.impl.ImplEnginePlayer;
import be4rjp.parallel.nms.NMSManager;
import be4rjp.parallel.nms.PacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import thpmc.engine.THPEngine;
import thpmc.engine.api.THPEngineAPI;


public class PlayerJoinQuitListener implements Listener {
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        //Inject packet handler
        Player player = event.getPlayer();
        EnginePlayer enginePlayer = ImplEnginePlayer.onPlayerJoin(player);

        ParallelUniverse universe = THPEngineAPI.getInstance().createUniverse(player.getUniqueId().toString());
        universe.addPlayer(enginePlayer);
        
        Object wrappedParallelPlayer = ChiyogamiManager.getWrappedParallelPlayer(player);
        if(wrappedParallelPlayer != null) ChiyogamiManager.setCheckFunction(enginePlayer, wrappedParallelPlayer);
        
        PacketHandler packetHandler = new PacketHandler(enginePlayer);
        
        try {
            ChannelPipeline pipeline = NMSManager.getNMSHandler().getChannel(player).pipeline();
            pipeline.addBefore("packet_handler", THPEngine.getPlugin().getName() + "PacketInjector:" + player.getName(), packetHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*-----------------------TEST CODE--------------------------
    @EventHandler
    public void onClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
        if(!player.isSneaking()) return;
        
        ParallelAPI api = ParallelAPI.getInstance();
        ParallelPlayer parallelPlayer = api.getParallelPlayer(player);
        if(parallelPlayer == null) return;
        
        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null) return;
        
        for(ParallelUniverse otherUniverse : api.getAllUniverse()){
            if(otherUniverse != universe) universe.addDiffs(otherUniverse);
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        ParallelPlayer parallelPlayer = ParallelPlayer.getParallelPlayer(player);
        if(parallelPlayer == null) return;

        ParallelUniverse universe = parallelPlayer.getUniverse();
        if(universe == null){
            player.sendMessage("NULL!");
            return;
        }

        ParallelWorld parallelWorld = universe.getWorld(player.getWorld().getName());

        Block block = event.getBlock();
        parallelWorld.setType(block.getX(), block.getY(), block.getZ(), Material.AIR);
        parallelWorld.sendBlockUpdate(block.getX(), block.getY(), block.getZ());

        if(parallelWorld.getType(block.getX(), block.getY(), block.getZ()) != Material.AIR){
            player.sendMessage("NOT EQUAL!");
        }

        event.setCancelled(true);
    }*/
    
    
    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        ImplEnginePlayer.onPlayerQuit(player);
        
        ChiyogamiManager.removeWrappedParallelPlayer(player);

        try {
            Channel channel = NMSManager.getNMSHandler().getChannel(player);
            
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(THPEngine.getPlugin().getName() + "PacketInjector:" + player.getName());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
