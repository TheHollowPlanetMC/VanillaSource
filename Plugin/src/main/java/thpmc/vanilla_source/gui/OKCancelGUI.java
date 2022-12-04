package thpmc.vanilla_source.gui;

import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import thpmc.vanilla_source.VanillaSource;

public class OKCancelGUI {
    
    private String title = "";
    private String okText = "";
    private String cancelText = "";
    private String[] okLore = new String[0];
    private String[] cancelLore = new String[0];
    private Runnable okRunnable = () -> {};
    private Runnable cancelRunnable = () -> {};
    private Runnable closeRunnable = () -> {};
    
    public OKCancelGUI(String title) {
        this.title = title;
    }
    
    public void open(Player player) {
        Artist artist = new Artist(() -> {
            ArtButton G = new ArtButton(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name("&a").build());
        
            ArtButton O = new ArtButton(new ItemBuilder(Material.EMERALD_BLOCK).name(okText).lore(okLore).build())
                    .listener((event, menu) -> {
                        okRunnable.run();
                    });
    
            ArtButton C = new ArtButton(new ItemBuilder(Material.EMERALD_BLOCK).name(cancelText).lore(cancelLore).build())
                    .listener((event, menu) -> {
                        cancelRunnable.run();
                    });
        
            return new ArtButton[]{
                    G, G, G, G, G, G, G, G, G,
                    G, G, G, G, G, G, G, G, G,
                    G, G, O, G, G, G, C, G, G,
                    G, G, G, G, G, G, G, G, G,
                    G, G, G, G, G, G, G, G, G,
            };
        });
        
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), title);
        artMenu.asyncCreate(menu -> {});
        artMenu.onClose((event, menu) -> closeRunnable.run());
        
        artMenu.open(player);
    }
    
    public OKCancelGUI okText(String okText) {
        this.okText = okText;
        return this;
    }
    
    public OKCancelGUI okLore(String... okLore) {
        this.okLore = okLore;
        return this;
    }
    
    public OKCancelGUI cancelText(String cancelText) {
        this.cancelText = cancelText;
        return this;
    }
    
    public OKCancelGUI cancelLore(String... cancelLore) {
        this.cancelLore = cancelLore;
        return this;
    }
    
    public OKCancelGUI onOK(Runnable okRunnable) {
        this.okRunnable = okRunnable;
        return this;
    }
    
    public OKCancelGUI onCancel(Runnable cancelRunnable) {
        this.cancelRunnable = cancelRunnable;
        return this;
    }
    
    public OKCancelGUI onClose(Runnable closeRunnable) {
        this.closeRunnable = closeRunnable;
        return this;
    }
    
}
