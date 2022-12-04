package thpmc.vanilla_source.command;

import net.propromp.neocommander.api.annotation.Command;
import net.propromp.neocommander.api.annotation.Sender;
import net.propromp.neocommander.api.argument.annotation.StringArgument;
import org.bukkit.entity.Player;
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
    public void create(@Sender Player sender, @StringArgument String name) {
        CameraEditor.onEnd(sender, name);
    }
    
    @Command(name = "cancel", description = "Cancel create curve.")
    public void set(@Sender Player sender) {
        CameraEditor.remove(sender);
    }
    
}
