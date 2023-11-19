package com.curseclient.client.module.modules.combat

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PlayerPacketEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.calcIsInWater
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.client.utility.player.Rotations.getRotationsByMode
import com.curseclient.client.utility.player.RotationsMode
import com.curseclient.client.utility.player.TargetingUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec2f
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.pow


object KillAura : Module(
    "KillAura",
    "Automatically attacks nearest entity",
    Category.COMBAT
) {
    private val page by setting("Page", Page.General)

    private val attackMode by setting("AttackMode", AttackMode.Cooldown, visible = { page == Page.General })
    private val delayMin by setting("Min Delay", 12.0, 1.0, 20.0, 1.0, visible = { attackMode == AttackMode.Delay &&  page == Page.General })
    private val delayMax by setting("Max Delay", 12.0, 1.0, 20.0, 1.0, visible = { attackMode == AttackMode.Delay &&  page == Page.General })
    private val criticalSync by setting("Critical Sync", true, visible = { attackMode == AttackMode.Cooldown &&  page == Page.General })
    private val cancelSprinting by setting("Cancel Sprinting", false, visible = { page == Page.General })
    private val rotationMode by setting("Rotation Mode", RotationMode.Center, visible = { page == Page.General})
    private val prerotateDistance by setting("Prerotate Distance", 0.0, 0.0, 5.0, 0.1, visible = { rotationMode != RotationMode.None && page == Page.General })
    val maxPitch by setting("Pitch Limit", 85.0, 1.0, 90.0, 1.0, visible = { rotationMode != RotationMode.None && page == Page.General })
    private val spoofRotation by setting("Spoof Rotation", true, visible = { rotationMode != RotationMode.None && page == Page.General })

    val reach by setting("Reach", 4.2, 1.0, 6.0, 0.1, visible = { page == Page.Targeting })
    val ignoreWalls by setting("IgnoreWalls", true, visible = { page == Page.Targeting })
    val players by setting("Players", true, visible = { page == Page.Targeting })
    val friends by setting("Friends", true, visible = { players && page == Page.Targeting })
    val invisible by setting("Invisible", true, visible = { page == Page.Targeting })
    val animals by setting("Animals", true, visible = { page == Page.Targeting })
    val hostileMobs by setting("Hostile", true, visible = { page == Page.Targeting })

    private var groundTicks = 0

    private enum class Page {
        General,
        Targeting
    }

    private enum class RotationMode {
        None,
        Center,
        Smart,
        Matrix
    }

    private enum class AttackMode {
        Cooldown,
        Delay
    }

    private var delay = 0


    override fun getHudInfo() = "${rotationMode.settingName} | ${attackMode.settingName}"

    private var ticksAfterLastHit = 0

    var target: EntityLivingBase? = null

    override fun onEnable() {
        target = null
        ticksAfterLastHit = 0
        delay = delayMin.toInt()
        groundTicks = 0
    }

    init {
        safeListener<ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener
            ticksAfterLastHit++

            target = TargetingUtils.getTarget(reach, ignoreWalls)

            if (!shouldAttack() || target == null) return@safeListener

            attack(target!!)
        }

        safeListener<Render3DEvent> {
            val t = TargetingUtils.getTarget(reach + prerotateDistance, ignoreWalls)
            if (t == null || rotationMode == RotationMode.None) return@safeListener
            val rotations = getRotations(t)

            if (!spoofRotation) {
                player.rotationYaw = rotations.x
                player.rotationPitch = rotations.y
            }

        }

        safeListener<PlayerPacketEvent.Data>(-1) {
            val t = TargetingUtils.getTarget(reach + prerotateDistance, ignoreWalls)
            if (!spoofRotation || t == null || rotationMode == RotationMode.None) return@safeListener
            val rotations = getRotations(t)
            it.yaw = rotations.x
            it.pitch = rotations.y
        }

        safeListener<PlayerPacketEvent.Post> {
            groundTicks = (groundTicks + 1) * PacketManager.lastReportedOnGround.toInt()
        }
    }

    private fun SafeClientEvent.attack(entity: EntityLivingBase){
        val flag = PacketManager.lastReportedSprinting && cancelSprinting

        if (flag) {
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SPRINTING))
            PacketManager.lastReportedSprinting = false
        }

        playerController.attackEntity(player, entity)
        player.swingArm(EnumHand.MAIN_HAND)
        updateDelay()

        if (flag) {
            connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SPRINTING))
            PacketManager.lastReportedSprinting = true
        }
    }

    private fun easeInOutQuart(x: Double): Double {
        return if (x < 0.5) 8 * x * x * x * x else 1 - (-2 * x + 2).pow(4.0) / 2
    }

    private fun getRotations(entity: Entity): Vec2f {
        var mode: RotationsMode = RotationsMode.CENTER
        when(rotationMode){
            RotationMode.None -> {}
            RotationMode.Center -> mode = RotationsMode.CENTER
            RotationMode.Smart -> mode = RotationsMode.SMART
            RotationMode.Matrix -> mode = RotationsMode.MATRIX
        }
        return getRotationsByMode(entity, mode)
    }

    private fun SafeClientEvent.shouldAttack(): Boolean {
        return ((player.getCooledAttackStrength(0.0f) > 0.9f) && attackMode == AttackMode.Cooldown && checkCritical()) ||
            (ticksAfterLastHit > delay && attackMode == AttackMode.Delay)
    }

    private fun SafeClientEvent.checkCritical(): Boolean {
        if (player.calcIsInWater() || !criticalSync) return true

        return (PacketManager.lastReportedPosY < PacketManager.prevReportedPos.y && !PacketManager.lastReportedOnGround) || groundTicks > 4
    }

    private fun updateDelay(){
        var min = delayMin.toInt()
        var max = delayMax.toInt()

        if(min == max) {
            delay = min
            return
        }

        if(min > max){
            val temp = min
            min = max
            max = temp
        }

        delay = (min..max).random()
        ticksAfterLastHit = 0
    }

    override fun onDisable() {
        target = null
    }
}