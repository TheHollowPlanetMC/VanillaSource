package thpmc.vanilla_source.nms;

import thpmc.vanilla_source.config.ImplVSSettings;
import thpmc.vanilla_source.api.nms.INMSHandler;
import thpmc.vanilla_source.api.player.EnginePlayer;
import io.netty.channel.*;


public class PacketHandler extends ChannelDuplexHandler{
    
    private final EnginePlayer EnginePlayer;
    
    public PacketHandler(EnginePlayer EnginePlayer){this.EnginePlayer = EnginePlayer;}
    
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
    
        INMSHandler nmsHandler = NMSManager.getNMSHandler();
        
        if(nmsHandler.isFlyPacket(packet)){
            super.channelRead(channelHandlerContext, NMSManager.getFlyPacketHandler().rewrite(packet, EnginePlayer, ImplVSSettings.isUseCachedChunkPacket()));
            return;
        }
        
        super.channelRead(channelHandlerContext, packet);
    }
    
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {

        INMSHandler nmsHandler = NMSManager.getNMSHandler();

        if(nmsHandler.isMapChunkPacket(packet)){
            super.write(channelHandlerContext, NMSManager.getMapChunkPacketHandler().rewrite(packet, EnginePlayer, ImplVSSettings.isUseCachedChunkPacket()), channelPromise);
            return;
        }
    
        if(nmsHandler.isLightUpdatePacket(packet) && ImplVSSettings.isRewriteLightPacket()){
            super.write(channelHandlerContext, NMSManager.getLightUpdatePacketHandler().rewrite(packet, EnginePlayer, ImplVSSettings.isUseCachedChunkPacket()), channelPromise);
            return;
        }
        
        if(nmsHandler.isBlockChangePacket(packet)){
            super.write(channelHandlerContext, NMSManager.getBlockChangePacketHandler().rewrite(packet, EnginePlayer, ImplVSSettings.isUseCachedChunkPacket()), channelPromise);
            return;
        }
        
        if(nmsHandler.isMultiBlockChangePacket(packet)){
            super.write(channelHandlerContext, NMSManager.getMultiBlockChangePacketHandler().rewrite(packet, EnginePlayer, ImplVSSettings.isUseCachedChunkPacket()), channelPromise);
            return;
        }
        
        super.write(channelHandlerContext, packet, channelPromise);
    }

}