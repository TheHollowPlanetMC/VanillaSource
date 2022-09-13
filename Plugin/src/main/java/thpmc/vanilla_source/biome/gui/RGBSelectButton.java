package thpmc.vanilla_source.biome.gui;

import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.util.gui.TextInputButton;
import thpmc.vanilla_source.lang.SystemLanguage;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class RGBSelectButton extends ArtButton {
    
    private Consumer<Integer> onInput = null;
    
    public RGBSelectButton(ItemStack itemStack) {
        super(itemStack);
        
        listener((event, menu) -> {
        
        });
    }
    
    public RGBSelectButton onInput(Consumer<Integer> onInput) {
        this.onInput = onInput;
        return this;
    }
    
    
    
    public static void openRGBSelectGUI(Player player, Consumer<Integer> onInput) {
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
    
        ArtMenu artMenu = artist.createMenu(VanillaSource.getPlugin().getArtGUI(), SystemLanguage.getText("gui-select-color"));
    
        artMenu.asyncCreate(menu -> {
            ItemStack inputButtonItem = new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-input-rgb")).build();
            String title = SystemLanguage.getText("gui-select-color");
            String defaultText = "#FFFFFF";
            TextInputButton inputButton = new TextInputButton(inputButtonItem, title, defaultText);
            inputButton.onInput((p, text) -> {
                try {
                    int rgb = Integer.parseInt(text, 16);
                    onInput.accept(rgb);
                    return null;
                } catch (NumberFormatException e) {
                    return "Invalid color code!";
                }
            });
        });
    
        artMenu.open(player);
    }
    
}
