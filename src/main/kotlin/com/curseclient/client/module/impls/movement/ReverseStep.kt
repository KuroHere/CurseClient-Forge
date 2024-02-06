package com.curseclient.client.module.impls.movement

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.events.TravelEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.calcIsInWater
import com.curseclient.client.utility.math.MathUtils.ceilToInt
import com.curseclient.client.utility.math.MathUtils.floorToInt
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.max

object ReverseStep : Module(
    "ReverseStep",
    "Reverse step :)",
    Category.MOVEMENT
) {
    private val height by setting("Height", 2.0, 0.25, 8.0, 0.1)
    private val speed by setting("Speed", 1.0, 0.1, 10.0, 0.1)

    private var lastSetbackTime = 0L

    init {
        safeListener<PacketEvent.Receive> {
            if (it.packet is SPacketPlayerPosLook) {
                lastSetbackTime = System.currentTimeMillis()
            }
        }

        safeListener<TravelEvent>(100) {
            if (!check()) return@safeListener

            player.motionY -= speed
        }
    }

    private fun SafeClientEvent.check(): Boolean {
        return !mc.gameSettings.keyBindSneak.isKeyDown
            && !mc.gameSettings.keyBindJump.isKeyDown
            && !player.isElytraFlying
            && !player.capabilities.isFlying
            && !player.isOnLadder
            && !player.calcIsInWater()
            && player.onGround
            && player.motionY in -0.1..0.0
            && (System.currentTimeMillis() - lastSetbackTime > 3000)
            && checkGroundLevel()
    }

    private fun SafeClientEvent.checkGroundLevel(): Boolean {
        return player.posY - world.getGroundLevel(player.entityBoundingBox) in 0.25..height
            || player.posY - world.getGroundLevel(player.entityBoundingBox.offset(player.motionX, 0.0, player.motionZ)) in 0.25..height
            || player.posY - world.getGroundLevel(player.entityBoundingBox.offset(player.motionX * 2.0, 0.0, player.motionZ * 2.0)) in 0.25..height
    }

    private fun World.getGroundLevel(boundingBox: AxisAlignedBB): Double {
        var maxY = Double.MIN_VALUE
        val pos = BlockPos.PooledMutableBlockPos.retain()

        for (x in (boundingBox.minX - 0.1).floorToInt()..(boundingBox.maxX + 0.1).floorToInt()) {
            for (z in (boundingBox.minZ - 0.1).floorToInt()..(boundingBox.maxZ + 0.1).floorToInt()) {
                for (y in (boundingBox.minY - 0.5).floorToInt() downTo -1) {
                    if (y < maxY.ceilToInt() - 1) break

                    pos.setPos(x, y, z)
                    val box = this.getBlockState(pos).getCollisionBoundingBox(this, pos)
                    if (box != null) {
                        maxY = max(maxY, y + box.maxY)
                    }
                }
            }
        }

        return if (maxY == Double.MIN_VALUE) -999.0 else maxY
    }
}
