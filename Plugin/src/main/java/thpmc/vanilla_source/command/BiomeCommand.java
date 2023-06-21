package thpmc.vanilla_source.command;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import thpmc.vanilla_source.biome.BiomeManager;
import thpmc.vanilla_source.biome.gui.BiomeGUI;
import thpmc.vanilla_source.lang.SystemLanguage;

public class BiomeCommand {
    public static void register() {

        new CommandAPICommand("biome").withSubcommands(
                        new CommandAPICommand("menu")
                                .executesPlayer((sender, args) -> {
                                    BiomeGUI.openBiomeSelectGUI(sender, "", biomeSource -> {});
                                }),

                        new CommandAPICommand("set")
                                .executesPlayer((sender, args) -> {
                                    if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
                                        sender.sendMessage(SystemLanguage.getText("world-edit-is-required"));
                                        return;
                                    }
                                    BiomeManager.setBiome(sender);
                                })
                )
                .withPermission("vanilla_source.camera")
                .register();
    }
}
