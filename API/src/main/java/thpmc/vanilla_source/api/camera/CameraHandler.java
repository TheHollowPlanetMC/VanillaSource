package thpmc.vanilla_source.api.camera;

import org.contan_lang.variables.primitive.ContanClassInstance;
import org.jetbrains.annotations.NotNull;
import thpmc.vanilla_source.api.entity.TickBase;
import thpmc.vanilla_source.api.player.EnginePlayer;

public class CameraHandler implements TickBase {

    private final EnginePlayer owner;

    private final ContanClassInstance scriptHandle;

    public CameraHandler(EnginePlayer owner, @NotNull ContanClassInstance scriptHandle) {
        this.owner = owner;
        this.scriptHandle = scriptHandle;
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean shouldRemove() {
        return false;
    }

}
