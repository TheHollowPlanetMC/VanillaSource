package thpmc.vanilla_source.api.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemInstanceHolder {

    private static final Map<UUID, EngineItemInstance> engineItemMap = new HashMap<>();

    public static void resister(@NotNull UUID uuid, @NotNull EngineItemInstance engineItemInstance) {
        engineItemMap.put(uuid, engineItemInstance);
    }

    public static @Nullable EngineItemInstance getEngineItemFromUUID(UUID uuid) {
        return engineItemMap.get(uuid);
    }

    public static void remove(@NotNull UUID uuid) {
        engineItemMap.remove(uuid);
    }

}
