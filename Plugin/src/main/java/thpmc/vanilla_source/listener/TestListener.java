package thpmc.vanilla_source.listener;

import org.bukkit.*;
import thpmc.vanilla_source.util.TaskHandler;
import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.entity.EnginePlayerEntity;
import thpmc.vanilla_source.api.entity.ai.navigation.goal.EntityFollowGoal;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.nms.entity.NMSEntity;
import thpmc.vanilla_source.api.nms.entity.NMSEntityPlayer;
import thpmc.vanilla_source.api.util.collision.CollideOption;

import java.util.UUID;

public class TestListener implements Listener {
    
    private int count = 0;
    
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(!event.getMessage().equals("spawn")) return;
        
        TaskHandler.runSync(() -> {
            Location location = player.getLocation();
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
    
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
            NMSEntity entityPlayer = nmsHandler.createNMSEntity(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
            entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());

            CollideOption collideOption = new CollideOption(FluidCollisionMode.NEVER, true);
            /*
            collideOption.setCollideBlockFunction(engineBlock -> {
                return engineBlock.getMaterial() != Material.GLASS;
            });*/
    
            VanillaSourceAPI.getInstance().getTickRunnerPool().spawn(tickRunner -> {
                EnginePlayerEntity npc = new EnginePlayerEntity(tickRunner.getThreadLocalCache().getWorld(location.getWorld().getName()), (NMSEntityPlayer) entityPlayer, tickRunner, true);
                npc.getGoalSelector().registerGoal(0, new EntityFollowGoal(player));
                npc.setMovementCollideOption(collideOption);
                tickRunner.addEntity(npc);
            });
        });
    }
    
    @EventHandler
    public void onPlayerClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
        if(!player.isSneaking()) return;
        
        /*
        Location loc = player.getLocation();
        NativeBridge.test2(player.getWorld().getName().toCharArray(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        player.sendMessage("click!");*/
    
        /*
        List<EngineBoundingBox> list = new ArrayList<>();
        EngineWorld world = AsyncWorldCache.getAsyncWorld(player.getWorld().getName());
        
        Location loc = player.getLocation();
        
        INMSHandler nmsHandler = THPEngineAPI.getInstance().getNMSHandler();
        
        for(int x = loc.getBlockX() - 10; x < loc.getBlockX() + 10; x++){
            for(int y = loc.getBlockY() - 10; y < loc.getBlockY() + 10; y++){
                for(int z = loc.getBlockZ() - 10; z < loc.getBlockZ() + 10; z++){
                    EngineChunk chunk = world.getChunkAt(x >> 4, z >> 4);
                    Object iBlockData = chunk.getNMSBlockData(x, y, z);
                    if(iBlockData == null) continue;
                    
                    EngineBlock block = new EngineBlock(world, chunk, x, y, z, iBlockData);
                    nmsHandler.collectBlockCollisions(block, list, new CollideOption(FluidCollisionMode.ALWAYS, true));
                }
            }
        }
        
        for(EngineBoundingBox boundingBox : list){
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMaxZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMinX(), boundingBox.getMaxY(), boundingBox.getMaxZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMinZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMinY(), boundingBox.getMaxZ(), 0);
            player.spawnParticle(Particle.FLAME, boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ(), 0);
        }*/
        
    
        /*
        TaskHandler.runAsync(() -> {
            EngineWorld world = AsyncWorldCache.getAsyncWorld(player.getWorld().getName());
            CollideOption collideOption = new CollideOption(FluidCollisionMode.ALWAYS, false);
            collideOption.setCollideBlockFunction(engineBlock -> {
                return engineBlock.getMaterial() != Material.OAK_LEAVES;
            });
            collideOption.setBoundingBoxGrow(0.2);
            EngineRayTraceResult rayTraceResult = world.rayTraceEntities(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(), 20, collideOption);
            if(rayTraceResult == null){
                player.sendMessage("NOT HIT!");
            }else{
                Vector hitPosition = rayTraceResult.getHitPosition();
                BlockFace hitFace = rayTraceResult.getHitFace();
        
                player.spawnParticle(Particle.FLAME, hitPosition.getX(), hitPosition.getY(), hitPosition.getZ(), 0);
                player.sendMessage("Hit face : " + hitFace);
            }
        });*/
        
        
        Location location = player.getLocation();
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        for (int index = 0; index < 100; index++) {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
            NMSEntity entityPlayer = nmsHandler.createNMSEntity(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
            entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());
    
            VanillaSourceAPI.getInstance().getTickRunnerPool().spawn(tickRunner -> {
                EnginePlayerEntity npc = new EnginePlayerEntity(tickRunner.getThreadLocalCache().getWorld(location.getWorld().getName()), (NMSEntityPlayer) entityPlayer, tickRunner, true);
                npc.getGoalSelector().registerGoal(0, new EntityFollowGoal(player));
                tickRunner.addEntity(npc);
            });
    
            count++;
            
            player.sendMessage("PlayerNPC -> " + count);
        }
    
        /*
        World world = player.getWorld();
        BlockPosition start = new BlockPosition(173, 80, -23);
        BlockPosition goal = new BlockPosition(172, 80, 4);
    
        ThreadLocalCache cache = new ThreadLocalCache();
        ThreadLocalEngineWorld engineWorld = cache.getWorld(world.getName());
        
        CollideOption collideOption = new CollideOption(FluidCollisionMode.NEVER, true);
        
        AsyncAStarMachine astar;
        long s;
        long e;
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 50, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 50,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 50,  Time = " + (e - s) + "ns");
    
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 100, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 100,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 100,  Time = " + (e - s) + "ns");
    
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 500, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 500,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 500,  Time = " + (e - s) + "ns");
    
        astar = new AsyncAStarMachine(engineWorld, start, goal, 1, 1, 5000, false, collideOption);
        s = System.nanoTime();
        astar.runPathFinding();
        e = System.nanoTime();
        System.out.println("[Java] iteration = 5000,  Time = " + (e - s) + "ns");
        s = System.nanoTime();
        astar.runPathfindingNative();
        e = System.nanoTime();
        System.out.println("[Rust] iteration = 5000,  Time = " + (e - s) + "ns");*/
    }
    
}
