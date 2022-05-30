package thpmc.vanilla_source.api.contan;

import org.bukkit.Location;
import org.bukkit.World;
import org.contan_lang.standard.functions.StandardFunctions;
import org.contan_lang.syntax.tokens.Token;
import thpmc.vanilla_source.api.contan.functions.Floor;

public class ContanUtil {
    
    public static void setUpContan() {
        StandardFunctions.FUNCTIONS.put("floor", new Floor(null, new Token(null, "floor", 5, null, null), null));
    }

    public static Location createLocation(World world, double x, double y, double z) {
        return new Location(world, x, y, z);
    }
    
}
