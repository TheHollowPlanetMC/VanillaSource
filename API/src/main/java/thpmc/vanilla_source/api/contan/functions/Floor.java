package thpmc.vanilla_source.api.contan.functions;

import org.bukkit.util.NumberConversions;
import org.contan_lang.ContanEngine;
import org.contan_lang.environment.Environment;
import org.contan_lang.environment.expection.ContanRuntimeError;
import org.contan_lang.evaluators.Evaluator;
import org.contan_lang.evaluators.FunctionBlock;
import org.contan_lang.syntax.tokens.Token;
import org.contan_lang.thread.ContanThread;
import org.contan_lang.variables.ContanObject;
import org.contan_lang.variables.primitive.ContanI64;
import org.jetbrains.annotations.Nullable;

public class Floor extends FunctionBlock {
    
    public Floor(ContanEngine contanEngine, Token functionName, Evaluator evaluator, Token... args) {
        super(contanEngine, functionName, evaluator, args);
    }
    
    @Override
    public ContanObject<?> eval(@Nullable Environment parentEnvironment, Token token, ContanThread contanThread, ContanObject<?>... contanObjects) {
        if (contanObjects.length != 1) {
            ContanRuntimeError.E0016.throwError("", null, token);
            return null;
        }
    
        if (!contanObjects[0].convertibleToDouble()) {
            ContanRuntimeError.E0016.throwError("", null, token);
            return null;
        }
        
        double number = contanObjects[0].toDouble();
        return new ContanI64(contanThread.getContanEngine(), (long) NumberConversions.floor(number));
    }
    
}
