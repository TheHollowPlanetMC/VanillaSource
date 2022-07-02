package thpmc.vanilla_source.command;

import net.propromp.neocommander.api.annotation.Command;
import net.propromp.neocommander.api.annotation.Sender;
import net.propromp.neocommander.api.argument.annotation.StringArgument;
import org.bukkit.entity.Player;
import org.contan_lang.ContanEngine;
import org.contan_lang.ContanModule;
import org.contan_lang.variables.ContanObject;
import thpmc.vanilla_source.api.VanillaSourceAPI;

@Command(name = "contan", permission = "vanilla_source.contan", senderType = Player.class)
public class contanCommand {

    @Command(name = "run", description = "Run contan script.")
    public void run(@Sender Player sender, @StringArgument String code) {
        ContanEngine contanEngine = VanillaSourceAPI.getInstance().getContanEngine();

        //Compile and run script.
        try {
            ContanModule contanModule = contanEngine.compile(".command", code);
            ContanObject<?> result = contanModule.getGlobalEvaluator().eval(contanModule.getModuleEnvironment());
            sender.sendMessage("§aResult : " + result);
        } catch (Exception e) {
            sender.sendMessage(e.getMessage());
        }
    }

    public void invoke(@Sender Player sender) {

    }

}
