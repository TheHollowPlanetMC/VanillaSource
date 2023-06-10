package thpmc.vanilla_source.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPIConfig;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandRegistry {
    
    public static void onLoad(JavaPlugin plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).verboseOutput(true));

        CameraCommand.register();
        ContanCommand.register();
    }

    public static void onEnable() {
        CommandAPI.onEnable();
    }
    
    public static void onDisable() {
        CommandAPI.onDisable();
    }
    
}
