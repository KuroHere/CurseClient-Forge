package com.curseclient.client.module.modules.hud.graph

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.Timer
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.BonIcon
import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat


object FpsGraph: DraggableHudModule(
    "FpsGraph",
    "Draw FPS graph on your HUD",
    HudCategory.HUD,
) {

    //The amount of delay(ms) between updates.
    val delay by setting("Delay", 500.0f, 0.0f, 2500.0f, 100.0f)
    val cWidth by setting("Width", 80.0, 60.0, 120.0, 5.0)
    val cHeight by setting("Height", 30.0, 10.0, 50.0, 5.0)
    private val fpsNodes: ArrayList<FpsNode> = ArrayList<FpsNode>()
    private val timer: Timer = Timer()

    override fun onRender() {
        super.onRender()
        val c1 = HUD.getColor(0)
        val c2 = HUD.getColor(5)
        if (Helper.mc.player != null && Helper.mc.world != null) {
            val sr = ScaledResolution(Helper.mc)
            val decimalFormat = DecimalFormat("###.##")
            if (fpsNodes.size > this.getWidth() / 2) { // overflow protection
                fpsNodes.clear()
            }
            if (timer.passed(delay)) {
                if (fpsNodes.size > this.getWidth() / 2 - 1) {
                    fpsNodes.removeAt(0) // remove oldest
                }
                val fps = Minecraft.getDebugFPS().toFloat()
                fpsNodes.add(FpsNode(fps))
                timer.reset()
            }


            // background
            val pos1 = Vec2d(pos.x, pos.y)
            val pos2 = Vec2d(pos.x + this.getWidth(), pos.y + this.getHeight())

            // shadow
            RenderUtils2D.drawBlurredRect(Vec2d(pos.x - 4, pos.y - 10), pos2.plus(4.0, 4.0), 10, c1.darker().darker())

            // border

            //RectBuilder(Vec2d(pos.x - 4, pos.y - 7), pos2.plus(4.0, 4.0)).outlineColor(c2.brighter(), c2.brighter(), c1.brighter(), c1.brighter()).width(0.5).radius(3.5).draw()
            RectBuilder(pos1.minus(4.0, 10.0), pos2.plus(4.0, 4.0)).color(c1, c2, c1, c2).radius(3.5).draw()
            RectBuilder(pos1, pos2).color(c1.darker().darker(), c2.darker().darker(), c2.darker(), c1.darker()).radius(3.2).draw()

            FontRenderer.drawString("Fps Graph", pos.x.toFloat() + 1, pos.y.toFloat() - 10, true, Color.WHITE, 0.9f, Fonts.DEFAULT_BOLD)
            FontRenderer.drawString(BonIcon.BOLT, pos.x.toFloat() + 1 + FontRenderer.getStringWidth("Fps Graph", Fonts.DEFAULT_BOLD, 0.9f), pos.y.toFloat() - 8, true, Color.WHITE, 0.9f, Fonts.BonIcon20)
            // create temporary hovered data string
            var hoveredData = ""

            // begin scissoring
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            RenderUtils2D.glScissor(pos.x.toFloat(), pos.y.toFloat(), (pos.x + this.getWidth()).toFloat(), (pos.y + this.getHeight()).toFloat(), sr)

            // movement bars
            var lastNode = FpsNode()
            for (i in fpsNodes.indices) {
                val fpsNode: FpsNode = fpsNodes[i]

                val mappedX = MathUtils.map(this.getWidth() / 2 - 1 - i, 0.0, this.getWidth() / 2 - 1, pos.x + this.getWidth() - 1, pos.x + 1).toFloat()
                val mappedY: Float = (MathUtils.map(fpsNode.speed.toDouble(), -2.0, this.getAverageHeight().toDouble(), pos.y + this.getHeight() - 1, pos.y + 1).toFloat() + this.getHeight() / 2).toFloat()

                // set node's mapped coordinates
                fpsNode.mappedX = mappedX
                fpsNode.mappedY = mappedY

                // rect on top of bar
                RenderUtils2D.drawLine(fpsNode.mappedX, fpsNode.mappedY, lastNode.mappedX, lastNode.mappedY, 1.0f, -1)

                // draw graph line
                RenderUtils2D.drawBlurredRect(Vec2d((fpsNode.mappedX - fpsNode.size).toDouble(), (fpsNode.mappedY - fpsNode.size).toDouble()), Vec2d((fpsNode.mappedX + fpsNode.size).toDouble(), (fpsNode.mappedY + fpsNode.size).toDouble()),3, Color(fpsNode.color.red, fpsNode.color.green, fpsNode.color.blue))

                // draw text
                if (i == fpsNodes.size - 1) {
                    val textToDraw: String = decimalFormat.format(fpsNode.speed) + "fps"
                    FontRenderer.drawString(textToDraw, fpsNode.mappedX - FontRenderer.getStringWidth(textToDraw, Fonts.DEFAULT, 1f), fpsNode.mappedY + 3, true, Color(-0x555556), 1f, Fonts.DEFAULT)
                }

                // draw hover
                if (dragX >= fpsNode.mappedX && dragX <= fpsNode.mappedX + fpsNode.size && dragY >= pos.y && dragY <= pos.y + this.getHeight()) {
                    // hover bar
                    RenderUtils2D.drawRect(fpsNode.mappedX - fpsNode.size, pos.y.toFloat(), fpsNode.mappedX + fpsNode.size, pos.y.toFloat() + this.getHeight().toFloat(), 0x40101010)
                    // hover red dot
                    RenderUtils2D.drawRect(fpsNode.mappedX - fpsNode.size, fpsNode.mappedY - fpsNode.size, fpsNode.mappedX + fpsNode.size, fpsNode.mappedY + fpsNode.size, -0x10000)

                    // set hovered data
                    hoveredData = java.lang.String.format("FPS: %s", decimalFormat.format(fpsNode.speed))
                }
                lastNode = fpsNode
            }

            // draw hovered data
            if (hoveredData != "") {
                FontRenderer.drawString(hoveredData, (pos.x + 2).toFloat(), (pos.y + this.getHeight() - FontRenderer.getFontHeight(Fonts.DEFAULT, 1f) * 2 - 1).toFloat(), true, Color(-0x555556), 1f, Fonts.DEFAULT)
            }

            // disable scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST)

        } else {
            FontRenderer.drawString("(fps graph)", pos.x.toFloat(), pos.y.toFloat(), true, Color(-0x555556), 1f, Fonts.DEFAULT)
        }

    }

    private fun getAverageHeight(): Float {
        var totalSpeed = 0f
        for (i in fpsNodes.size - 1 downTo 1) {
            val fpsNode = fpsNodes[i]
            if (fpsNodes.size > 11) {
                if (i > fpsNodes.size - 10) {
                    totalSpeed += fpsNode.speed
                }
            }
        }
        return totalSpeed / 10
    }

    internal class FpsNode {
        var size = 0.5f
        var speed = 0.0f
        var color: Color = Color(255, 255, 255)
        var mappedX = 0f
        var mappedY = 0f

        constructor(speed: Float) {
            this.speed = speed
            color
        }

        constructor()
    }

    override fun getWidth() = this.cWidth + FontRenderer.getStringWidth("Bps Graph", Fonts.DEFAULT_BOLD)
    override fun getHeight() = this.cHeight
}