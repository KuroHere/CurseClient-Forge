package com.curseclient.client.utility.render.graphic

import com.curseclient.client.utility.math.FPSCounter
import com.curseclient.client.utility.render.RenderTessellator.prepareGL
import com.curseclient.client.utility.render.RenderTessellator.releaseGL
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object GLUtils {
    fun deltaTimeDouble() =
        FPSCounter.deltaTime

    fun deltaTimeFloat() =
        FPSCounter.deltaTime.toFloat()

    fun matrix(block: () -> Unit) {
        glPushMatrix()
        block()
        glPopMatrix()
    }

    fun renderGL(block: () -> Unit) {
        prepareGL()
        block()
        releaseGL()
    }

    fun draw(mode: Int, block: () -> Unit) {
        glBegin(mode)
        block()
        glEnd()
    }

    fun withScale(scale: Double, block: () -> Unit) {
        glScaled(scale, scale, scale)
        block()
        glScaled(1.0 / scale, 1.0 / scale, 1.0 / scale)
    }

    fun prepareGL2D() {
        GlStateManager.disableAlpha()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.shadeModel(GL_SMOOTH)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.disableCull()
    }

    fun releaseGL2D() {
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.shadeModel(GL_FLAT)
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.enableCull()
    }

    fun glColor(color: Color) {
        val red: Float = color.red / 255f
        val green: Float = color.green / 255f
        val blue: Float = color.blue / 255f
        GlStateManager.color(red, green, blue, color.alpha / 255f)
    }

    fun startTranslate(x: Float, y: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
    }

    fun stopTranslate() {
        GlStateManager.popMatrix()
    }

    fun Vec3d.translateGL(): Vec3d {
        glTranslated(this.x, this.y, this.z)
        return this
    }

    fun Vec3d.glVertex(): Vec3d {
        glVertex3d(this.x, this.y, this.z)
        return this
    }
}