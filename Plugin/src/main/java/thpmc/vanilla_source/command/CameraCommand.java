package thpmc.vanilla_source.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
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


public class CameraCommand {

    public static void register() {

        new CommandAPICommand("camera").withSubcommands(
                new CommandAPICommand("gui-item")
                        .executesPlayer((sender, args) -> {
                            sender.getInventory().addItem(CameraEditor.setter);
                            sender.sendMessage(SystemLanguage.getText("gave-curve-setting-item"));
                        }),

                new CommandAPICommand("create")
                        .withArguments(new StringArgument("name"), new IntegerArgument("endTick"))
                        .executesPlayer((sender, args) -> {
                            Object[] argObjects = args.args();
                            CameraEditor.onEnd(sender, (String) argObjects[0], (Integer) argObjects[1]);
                        }),


                new CommandAPICommand("cancel")
                        .executesPlayer((sender, args) -> {
                            CameraEditor.remove(sender);
                            sender.sendMessage("§bCanceled.");
                        }),


                new CommandAPICommand("play")
                        .withArguments(new StringArgument("cameraName").replaceSuggestions(ArgumentSuggestions.strings(CameraPositionsManager.getAllCameraPositionName())))
                        .executesPlayer((sender, args) -> {
                            CameraPositions cameraPositions = CameraPositionsManager.getCameraPositionsByName((String) args.args()[0]);
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
                        })
                )
                .withPermission("vanilla_source.camera")
                .register();
    }
    
}
