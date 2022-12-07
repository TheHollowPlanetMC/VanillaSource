package thpmc.vanilla_source.command;

import net.propromp.neocommander.api.annotation.Command;
import net.propromp.neocommander.api.annotation.Sender;
import net.propromp.neocommander.api.argument.annotation.IntegerArgument;
import net.propromp.neocommander.api.argument.annotation.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.camera.CameraHandler;
import thpmc.vanilla_source.api.camera.CameraPositionAt;
import thpmc.vanilla_source.api.camera.CameraPositions;
import thpmc.vanilla_source.api.camera.CameraPositionsManager;
import thpmc.vanilla_source.api.contan.ContanUtil;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.player.EnginePlayer;
import thpmc.vanilla_source.camera.CameraEditor;
import thpmc.vanilla_source.lang.SystemLanguage;

@Command(name = "camera", permission = "vanilla_source.camera")
public class CameraCommand {
    
    @Command(name = "get-item", description = "Give a item for setting.")
    public void getItem(@Sender Player sender) {
        sender.getInventory().addItem(CameraEditor.setter);
        sender.sendMessage(SystemLanguage.getText("gave-curve-setting-item"));
    }
    
    @Command(name = "create", description = "Create curve.")
    public void create(@Sender Player sender, @StringArgument String name, @IntegerArgument int endTick) {
        CameraEditor.onEnd(sender, name, endTick);
    }
    
    @Command(name = "cancel", description = "Cancel create curve.")
    public void set(@Sender Player sender) {
        CameraEditor.remove(sender);
        sender.sendMessage("§bCanceled.");
    }
    
    @Command(name = "play", description = "Play camera.")
    public void play(@Sender Player sender, @StringArgument String cameraName) {
        CameraPositions cameraPositions = CameraPositionsManager.getCameraPositionsByName(cameraName);
        if (cameraPositions == null) {
            sender.sendMessage(SystemLanguage.getText("camera-not-found"));
            return;
        }
        
        EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer(sender);
        if (enginePlayer == null) {
            throw new IllegalStateException("EnginePlayer is null.");
        }
    
        TickThread tickThread = VanillaSourceAPI.getInstance().getTickThreadPool().getNextTickThread();
        
        CameraHandler cameraHandler = new CameraHandler(enginePlayer, tickThread, ContanUtil.getEmptyClassInstance());
        CameraPositionAt lookAt = new CameraPositionAt(sender.getLocation().toVector());
        cameraHandler.setCameraPositions(cameraPositions);
        cameraHandler.setLookAtPositions(lookAt);
        
        tickThread.addEntity(cameraHandler);
    
        Bukkit.getScheduler().runTaskLater(VanillaSource.getPlugin(), cameraHandler::end, cameraPositions.getEndTick());
    }
    
}
