package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.MoveEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.combat.KillAura
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.player.RotationUtils.rotationsToCenter
import com.curseclient.client.utility.player.TargetingUtils
import net.minecraft.entity.EntityLivingBase
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object TargetStrafe : Module(
    "TargetStrafe",
    "Strafe around nearest target",
    Category.MOVEMENT
) {

    private val groundSpeed by setting("Ground Speed", 1.0, 0.1, 1.0, 0.01)
    private val airSpeed by setting("Air Speed", 1.0, 0.1, 1.0, 0.01)
    private val reach by setting("Reach", 5.0, 1.0, 10.0, 0.1)
    private val distance by setting("Distance", 3.0, 1.0, 3.0, 0.1)
    private val fallSpeedBoost by setting("Fall Speed Boost", 0.0, 0.0, 2.0, 0.1)
    private val damageBoost by setting("Damage Boost", 0.0, 0.0, 2.0, 0.1)
    private val direction by setting("Direction", Direction.Smart)
    private val autoJump by setting("Auto Jump", true)

    private var customDirection = -1f
    private var ticksAfterLastChange = 0

    private enum class Direction {
        Left,
        Right,
        Smart
    }

    private fun SafeClientEvent.calcSpeed(): Double {
        var speed = 1.0

        val damageBoostTime = player.hurtTime.toDouble() / max(player.maxHurtTime.toDouble(), 1.0)
        val damageBoostAmount = 1.0 + damageBoost * damageBoostTime
        speed *= damageBoostAmount

        var speedModifier = groundSpeed
        if (!player.onGround) speedModifier = airSpeed * (1.0 + clamp(player.fallDistance, 0.0f, 1.0f) * fallSpeedBoost)

        speed *= speedModifier

        return speed
    }

    init {
        safeListener<MoveEvent> {
            val target = TargetingUtils.getTarget(reach, KillAura.ignoreWalls) ?: return@safeListener

            if (canStrafe()) {
                if (autoJump && player.onGround) player.jump()

                player.motionX = -sin(getDirection(target)) * calcSpeed() * 0.287
                player.motionZ = cos(getDirection(target)) * calcSpeed() * 0.287
            }

            if (player.collidedHorizontally && ticksAfterLastChange >= 5) {
                customDirection *= -1f
                ticksAfterLastChange = 0
            }

            ticksAfterLastChange++
        }
    }

    override fun onEnable() {
        customDirection = -1f
        ticksAfterLastChange = 0
    }

    private fun canStrafe(): Boolean {
        return !mc.player.capabilities.isFlying &&
            !mc.player.isElytraFlying &&
            !mc.gameSettings.keyBindSneak.isKeyDown &&
            !mc.player.isInWater &&
            !mc.player.isInLava
    }

    private fun getDirection(target: EntityLivingBase): Double {
        val forwardMove =
            if(mc.player.getDistance(target) > distance + 0.75f) 1f
            else if(mc.player.getDistance(target) < distance - 0.75f) -1f
            else 0f
        val strafeMove =
            when (direction) {
                Direction.Left -> 1f
                Direction.Right -> -1f
                else -> customDirection
            }
        var rotationYaw = rotationsToCenter(target).x

        if (forwardMove < 0f) rotationYaw += 180f
        var forward = 1f
        if (forwardMove < 0f) forward = -0.5f else if (forwardMove > 0f) forward = 0.5f
        if (strafeMove > 0f) rotationYaw -= 90f * forward
        if (strafeMove < 0f) rotationYaw += 90f * forward
        return Math.toRadians(rotationYaw.toDouble())
    }
}