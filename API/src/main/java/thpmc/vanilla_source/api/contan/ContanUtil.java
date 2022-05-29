package thpmc.vanilla_source.api.contan;

import org.contan_lang.standard.functions.StandardFunctions;
import org.contan_lang.syntax.tokens.Token;
import thpmc.vanilla_source.api.contan.functions.Floor;

public class ContanUtil {
    
    public static void setUpContan() {
        StandardFunctions.FUNCTIONS.put("floor", new Floor(null, new Token(null, "floor", 5, null, null), null));
    }
    
}
