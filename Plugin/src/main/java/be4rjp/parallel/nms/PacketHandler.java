package be4rjp.parallel.nms;

import be4rjp.parallel.Config;
import thpmc.engine.api.nms.INMSHandler;
import thpmc.engine.api.player.EnginePlayer;
import io.netty.channel.*;


public class PacketHandler extends ChannelDuplexHandler{
    
    private final EnginePlayer EnginePlayer;
    
    public PacketHandler(EnginePlayer EnginePlayer){this.EnginePlayer = EnginePlayer;}
    
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
    
        INMSHandler nmsHandler = NMSManager.getNMSHandler();
        
        if(nmsHandler.isFlyPacket(packet)){
            super.channelRead(channelHandlerContext, NMSManager.getFlyPacketHandler().rewrite(packet, EnginePlayer, Config.isPerformanceMode()));
            return;
        }
        
        super.channelRead(channelHandlerContext, packet);
    }
    
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {

        INMSHandler nmsHandler = NMSManager.getNMSHandler();

        if(nmsHandler.isMapChunkPacket(packet)){
            super.write(channelHandlerContext, NMSManager.getMapChunkPacketHandler().rewrite(packet, EnginePlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
    
        if(nmsHandler.isLightUpdatePacket(packet) && Config.isRewriteLightPacket()){
            super.write(channelHandlerContext, NMSManager.getLightUpdatePacketHandler().rewrite(packet, EnginePlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
        
        if(nmsHandler.isBlockChangePacket(packet)){
            super.write(channelHandlerContext, NMSManager.getBlockChangePacketHandler().rewrite(packet, EnginePlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
        
        if(nmsHandler.isMultiBlockChangePacket(packet)){
            super.write(channelHandlerContext, NMSManager.getMultiBlockChangePacketHandler().rewrite(packet, EnginePlayer, Config.isPerformanceMode()), channelPromise);
            return;
        }
        
        super.write(channelHandlerContext, packet, channelPromise);
    }

}