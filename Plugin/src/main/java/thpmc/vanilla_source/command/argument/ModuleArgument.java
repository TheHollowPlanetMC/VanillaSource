package thpmc.vanilla_source.command.argument;

import net.propromp.neocommander.api.NeoCommandContext;
import net.propromp.neocommander.api.argument.CustomArgument;
import org.contan_lang.ContanModule;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.VanillaSourceAPI;
import thpmc.vanilla_source.contan.ContanManager;

public class ModuleArgument extends CustomArgument<ContanModule> {

    public ModuleArgument(@NotNull String name) {
        super(name);
    }

    @Override
    public void suggest(@NotNull String s, @NotNull NeoCommandContext neoCommandContext, @NotNull com.mojang.brigadier.suggestion.SuggestionsBuilder suggestionsBuilder) {
        ContanManager.loadedModuleNames.forEach(suggestionsBuilder::suggest);
    }

    @Override
    public ContanModule parse(@NotNull NeoCommandContext neoCommandContext, String s) {
        ContanModule module = VanillaSourceAPI.getInstance().getContanEngine().getModule(s);
        if (module != null) {
            return module;
        }

        throw new IllegalArgumentException("Module '" + s + "' is not found.");
    }

}
