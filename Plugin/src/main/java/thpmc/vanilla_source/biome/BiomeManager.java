package thpmc.vanilla_source.biome;

import org.bukkit.entity.Player;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.biome.gui.BiomeGUI;
import thpmc.vanilla_source.lang.SystemLanguage;
import thpmc.vanilla_source.util.RegionBlocks;
import thpmc.vanilla_source.world_edit.WorldEditUtil;

public class BiomeManager {

    public static void setBiome(Player player) {
        RegionBlocks regionBlocks = WorldEditUtil.getSelectedRegion(player);
        if (regionBlocks == null) {
            return;
        }

        BiomeGUI.openBiomeSelectGUI(player, SystemLanguage.getText("select-biome"), biomeSource -> {
            Object nmsBiome = biomeSource.getNMSBiome();
            INMSHandler nmsHandler = VanillaSourceAPI.getInstance().getNMSHandler();
            regionBlocks.apply(block -> nmsHandler.setBiomeForBlock(block, nmsBiome));

            player.sendMessage(SystemLanguage.getText("biome-applied"));
            player.closeInventory();
        });
    }

}
