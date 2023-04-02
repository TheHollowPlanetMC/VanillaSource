package thpmc.vanilla_source.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.contan_lang.variables.primitive.ContanFunctionExpression;
import org.contan_lang.variables.primitive.JavaClassInstance;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.api.contan.HoverText;

public class HoverTextCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args == null) return true;
        if (args.length == 0) return true;
        if (!(commandSender instanceof Player)) return true;

        Player player = (Player) commandSender;

        long id = -1;
        try {
            id = Long.parseLong(args[0]);
        } catch (Exception e) { /*Ignore*/ }

        if (id == -1) {
            return true;
        }

        HoverText hoverText = HoverText.getHoverText(id);
        if (hoverText != null && hoverText.isAllowed(player)) {
            ContanFunctionExpression event = hoverText.getClickEvent();
            if (event != null) {
                event.eval(VanillaSourceAPI.getInstance().getMainThread(), event.getBasedJavaObject().getFunctionName(),
                        new JavaClassInstance(VanillaSourceAPI.getInstance().getContanEngine(), player));
            }
        }

        return true;
    }
}
