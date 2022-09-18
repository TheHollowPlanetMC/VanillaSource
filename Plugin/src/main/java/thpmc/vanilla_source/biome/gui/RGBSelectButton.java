package thpmc.vanilla_source.biome.gui;

import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import be4rjp.artgui.menu.HistoryData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import thpmc.vanilla_source.VanillaSource;
import thpmc.vanilla_source.api.util.gui.TextInputButton;
import thpmc.vanilla_source.lang.SystemLanguage;

import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public class RGBSelectButton extends ArtButton {
    
    public RGBSelectButton(ItemStack itemStack, boolean nullable, Consumer<Integer> onInput) {
        super(itemStack);
        
        listener((event, menu) -> {
            HumanEntity humanEntity = event.getWhoClicked();
            if (!(humanEntity instanceof Player)) {
                return;
            }
            
            openRGBSelectGUI((Player) humanEntity, nullable, onInput);
        });
    }
    
    public RGBSelectButton(ItemStack itemStack, Consumer<Integer> onInput) {
        this(itemStack, false, onInput);
    }
    
    
    public static void openRGBSelectGUI(Player player, boolean nullable, Consumer<Integer> onInput) {
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
            if (nullable) {
                menu.addButton(new ArtButton(new ItemBuilder(Material.BARRIER).name(SystemLanguage.getText("gui-rgb-none")).build())
                        .listener((e, m) -> {
                            onInput.accept(null);
                            HistoryData historyData = HistoryData.getHistoryData(VanillaSource.getPlugin().getArtGUI(), player);
                            historyData.back();
                        }));
            }
            
            ItemStack inputButtonItem = new ItemBuilder(Material.NAME_TAG).name(SystemLanguage.getText("gui-input-rgb")).build();
            String title = SystemLanguage.getText("gui-select-color");
            String defaultText = "#FFFFFF";
            TextInputButton inputButton = new TextInputButton(inputButtonItem, title, defaultText);
            inputButton.onInput((p, text) -> {
                try {
                    int rgb = Integer.parseInt(text, 16);
                    onInput.accept(rgb);
                    return "back";
                } catch (NumberFormatException e) {
                    return "Invalid color code!";
                }
            });
            menu.addButton(inputButton);
            
            menu.addButton(new ColorButton(Material.WHITE_WOOL, Color.WHITE, SystemLanguage.getText("color-white"), onInput));
            menu.addButton(new ColorButton(Material.BLACK_WOOL, Color.BLACK, SystemLanguage.getText("color-black"), onInput));
            menu.addButton(new ColorButton(Material.BLUE_WOOL, Color.BLUE, SystemLanguage.getText("color-blue"), onInput));
            menu.addButton(new ColorButton(Material.BROWN_WOOL, "#56331C", SystemLanguage.getText("color-brown"), onInput));
            menu.addButton(new ColorButton(Material.CYAN_WOOL, "#267191", SystemLanguage.getText("color-cyan"), onInput));
            menu.addButton(new ColorButton(Material.GRAY_WOOL, Color.GRAY, SystemLanguage.getText("color-gray"), onInput));
            menu.addButton(new ColorButton(Material.GREEN_WOOL, Color.GREEN, SystemLanguage.getText("color-green"), onInput));
            menu.addButton(new ColorButton(Material.LIGHT_BLUE_WOOL, "#6387D2", SystemLanguage.getText("color-light-blue"), onInput));
            menu.addButton(new ColorButton(Material.LIGHT_GRAY_WOOL, "#A0A7A7", SystemLanguage.getText("color-light-gray"), onInput));
            menu.addButton(new ColorButton(Material.LIME_WOOL, Color.LIME, SystemLanguage.getText("color-lime"), onInput));
            menu.addButton(new ColorButton(Material.MAGENTA_WOOL, "#BE49C9", SystemLanguage.getText("color-magenta"), onInput));
            menu.addButton(new ColorButton(Material.ORANGE_WOOL, Color.ORANGE, SystemLanguage.getText("color-orange"), onInput));
            menu.addButton(new ColorButton(Material.PINK_WOOL, "#D98199", SystemLanguage.getText("color-pink"), onInput));
            menu.addButton(new ColorButton(Material.PURPLE_WOOL, Color.PURPLE, SystemLanguage.getText("color-purple"), onInput));
            menu.addButton(new ColorButton(Material.RED_WOOL, Color.RED, SystemLanguage.getText("color-red"), onInput));
            menu.addButton(new ColorButton(Material.YELLOW_WOOL, Color.YELLOW, SystemLanguage.getText("color-yellow"), onInput));
        });
    
        artMenu.open(player);
    }
    
    
    static class ColorButton extends ArtButton {
        
        public ColorButton(Material woolMaterial, Color color, String name, Consumer<Integer> onInput) {
            super(new ItemBuilder(woolMaterial).name(ChatColor.of("#" + String.format("%06x", color.asRGB())) + "&n" + name)
                    .lore("&r&7RGB : #" + String.format("%06x", color.asRGB())).build());
            listener((event, menu) -> {
                onInput.accept(color.asRGB());
                HumanEntity humanEntity = event.getWhoClicked();
                if (!(humanEntity instanceof Player)) {
                    return;
                }
                HistoryData historyData = HistoryData.getHistoryData(menu.getArtMenu().getArtGUI(), (Player) humanEntity);
                if (historyData != null) {
                    historyData.back();
                }
            });
        }
    
        public ColorButton(Material woolMaterial, String rgb, String name, Consumer<Integer> onInput) {
            this(woolMaterial, Color.fromRGB(Integer.parseInt(rgb.replace("#", ""), 16)), name, onInput);
        }
        
    }
    
}
