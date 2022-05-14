package thpmc.vanilla_source.api.nms;

import thpmc.vanilla_source.api.player.EnginePlayer;

public interface IPacketHandler {

    Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting);

}
