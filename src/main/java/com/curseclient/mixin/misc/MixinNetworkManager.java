package com.curseclient.mixin.misc;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.EventProcessor;
import com.curseclient.client.event.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketPre(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Send(packet);
        EventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"), cancellable = true)
    private void sendPacketPost(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.PostSend(packet);
        EventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent event = new PacketEvent.Receive(packet);
        EventBus.INSTANCE.post(event);

        if (event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void channelReadPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketEvent.PostReceive event = new PacketEvent.PostReceive(packet);
        EventBus.INSTANCE.post(event);
        EventProcessor.handlePacketReceive(event);
    }

}
