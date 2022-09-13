package thpmc.vanilla_source.biome.gui;

import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.biome.BiomeSource;
import thpmc.vanilla_source.api.biome.BiomeStore;
import thpmc.vanilla_source.api.biome.CustomBiome;
import thpmc.vanilla_source.lang.SystemLanguage;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class BiomeGUI {

    public static void openBiomeSelectGUI(Player player, String title, Consumer<BiomeSource> onSelect) {
        Artist artist = new Artist(() -> {
            
            //ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
            
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
            
            //PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
            
            //PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
            
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
            
            //ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
            
            ArtButton M = new ArtButton(new ItemBuilder(Material.GRASS_BLOCK).name(SystemLanguage.getText("gui-biome-minecraft")).build());
            M.listener((event, menu) -> openMinecraftBiomeSelectGUI(player, title, onSelect));
            
            ArtButton C = new ArtButton(new ItemBuilder(Material.COMMAND_BLOCK).name(SystemLanguage.getText("gui-biome-custom")).build());
            C.listener((event, menu) -> openCustomBiomeSelectGUI(player, title, onSelect));
            
            return new ArtButton[]{
                    G, G, G, G, G, G, G, G, G,
                    G, G, M, G, G, G, C, G, B,
                    G, G, G, G, G, G, G, G, E,
            };
        });
        
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
        
        artMenu.asyncCreate(menu -> {});
        
        artMenu.open(player);
    }
    
    
    public static void openMinecraftBiomeSelectGUI(Player player, String title, Consumer<BiomeSource> onSelect) {
        Artist artist = new Artist(() -> {
        
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
        
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
        
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
        
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
        
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
        
            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, I,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, B,
                    V, V, V, V, V, V, V, G, E,
            };
        });
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
    
        artMenu.asyncCreate(menu -> {
            for (BiomeSource biomeSource : BiomeStore.getAllVanillaBiomes()) {
                ArtButton biomeButton = new ArtButton(new ItemBuilder(Material.GRASS_BLOCK).name(biomeSource.getKey()).build());
                biomeButton.listener((e, m) -> {
                    onSelect.accept(biomeSource);
                    player.closeInventory();
                });
                menu.addButton(biomeButton);
            }
        });
    
        artMenu.open(player);
    }
    
    
    public static void openCustomBiomeSelectGUI(Player player, String title, Consumer<BiomeSource> onSelect) {
        Artist artist = new Artist(() -> {
        
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
        
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
        
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
        
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
        
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
        
            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, I,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, B,
                    V, V, V, V, V, V, V, G, E,
            };
        });
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
    
        artMenu.asyncCreate(menu -> {
            for (CustomBiome customBiome : BiomeStore.getAllCustomBiomes()) {
                ArtButton biomeButton = new ArtButton(new ItemBuilder(Material.GRASS_BLOCK)
                        .name(customBiome.getKey()).lore(SystemLanguage.getText("gui-right-click-to-edit")).build());
                biomeButton.listener((e, m) -> {
                    if (e.getClick() == ClickType.LEFT) {
                        onSelect.accept(customBiome);
                        player.closeInventory();
                    } else if (e.getClick() == ClickType.RIGHT) {

                    }
                });
                menu.addButton(biomeButton);
            }
        });
    
        artMenu.open(player);
    }
    
    
    public static void openCustomBiomeEditor(Player player, CustomBiome customBiome) {
        Artist artist = new Artist(() -> {
            ArtButton V = null;
            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton E = new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-exit")).build());
            E.listener((event, menu) -> player.closeInventory());
        
            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-next")).build());
        
            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name(SystemLanguage.getText("gui-page-previous")).build());
        
            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name(SystemLanguage.getText("gui-back")).build());
        
            ReplaceableButton I = new ReplaceableButton(new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-page-current")).build());
        
            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, I,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, B,
                    V, V, V, V, V, V, V, G, E,
            };
        });
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), SystemLanguage.getText("gui-edit-custom-biome", customBiome.getKey()));
    
        artMenu.asyncCreate(menu -> {});
    
        artMenu.open(player);
    }

}
