package thpmc.vanilla_source.command.argument;

import net.propromp.neocommander.api.NeoCommandContext;
import net.propromp.neocommander.api.argument.CustomArgument;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.contan.ContanManager;

public class ModuleCandidateArgument extends CustomArgument<String> {
    
    public ModuleCandidateArgument(@NotNull String name) {
        super(name);
    }
    
    @Override
    public void suggest(@NotNull String s, @NotNull NeoCommandContext neoCommandContext, @NotNull com.mojang.brigadier.suggestion.SuggestionsBuilder suggestionsBuilder) {
        ContanManager.loadedModuleNames.forEach(suggestionsBuilder::suggest);
    }
    
    @Override
    public String parse(@NotNull NeoCommandContext neoCommandContext, String s) {
        return s;
    }
    
}