package thpmc.vanilla_source.camera;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.camera.Bezier3DPositions;
import thpmc.vanilla_source.api.camera.CameraPositionsManager;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.api.util.math.BezierCurve3D;
import thpmc.vanilla_source.api.util.math.EasingBezier2D;
import thpmc.vanilla_source.gui.OKCancelGUI;
import thpmc.vanilla_source.lang.SystemLanguage;

import java.util.*;

public class CameraEditor {
    
    public final static ItemStack setter;
    
    static {
        setter = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta meta = setter.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName("§6Click to set camera curve position.");
        setter.setItemMeta(meta);
    }
    
    
    public static final Map<Player, BezierCurve3D> playerSettingPositionMap = new HashMap<>();
    public static final Map<Player, CameraSettingRunnable> playerSettingRunnableMap = new HashMap<>();
    
    public static void onSet(Player player) {
        BezierCurve3D endCurve = playerSettingPositionMap.get(player);
        if (endCurve == null) {
            Location loc = player.getLocation();
            endCurve = new BezierCurve3D(loc.toVector(), loc.toVector().add(new Vector(0.1, 0.1, 0.1)));
            playerSettingPositionMap.put(player, endCurve);
            CameraSettingRunnable task = new CameraSettingRunnable(player);
            task.runTaskTimer(VanillaSource.getPlugin(), 0, 2);
            playerSettingRunnableMap.put(player, task);
        } else {
            playerSettingPositionMap.put(player, endCurve.createNextBezierCurve(player.getLocation().toVector()));
        }
    }
    
    public static void onEnd(Player player, String name, int endTick) {
        BezierCurve3D endCurve = playerSettingPositionMap.get(player);
        if (endCurve == null) {
            player.sendMessage(SystemLanguage.getText("curve-setting-not-exist"));
            return;
        }
    
        List<BezierCurve3D> bezierCurve3DList = new ArrayList<>();
        BezierCurve3D current = endCurve.getPrevious();
        if (current == null) {
            player.sendMessage(SystemLanguage.getText("curve-setting-not-exist"));
            return;
        }
        
        while (true) {
            bezierCurve3DList.add(current);
        
            BezierCurve3D previous = current.getPrevious();
            if (previous == null) {
                break;
            }
            current = previous;
        }
        Collections.reverse(bezierCurve3DList);
    
        EasingBezier2D easingBezier2D = new EasingBezier2D(0.3, 0, 0.3, 1);
        Bezier3DPositions positions = new Bezier3DPositions(bezierCurve3DList, easingBezier2D, endTick);
    
        EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer(player);
    
        if (enginePlayer == null) {
            return;
        }
        
        if (CameraPositionsManager.getCameraPositionsByName(name) != null) {
            new OKCancelGUI(SystemLanguage.getText("ok-cancel-override-setting"))
                    .okText(SystemLanguage.getText("ok-cancel-override-setting-ok"))
                    .okLore(SystemLanguage.getText("ok-cancel-override-setting-ok-lore"))
                    .cancelText(SystemLanguage.getText("ok-cancel-override-setting-cancel"))
                    .cancelLore(SystemLanguage.getText("ok-cancel-override-setting-cancel-lore"))
                    .onOK(() -> {
                        CameraPositionsManager.registerCameraPositions(name, positions);
    
                        playerSettingPositionMap.remove(player);
                        playerSettingRunnableMap.remove(player);
    
                        player.sendMessage(SystemLanguage.getText("curve-setting-success"));
                    }).open(player);
            return;
        }
    
        CameraPositionsManager.registerCameraPositions(name, positions);
        
        playerSettingPositionMap.remove(player);
        playerSettingRunnableMap.remove(player);
        
        player.sendMessage(SystemLanguage.getText("curve-setting-success", name));
    }
    
    public static void remove(Player player) {
        playerSettingPositionMap.remove(player);
        playerSettingRunnableMap.remove(player);
    }
    
    
    static class CameraSettingRunnable extends BukkitRunnable {
        
        private final Player player;
        
        public CameraSettingRunnable(Player player) {
            this.player = player;
        }
    
        @Override
        public void run() {
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (!handItem.equals(setter)) {
                return;
            }
            
            BezierCurve3D endCurve = playerSettingPositionMap.get(player);
            if (endCurve == null) {
                cancel();
                return;
            }
    
            Location location = player.getLocation();
            
            endCurve.moveEndAnchorForExperiment(location.getX(), location.getY(), location.getZ());
    
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
    }
    
}
