package thpmc.engine.api.nms;

import thpmc.engine.api.player.EnginePlayer;

public interface IPacketHandler {

    Object rewrite(Object packet, EnginePlayer EnginePlayer, boolean cacheSetting);

}
