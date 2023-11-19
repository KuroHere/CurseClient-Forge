package com.curseclient.client.module.modules.player

import com.curseclient.client.event.events.AttackEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.Wrapper.world
import com.curseclient.client.utility.extension.mixins.useEntityId
import com.curseclient.client.utility.player.RotationUtils.normalizeAngle
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.MoverType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameType
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import kotlin.math.*

object FreeCam : Module(
    "FreeCam",
    "Allows you to fly out of player",
    Category.PLAYER
) {
    private val horizontalSpeed by setting("Horizontal Speed", 1.0, 0.1, 2.0, 0.1)
    private val verticalSpeed by setting("Vertical Speed", 1.0, 0.1, 2.0, 0.1)

    var camera: EntityOtherPlayerMP? = null; private set
    private const val ENTITY_ID = -6969420

    init {
        safeListener<TickEvent.ClientTickEvent> {
            camera?.let {
                it.health = player.health
                it.absorptionAmount = player.absorptionAmount
            }
        }

        safeListener<InputEvent.KeyInputEvent> {
            if (mc.gameSettings.keyBindTogglePerspective.isKeyDown) mc.gameSettings.thirdPersonView = 2
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity) return@safeListener
            if (it.packet.useEntityId == player.entityId) it.cancel()
        }

        safeListener<AttackEvent.Pre> {
            if (it.entity == player) it.cancel()
        }
    }

    override fun onEnable() {
        camera = FakeCamera(mc.player.world as WorldClient, mc.player)

        world?.addEntityToWorld(ENTITY_ID, camera!!)

        camera!!.posX = mc.player.posX
        camera!!.posY = mc.player.posY + 1
        camera!!.posZ = mc.player.posZ

        camera!!.turn(mc.player.cameraYaw, mc.player.cameraPitch)

        camera!!.setGameType(GameType.SURVIVAL)

        mc.renderViewEntity = camera

        mc.renderChunksMany = false

        mc.gameSettings.thirdPersonView = 0
    }

    override fun onDisable() {
        mc.renderViewEntity = mc.player
        if(camera != null) mc.world.removeEntityFromWorld(ENTITY_ID)
        mc.renderChunksMany = true
    }

    private class FakeCamera(world: WorldClient, val player: EntityPlayerSP) : EntityOtherPlayerMP(world, mc.session.profile) {
        init {
            copyLocationAndAnglesFrom(player)
            capabilities.allowFlying = true
            capabilities.isFlying = true
        }

        override fun onLivingUpdate() {
            // Update inventory
            inventory.copyInventory(player.inventory)

            // Update yaw head
            updateEntityActionState()

            // We have to update movement input from key binds because mc.player.movementInput is used by Baritone
            val forward = mc.gameSettings.keyBindForward.isKeyDown to mc.gameSettings.keyBindBack.isKeyDown
            val strafe = mc.gameSettings.keyBindLeft.isKeyDown to mc.gameSettings.keyBindRight.isKeyDown
            val vertical = mc.gameSettings.keyBindJump.isKeyDown to mc.gameSettings.keyBindSneak.isKeyDown
            val movementInput = calcMovementInput(forward, strafe, vertical)

            moveForward = movementInput.first
            moveStrafing = movementInput.second
            moveVertical = movementInput.third

            // Update sprinting
            isSprinting = mc.gameSettings.keyBindSprint.isKeyDown

            val absYaw = getRotationFromVec(Vec3d(moveStrafing.toDouble(), 0.0, moveForward.toDouble())).x
            fun Double.toRadian() = (this / 180.0f * 3.1415926)
            val yawRad = (rotationYaw - absYaw).toDouble().toRadian()
            val speed = horizontalSpeed * min(abs(moveForward) + abs(moveStrafing), 1.0f)

            motionX = -sin(yawRad) * speed
            motionZ = cos(yawRad) * speed
            motionY = moveVertical.toDouble() * verticalSpeed

            if (isSprinting) {
                motionX *= 1.5
                motionZ *= 1.5
            }

            move(MoverType.SELF, motionX, motionY, motionZ)
        }

        override fun getEyeHeight() = 1.65f

        override fun isSpectator() = true

        override fun isInvisible() = true

        override fun isInvisibleToPlayer(player: EntityPlayer) = true
    }

    private fun calcMovementInput(forward: Pair<Boolean, Boolean>, strafe: Pair<Boolean, Boolean>, vertical: Pair<Boolean, Boolean>): Triple<Float, Float, Float> {
        // Forward movement input
        val moveForward = if (forward.first xor forward.second) {
            if (forward.first) 1.0f else -1.0f
        } else {
            0.0f
        }

        // Strafe movement input
        val moveStrafing = if (strafe.first xor strafe.second) {
            if (strafe.second) 1.0f else -1.0f
        } else {
            0.0f
        }

        // Vertical movement input
        val moveVertical = if (vertical.first xor vertical.second) {
            if (vertical.first) 1.0f else -1.0f
        } else {
            0.0f
        }

        return Triple(moveForward, moveStrafing, moveVertical)
    }

    private fun getRotationFromVec(vec: Vec3d): Vec2f {
        val xz = hypot(vec.x, vec.z)
        val yaw = normalizeAngle(Math.toDegrees(atan2(vec.z, vec.x)) - 90.0)
        val pitch = normalizeAngle(Math.toDegrees(-atan2(vec.y, xz)))
        return Vec2f(yaw.toFloat(), pitch.toFloat())
    }

    @JvmStatic
    fun handleTurn(entity: Entity, yaw: Float, pitch: Float, ci: CallbackInfo): Boolean {
        if (!isEnabled()) return false
        val player = mc.player ?: return false
        val cameraGuy = camera ?: return false

        return if (entity == player) {
            cameraGuy.turn(yaw, pitch)
            ci.cancel()
            true
        } else {
            false
        }
    }
}