package thpmc.vanilla_source.world_edit;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import thpmc.vanilla_source.lang.SystemLanguage;
import thpmc.vanilla_source.util.RegionBlocks;

public class WorldEditUtil {

    public static RegionBlocks getSelectedRegion(Player player) {
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
        LocalSession localSession = sessionManager.get(wePlayer);

        com.sk89q.worldedit.world.World selectionWorld = localSession.getSelectionWorld();
        Region region;
        try {
            if (selectionWorld == null) throw new IncompleteRegionException();
            region = localSession.getSelection(selectionWorld);
        } catch (IncompleteRegionException ex) {
            player.sendMessage(SystemLanguage.getText("world-edit-not-selected"));
            return null;
        }

        BlockVector3 max = region.getMaximumPoint();
        BlockVector3 min = region.getMinimumPoint();

        World world = BukkitAdapter.adapt(region.getWorld());

        Vector maxLocation = new Vector(max.getX(), max.getY(), max.getZ());
        Vector minLocation = new Vector(min.getX(), min.getY(), min.getZ());

        return new RegionBlocks(minLocation.toLocation(world), maxLocation.toLocation(world));
    }

}
