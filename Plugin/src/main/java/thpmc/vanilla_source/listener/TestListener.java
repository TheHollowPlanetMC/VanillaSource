package thpmc.vanilla_source.listener;

import be4rjp.artgui.ArtGUI;
import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.ArtFrame;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import be4rjp.artgui.menu.HistoryData;
import be4rjp.artgui.menu.MenuHistory;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.contan_lang.ContanModule;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.biome.BiomeDataContainer;
import thpmc.vanilla_source.api.camera.Bezier3DPositions;
import thpmc.vanilla_source.api.camera.CameraHandler;
import thpmc.vanilla_source.api.camera.CameraPositionAt;
import thpmc.vanilla_source.api.camera.CameraPositionsManager;
import thpmc.vanilla_source.api.contan.ContanUtil;
import thpmc.vanilla_source.api.entity.EngineEntity;
import thpmc.vanilla_source.api.entity.ai.pathfinding.AsyncAStarMachine;
import thpmc.vanilla_source.api.entity.ai.pathfinding.BlockPosition;
import thpmc.vanilla_source.api.entity.controller.EntityController;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.math.BezierCurve3D;
import thpmc.vanilla_source.api.util.math.EasingBezier2D;
import thpmc.vanilla_source.api.world.cache.EngineWorld;
import thpmc.vanilla_source.util.TaskHandler;
import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.nms.entity.NMSEntityController;
import thpmc.vanilla_source.api.util.collision.CollideOption;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class TestListener implements Listener {
    
    private int count = 0;
    
    private BezierCurve3D endCurve = null;
    
    private boolean set = false;
    
    //@EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(!event.getMessage().equals("spawn")) return;
        
        TaskHandler.runSync(() -> {
            Location location = player.getLocation();
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
    
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
            NMSEntityController entityPlayer = nmsHandler.createNMSEntityController(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
            entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());

            CollideOption collideOption = new CollideOption(FluidCollisionMode.NEVER, true);
            /*
            collideOption.setCollideBlockFunction(engineBlock -> {
                return engineBlock.getMaterial() != Material.GLASS;
            });*/
    
            /*
            VanillaSourceAPI.getInstance().getTickRunnerPool().spawn(tickRunner -> {
                EnginePlayerEntity npc = new EnginePlayerEntity(tickRunner.getThreadLocalCache().getWorld(location.getWorld().getName()), (NMSEntityPlayer) entityPlayer, tickRunner, true);
                npc.getGoalSelector().registerGoal(0, new EntityFollowGoal(player));
                npc.setMovementCollideOption(collideOption);
                tickRunner.addEntity(npc);
            });*/
        });
    }
    
    //@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
    
        ItemStack itemStack = event.getItem();
        if (itemStack == null) {
            return;
        }
    
        if(itemStack.getType() == Material.LAPIS_LAZULI){
            Location loc = player.getLocation();
        
            if(endCurve == null){
                endCurve = new BezierCurve3D(loc.toVector(), loc.toVector().add(new Vector(0.1, 0.1, 0.1)));
            
                new BukkitRunnable(){
                    @Override
                    public void run() {
                    
                        if(player.getInventory().getItemInMainHand().getType() != Material.LAPIS_LAZULI) return;
                    
                        Location location = player.getLocation();
                        if (!set){
                            endCurve.moveEndAnchorForExperiment(location.getX(), location.getY(), location.getZ());
                        }
                    
                        BezierCurve3D current = endCurve;
                        while (true){
                        
                            for(double t = 0.0; t < 1.0; t += 0.025){
                                Vector pos = current.getPosition(t);
                            
                                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
                                player.spawnParticle(Particle.REDSTONE, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0, 0, dustOptions);
                            }
                        
                            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.BLUE, 1);
                            Vector start = current.getStartAnchor();
                            Vector end = current.getEndAnchor();
                            Vector startC = current.getStartControl();
                            Vector endC = current.getEndControl();
                            player.spawnParticle(Particle.REDSTONE, start.getX(), start.getY(), start.getZ(), 0, 0, 0, 0, dustOptions);
                            player.spawnParticle(Particle.REDSTONE, end.getX(), end.getY(), end.getZ(), 0, 0, 0, 0, dustOptions);
                            player.spawnParticle(Particle.REDSTONE, startC.getX(), startC.getY(), startC.getZ(), 0, 0, 0, 0, dustOptions);
                            player.spawnParticle(Particle.REDSTONE, endC.getX(), endC.getY(), endC.getZ(), 0, 0, 0, 0, dustOptions);
                        
                            BezierCurve3D previous = current.getPrevious();
                            if(previous == null){
                                break;
                            }
                        
                            current = previous;
                        
                        }
                    
                    
                    }
                }.runTaskTimerAsynchronously(VanillaSource.getPlugin(), 0, 1);
            }else{
                endCurve = endCurve.createNextBezierCurve(player.getLocation().toVector());
            }
        }
    
        if(itemStack.getType() == Material.PAPER){
            if (endCurve == null) {
                return;
            }
            
            set = true;
    
            List<BezierCurve3D> bezierCurve3DList = new ArrayList<>();
            BezierCurve3D current = endCurve;
            while (true) {
                bezierCurve3DList.add(current);
                
                BezierCurve3D previous = current.getPrevious();
                if (previous == null) {
                    break;
                }
                current = previous;
            }
            Collections.reverse(bezierCurve3DList);
    
            EasingBezier2D easingBezier2D = new EasingBezier2D(0.3, 0, 0, 0.3);
            Bezier3DPositions positions = new Bezier3DPositions(bezierCurve3DList, easingBezier2D, 400);
    
            EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer(player);
            
            if (enginePlayer == null) {
                return;
            }
    
            CameraPositionsManager.registerCameraPositions("test", positions);
            
            /*
            TickThread tickThread = (TickThread) VanillaSourceAPI.getInstance().getContanEngine().getNextAsyncThread();
            CameraHandler cameraHandler = new CameraHandler(enginePlayer, tickThread, ContanUtil.getEmptyClassInstance());
            cameraHandler.setCameraPositions(positions);
            
            Vector lookAtPosition = bezierCurve3DList.get(0).getPosition(0).midpoint(bezierCurve3DList.get(bezierCurve3DList.size() - 1).getPosition(0));
            cameraHandler.setLookAtPositions(new CameraPositionAt(lookAtPosition.getX(), lookAtPosition.getY(), lookAtPosition.getZ()));
            
            tickThread.addEntity(cameraHandler);*/
        }
    }
    
    
    private final Set<String> names = new HashSet<>();
    
    Color color = Color.BLUE;
    boolean flag = false;
    
    @EventHandler
    public void onPlayerClick(PlayerAnimationEvent event){
        Player player = event.getPlayer();
        if(!player.isSneaking()) return;
    
        VanillaSourceAPI api = VanillaSourceAPI.getInstance();
        INMSHandler nmsHandler = api.getNMSHandler();
    
        BiomeDataContainer container = new BiomeDataContainer();
        nmsHandler.setDefaultBiomeData(container);
        container.fogColorRGB = color.asRGB();
        container.skyColorRGB = color.asRGB();
        color = Color.GREEN;
        container.particle = Particle.valueOf("ASH");
        
        if (!flag) {
            nmsHandler.createBiome("test", container);
            flag = true;
            player.sendMessage("1");
        } else {
            nmsHandler.setBiomeSettings("test", container);
            player.sendMessage("2");
        }
        nmsHandler.setBiomeForBlock(player.getLocation().getBlock(), "test");
        
        if (true) {
            return;
        }
        
        //Artistクラスのインスタンスを作成
        //GUIの大きさと全てのページに配置するボタンを定義する
        //ここで定義したボタンは全てのページで表示されます
        Artist artist = new Artist(() -> {
        
            //nullを指定すると空白になりアイテムを配置したりできるようになる
            ArtButton V = null;
            //ボタンを作成
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
        
            //ページ移動用ボタンを作成
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name("&r次のページ &7[{NextPage}/{MaxPage}]").build());
        
            //ページ移動用ボタンを作成
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name("&r前のページ &7[{PreviousPage}/{MaxPage}]").build());
            //戻るボタンを作成
            //もしこのGUIを開く前に別のGUIを開いていた場合はそのGUIに戻ります
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name("&r{PreviousName}&7に戻る").build());
            
            ArtButton C = new ArtButton(new ItemBuilder(Material.COMMAND_BLOCK).name("&n作成").build()).listener((e, menu) -> {
                HistoryData historyData = HistoryData.getHistoryData(VanillaSource.getPlugin().getArtGUI(), player);
                historyData.clearOnClose = false;
                
                new AnvilGUI.Builder()
                        .onClose(p -> {                                               //called when the inventory is closing
                            player.sendMessage("You closed the inventory.");
                        })
                        .onComplete((p, text) -> {                                    //called when the inventory output slot is clicked
                            names.add(text);
                            Bukkit.getScheduler().runTaskLater(VanillaSource.getPlugin(), () -> {
                                historyData.clearOnClose = true;
                                MenuHistory history = historyData.getCurrentMenu();
                                if (history == null){
                                    player.sendMessage("NULL!!");
                                    return;
                                }
                                history.getArtMenu().open(player);
                                player.sendMessage("OPEN!");
                            }, 1);
                            return AnvilGUI.Response.close();
                        })
                        .preventClose()                                                    //prevents the inventory from being closed
                        .text("")                              //sets the text the GUI should start with
                        .itemLeft(new ItemStack(Material.PAPER))                      //use a custom item for the first slot//use a custom item for the second slot
                        .onLeftInputClick(p -> player.sendMessage("first sword"))     //called when the left input slot is clicked
                        .title("Enter your answer.")                                       //set the title of the GUI (only works in 1.14+)
                        .plugin(VanillaSource.getPlugin())                                          //set the plugin instance
                        .open(player);
            });
        
            //現在のページを表示するボタンを作成
            //ReplaceableButtonを継承したボタンの名前は特定の文字列が置き換わるようになります
            //詳細はReplaceNameManagerを参照
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name("&7現在のページ&r[{CurrentPage}/{MaxPage}]").build());
        
            //配列として視覚的に表記
            //配列の長さは必ず9の倍数である必要があります
            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, G,
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, I,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, C,
                    V, V, V, V, V, V, V, G, B,
            };
        });
    
        //GUIを作成
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), "&nテストGUI&r [{CurrentPage}/{MaxPage}]");
    
        //非同期でアイテムを配置
        //GUIを開くたびに実行されます
        artMenu.asyncCreate(menu -> {
            for (String name : names) {
                menu.addButton(new ArtButton(new ItemBuilder(Material.MUSIC_DISC_11).name("&r&n" + name).build()));
            }
        });
    
        //GUIを開く
        artMenu.open(player);
        
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
        
        /*
        Location location = player.getLocation();
        INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
        
        TickThread tickThread = VanillaSourceAPI.getInstance().getMainThread();
        ContanModule contanModule = VanillaSourceAPI.getInstance().getContanEngine().getModule("engine/event/EventHandler.cntn");
        if (contanModule != null) {
            try {
                contanModule.invokeFunction(tickThread, "firePlayerClick", event);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        
    
        /*
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");
        NMSEntityController entityPlayer = nmsHandler.createNMSEntityController(location.getWorld(), location.getX(), location.getY(), location.getZ(), EntityType.PLAYER, gameProfile);
        entityPlayer.setPositionRaw(location.getX(), location.getY(), location.getZ());
    
    
        TickThread tickThread = VanillaSourceAPI.getInstance().getTickThreadPool().getNextTickThread();
        
        EngineEntity npc = new EngineEntity(tickThread.getThreadLocalCache().getWorld(location.getWorld().getName()), entityPlayer, tickThread);
        npc.getAIController().goalSelector.registerGoal(0, new EntityFollowGoal(player));
        npc.setAutoClimbHeight(1.0F);
        tickThread.addEntity(npc);*/
    
        /*
        World world = player.getWorld();
        BlockPosition start = new BlockPosition(178, 80, -27);
        BlockPosition goal = new BlockPosition(178, 80, -27);
        
        EngineWorld engineWorld = VanillaSourceAPI.getInstance().getMainThread().getThreadLocalCache().getParallelWorld(VanillaSourceAPI.getInstance().getDefaultUniverse(), "world");
        
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
