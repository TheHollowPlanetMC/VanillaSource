package thpmc.vanilla_source.api.contan;

import org.bukkit.Location;
import org.bukkit.World;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.environment.ContanObjectReference;
import org.contan_lang.standard.functions.StandardFunctions;
import org.contan_lang.syntax.tokens.Token;
import org.contan_lang.variables.primitive.ContanClassInstance;
import thpmc.vanilla_source.api.contan.functions.Floor;

public class ContanUtil {

    private static ContanClassInstance EMPTY_CLASS_INSTANCE;
    
    public static void setUpContan(ContanEngine contanEngine) {
        try {
            ContanModule emptyModule = contanEngine.compile("empty", "const instance = new Empty(); class Empty {}");
            ContanObjectReference instance = emptyModule.getModuleEnvironment().getVariable("instance");
            if (instance == null) {throw new IllegalStateException("");}

            EMPTY_CLASS_INSTANCE = (ContanClassInstance) instance.getContanObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        StandardFunctions.FUNCTIONS.put("floor", new Floor(null, new Token(null, "floor", 5, null, null), null));
    }

    public static void setEmptyClassInstance(ContanClassInstance emptyClassInstance) {
        EMPTY_CLASS_INSTANCE = emptyClassInstance;
    }

    public static Location createLocation(World world, double x, double y, double z) {
        return new Location(world, x, y, z);
    }
    
}
