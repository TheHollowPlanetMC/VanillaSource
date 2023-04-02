package thpmc.vanilla_source.api.contan;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.environment.ContanObjectReference;
import org.contan_lang.runtime.JavaContanFuture;
import org.contan_lang.standard.functions.StandardFunctions;
import org.contan_lang.syntax.tokens.Token;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanClassInstance;
import org.contan_lang.variables.primitive.JavaClassObject;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.contan.functions.Floor;
import thpmc.vanilla_source.api.entity.tick.TickThread;
import thpmc.vanilla_source.api.util.Unicode;

import java.util.concurrent.ExecutionException;

public class ContanUtil {

    private static ContanClassInstance EMPTY_CLASS_INSTANCE;
    
    private static ContanModule utilModule;
    
    public static void setUpContan(ContanEngine contanEngine) {
        try {
            String utilCode = "\n" +
                    "const instance = new Empty()\n" +
                    "\n" +
                    "class Empty() {}\n" +
                    "\n" +
                    "function createFuture() { return new Future() }";
            
            utilModule = contanEngine.compile("vs_util", utilCode);
            utilModule.initialize(contanEngine.getMainThread());
            ContanObjectReference instance = utilModule.getModuleEnvironment().getVariable("instance");
            if (instance == null) {throw new IllegalStateException("");}

            EMPTY_CLASS_INSTANCE = (ContanClassInstance) instance.getContanObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        StandardFunctions.FUNCTIONS.put("floor", new Floor(null, new Token(null, "floor", 5, null, null), null));
        contanEngine.setRuntimeVariable("Vector", new JavaClassObject(contanEngine, Vector.class));
        contanEngine.setRuntimeVariable("Bukkit", new JavaClassObject(contanEngine, Bukkit.class));
        contanEngine.setRuntimeVariable("World", new JavaClassObject(contanEngine, World.class));
        contanEngine.setRuntimeVariable("Location", new JavaClassObject(contanEngine, Location.class));
        contanEngine.setRuntimeVariable("ContanUtil", new JavaClassObject(contanEngine, ContanUtil.class));
        contanEngine.setRuntimeVariable("Unicode", new JavaClassObject(contanEngine, Unicode.class));
        contanEngine.setRuntimeVariable("HoverText", new JavaClassObject(contanEngine, HoverText.class));
    }
    
    public static JavaContanFuture createFutureInstance() {
        try {
            ContanClassInstance future = (ContanClassInstance) utilModule.invokeFunction(VanillaSourceAPI.getInstance().getMainThread(), "createFuture");
            ContanObjectReference reference = future.getEnvironment().getVariable("javaFuture");
            if (reference == null) {
                throw new IllegalStateException();
            }
            
            return (JavaContanFuture) reference.getContanObject().convertToJavaObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static ContanClassInstance getEmptyClassInstance() {return EMPTY_CLASS_INSTANCE;}
    
    public static Location createLocation(World world, double x, double y, double z) {
        return new Location(world, x, y, z);
    }
    
}
