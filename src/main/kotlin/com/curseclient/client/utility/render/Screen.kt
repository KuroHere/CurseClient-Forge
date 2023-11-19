package com.curseclient.client.utility.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

object Screen {
    val width: Double get() = Minecraft.getMinecraft().displayWidth.toDouble()
    val height: Double get() = Minecraft.getMinecraft().displayHeight.toDouble()

    val scaledWidth: Double get() = width / currentScale
    val scaledHeight: Double get() = height / currentScale

    private var currentScale = 1.0

    fun pushRescale(scaleFactor: Double) {
        currentScale = scaleFactor

        rescale(width / currentScale, height / currentScale)
    }

    fun pushRescale() = pushRescale(2.0)

    fun popRescale() {
        val resolution = ScaledResolution(Minecraft.getMinecraft())
        rescale(resolution.scaledWidth_double, resolution.scaledHeight_double)

        currentScale = 1.0
    }

    private fun rescale(widthIn: Double, heightIn: Double) {
        GlStateManager.clear(256)
        GlStateManager.viewport(0, 0, width.toInt(), height.toInt())
        GlStateManager.matrixMode(GL11.GL_PROJECTION)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, widthIn, heightIn, 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0.0f, 0.0f, -2000.0f)
    }
}