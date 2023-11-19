package com.curseclient.client.event

import com.curseclient.client.event.events.*
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.events.render.ResolutionUpdateEvent
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.gui.impl.maingui.MainGui
import com.curseclient.client.manager.managers.ESPManager
import com.curseclient.client.utility.math.FPSCounter
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraftforge.client.event.*
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.living.LivingKnockBackEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard
import scala.collection.mutable.Subscriber


internal object EventProcessor {
    private val mc = Minecraft.getMinecraft()
    private var prevWidth = mc.displayWidth
    private var prevHeight = mc.displayHeight

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        EventBus.post(event)

        if (event.phase == TickEvent.Phase.END && (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight)) {
            prevWidth = mc.displayWidth
            prevHeight = mc.displayHeight
            EventBus.post(ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight))
        }
    }

    @SubscribeEvent
    fun guiOpenEvent(event: GuiOpenEvent){
        if (event.gui is GuiMainMenu) {
            event.gui = MainGui()
        }
    }

    @JvmStatic
    fun handlePacketReceive(event: PacketEvent.PostReceive) {
        runSafe {
            if (event.packet is SPacketEntityStatus && event.packet.opCode.toInt() == 35 && player.isEntityAlive) {
                (event.packet.getEntity(world) as? EntityPlayer)?.let {
                    EventBus.post(TotemPopEvent(it))
                }
            }
        }
    }

    @SubscribeEvent
    fun onServerDisconnect(event: FMLNetworkEvent.ServerDisconnectionFromClientEvent) {
        EventBus.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        EventBus.post(ConnectionEvent.Disconnect())
    }

    @SubscribeEvent
    fun onClientConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        EventBus.post(ConnectionEvent.Connect())
    }

    @SubscribeEvent
    fun onLivingKnockBack(event: LivingKnockBackEvent){
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onHandRender(event: RenderSpecificHandEvent){
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        EventBus.post(Render3DEvent())

        ESPManager.render()
        FPSCounter.tick()
    }

    @SubscribeEvent
    fun onRenderPre(event: RenderGameOverlayEvent.Pre) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onUpdate(event: LivingEvent.LivingUpdateEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onRenderBlock(event: RenderBlockOverlayEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onRender3D(event: RenderWorldLastEvent) {
        EventBus.post(Render3DEvent())
    }

    @SubscribeEvent
    fun onRender2D(event: RenderWorldLastEvent) {
        EventBus.post(Render2DEvent())
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        EventBus.post(event)
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (!Keyboard.getEventKeyState()) return
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onEventMouse(event: InputEvent.MouseInputEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatSent(event: ClientChatEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onWorldEvent(event: WorldEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onBlockEvent(event: BlockEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onInputUpdate(event: InputUpdateEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onLivingEntityUseItemEventTick(event: LivingEntityUseItemEvent.Tick) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onClientChat(event: ClientChatReceivedEvent) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onRenderFogColors(event: EntityViewRenderEvent.FogColors) {
        EventBus.post(event)
    }

    @SubscribeEvent
    fun onRenderArmFov(event: EntityViewRenderEvent.FOVModifier) {
        EventBus.post(event)
    }
}