package thpmc.vanilla_source.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.plugin.Plugin;

public class CommandRegistry {
    
    public static void onLoad() {
        CommandAPI.onLoad(new CommandAPIConfig().verboseOutput(true));

        CameraCommand.register();
        ContanCommand.register();
    }

    public static void onEnable(Plugin plugin) {
        CommandAPI.onEnable(plugin);
    }
    
    public static void onDisable() {
        CommandAPI.onDisable();
    }
    
}
