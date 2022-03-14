package thpmc.engine.gui;

import be4rjp.artgui.button.*;
import be4rjp.artgui.frame.Artist;
import be4rjp.artgui.menu.ArtMenu;
import thpmc.engine.api.world.parallel.ParallelUniverse;
import thpmc.engine.THPEngine;
import thpmc.engine.api.THPEngineAPI;
import thpmc.engine.api.player.EnginePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class UniverseGUI {

    public static void openUniverseGUI(Player player){

        EnginePlayer enginePlayer = EnginePlayer.getParallelPlayer(player);
        if(enginePlayer == null) return;

        Artist artist = new Artist(() -> {

            ArtButton V = null;

            ArtButton G = new ArtButton(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("&a").build());

            PageNextButton N = new PageNextButton(new ItemBuilder(Material.ARROW).name("&rNext page &7[{NextPage}/{MaxPage}]").build());

            PageBackButton P = new PageBackButton(new ItemBuilder(Material.ARROW).name("&rPrevious page &7[{PreviousPage}/{MaxPage}]").build());

            MenuBackButton B = new MenuBackButton(new ItemBuilder(Material.OAK_DOOR).name("&7Back to &r{PreviousName}").build());

            ArtButton L = new ArtButton(new ItemBuilder(Material.BARRIER).name("&b&nLeave from current universe").build())
                    .listener((inventoryClickEvent, menu) -> {
                        enginePlayer.setUniverse(null);
                        player.closeInventory();
                    });

            return new ArtButton[]{
                    V, V, V, V, V, V, V, G, G,
                    V, V, V, V, V, V, V, G, N,
                    V, V, V, V, V, V, V, G, P,
                    V, V, V, V, V, V, V, G, G,
                    V, V, V, V, V, V, V, G, L,
                    V, V, V, V, V, V, V, G, B,
            };
        });

        ArtMenu artMenu = artist.createMenu(THPEngine.getPlugin().getArtGUI(), "&nUniverse list");
        artMenu.asyncCreate(menu -> {

            for(ParallelUniverse universe : THPEngineAPI.getInstance().getAllUniverse()){
                menu.addButton(new ArtButton(new ItemBuilder(Material.END_PORTAL_FRAME).name(universe.getName()).lore("&7Click to join.").build()).listener((inventoryClickEvent, menu1) -> {
                    enginePlayer.setUniverse(universe);
                    player.closeInventory();
                    player.sendMessage("§7Switched to §r" + universe.getName());
                }));
            }

        });

        artMenu.open(player);

    }

}
