package com.curseclient.client.utility.render

import com.curseclient.client.module.modules.client.GuiClickCircle
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.animation.EaseUtils
import com.curseclient.client.utility.render.animation.Easing
import com.curseclient.client.utility.render.animation.SimpleAnimation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin


class ClickCircle(var x: Float, var y: Float, private var seconds: Int, var radius: Int, private val easing: String) {
    var time: Long = System.currentTimeMillis()
    private val animation = SimpleAnimation(0.0f)

    fun draw(color: Int) {
        val value = MathHelper.clamp((System.currentTimeMillis() - time).toFloat() / (seconds * 1000f), 0f, 1f)
        if (GuiClickCircle.mode == GuiClickCircle.Mode.Fill) {
            val animationMode: Double = Easing.toOutEasing(easing, value.toDouble())
            drawCircle(x, y, (radius * animationMode.toFloat()), Color(color).setAlpha((255 * (1 - EaseUtils.easeInOutBack(value.toDouble()).toInt()))).rgb)
        }
        else {
            animation.setAnimation(100f, 12.0)
            val radius: Double = (radius * animation.value / 100).toDouble()
            val alpha = (255 - 255 * animation.value / 100).toInt()
            val arc: Double = (360 * animation.value / 100).toDouble()
            val color: Int = Color(color).setAlpha((alpha)).rgb

            if (GuiClickCircle.isEnabled()) {
                drawArc(x, y, radius, color, 0, arc, 2)
            }
        }
    }

    fun canRemove(): Boolean {
        return animation.value > 99
    }

    private fun drawArc(x1: Float, y1: Float, r: Double, color: Int, startPoint: Int, arc: Double, linewidth: Int) {
        var x1 = x1
        var y1 = y1
        var r = r
        r *= 2.0
        x1 *= 2f
        y1 *= 2f
        val f = (color shr 24 and 0xFF) / 255.0f
        val f1 = (color shr 16 and 0xFF) / 255.0f
        val f2 = (color shr 8 and 0xFF) / 255.0f
        val f3 = (color and 0xFF) / 255.0f
        GL11.glDisable(2929)
        GL11.glPushMatrix()
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glDepthMask(true)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glHint(3155, 4354)
        GL11.glScalef(0.5f, 0.5f, 0.5f)
        GL11.glLineWidth(linewidth.toFloat())
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(f1, f2, f3, f)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        var i = startPoint
        while (i <= arc) {
            val x = sin(i * Math.PI / 180.0) * r
            val y = cos(i * Math.PI / 180.0) * r
            GL11.glVertex2d(x1 + x, y1 + y)
            i += 1
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glScalef(2.0f, 2.0f, 2.0f)
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glPopMatrix()
        GL11.glEnable(2929)
        GL11.glDisable(2848)
        GL11.glHint(3154, 4352)
        GL11.glHint(3155, 4352)
    }

    private fun drawCircle(centerX: Float, centerY: Float, radius: Float, color: Int) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.color((color shr 16 and 0xFF) / 255.0f, (color shr 8 and 0xFF) / 255.0f, (color and 0xFF) / 255.0f, (color shr 24 and 0xFF) / 255.0f)
        GL11.glBegin(GL11.GL_POLYGON)
        for (i in 0..360) GL11.glVertex2d((centerX + MathHelper.sin(i * Math.PI.toFloat() / 180f) * radius).toDouble(), (centerY + MathHelper.cos(i * Math.PI.toFloat() / 180f) * radius).toDouble())
        GL11.glEnd()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(.5f)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        for (i in 0..360) GL11.glVertex2d((centerX + MathHelper.sin(i * Math.PI.toFloat() / 180f) * radius).toDouble(), (centerY + MathHelper.cos(i * Math.PI.toFloat() / 180f) * radius).toDouble())
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    val isRemovable: Boolean
        get() = System.currentTimeMillis() > time + seconds * 1000L
}
