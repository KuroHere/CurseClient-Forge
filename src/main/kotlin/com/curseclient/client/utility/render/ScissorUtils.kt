package com.curseclient.client.utility.render

import com.curseclient.client.utility.math.MathUtils.ceilToInt
import com.curseclient.client.utility.math.MathUtils.floorToInt
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11

object ScissorUtils {
    private val mc = Minecraft.getMinecraft()
    private var scissorBuffer = ArrayList<Triple<Vec2d, Vec2d, Double>>()

    fun toggleScissor(flag: Boolean) {
        if (flag) GL11.glEnable(GL11.GL_SCISSOR_TEST)
        else GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    fun scissor(pos1: Vec2d, pos2: Vec2d, scale: Double, block: () -> Unit) {
        GL11.glPushMatrix()
        scissorRect(pos1, pos2, scale)
        scissorBuffer.add(Triple(pos1, pos2, scale))

        block()

        scissorBuffer.removeLast()
        scissorBuffer.lastOrNull()?.let { scissorRect(it.first, it.second, it.third) }
        GL11.glPopMatrix()
    }

    private fun scissorRect(pos1: Vec2d, pos2: Vec2d, scale: Double) {
        val width = pos2.x - pos1.x
        val height = pos2.y - pos1.y
        GL11.glScissor(
            ((pos1.x * scale) - 0.5).floorToInt(),
            mc.displayHeight - (((pos1.y + height) * scale) - 0.5).floorToInt(),
            ((width * scale) + 1.0).ceilToInt(),
            ((height * scale) + 1.0).ceilToInt()
        )
    }

}