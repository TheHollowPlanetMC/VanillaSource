package thpmc.vanilla_source.command;

import net.propromp.neocommander.api.CommandManager;
import net.propromp.neocommander.api.annotation.AnnotationManager;
import thpmc.vanilla_source.VanillaSource;

public class CommandRegistry {
    
    private static CommandManager commandManager;
    
    public static void register(VanillaSource plugin) {
        commandManager = new CommandManager(plugin);
        AnnotationManager annotationManager = commandManager.getAnnotationManager();
        annotationManager.register(new ContanCommand());
    }
    
    public static void unregister() {
        if (commandManager != null) {
            commandManager.clearCommands();
        }
    }
    
}
