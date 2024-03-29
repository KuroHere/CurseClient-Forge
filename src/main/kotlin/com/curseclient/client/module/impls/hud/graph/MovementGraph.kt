package com.curseclient.client.module.impls.hud.graph

import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.impls.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.Timer
import com.curseclient.client.utility.extension.mixins.tickLength
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.impl.BonIcon
import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat


object MovementGraph: DraggableHudModule(
    "MovementGraph",
    "Draw movement graph on your HUD",
    HudCategory.HUD,
) {

    //The amount of delay(ms) between updates.
    val delay by setting("Delay", 20.0f, 0.0f, 90.0f, 10.0f)
    val cWidth by setting("Width", 80.0, 60.0, 120.0, 5.0)
    val cHeight by setting("Height", 30.0, 10.0, 50.0, 5.0)
    private val movementNodes: ArrayList<MovementNode> = ArrayList<MovementNode>()
    private val timer: Timer = Timer()

    override fun onRender() {
        super.onRender()
        val c1 =  HUD.getColor(0)
        val c2 =  HUD.getColor(5)
        if (mc.player != null && mc.world != null) {
            val sr = ScaledResolution(mc)
            val decimalFormat = DecimalFormat("###.##")
            if (movementNodes.size > this.getWidth() / 2) { // overflow protection
                movementNodes.clear()
            }
            if (timer.passed(delay)) {
                if (movementNodes.size > this.getWidth() / 2 - 1) {
                    movementNodes.removeAt(0) // remove oldest
                }
                val deltaX = mc.player.posX - mc.player.prevPosX
                val deltaZ = mc.player.posZ - mc.player.prevPosZ
                val tickRate = mc.timer.tickLength / 1000.0f
                val bps = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate
                movementNodes.add(MovementNode(bps))
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

            FontRenderer.drawString("Bps Graph", pos.x.toFloat() + 1, pos.y.toFloat() - 10, true, Color.WHITE, 0.9f, Fonts.DEFAULT_BOLD)
            FontRenderer.drawString(BonIcon.RUN, pos.x.toFloat() + 1 + FontRenderer.getStringWidth("Fps Graph", Fonts.DEFAULT_BOLD, 0.9f), pos.y.toFloat() - 8, true, Color.WHITE, 0.9f, Fonts.BonIcon20)
            // create temporary hovered data string
            var hoveredData = ""

            // begin scissoring
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            RenderUtils2D.glScissor(pos.x.toFloat(), pos.y.toFloat(), (pos.x + this.getWidth()).toFloat(), (pos.y + this.getHeight()).toFloat(), sr)

            // movement bars
            var lastNode: MovementNode? = null
            for (i in movementNodes.indices) {
                val movementNode: MovementNode = movementNodes[i]
                val mappedX = MathUtils.map(this.getWidth() / 2 - 1 - i, 0.0, this.getWidth() / 2 - 1, pos.x + this.getWidth() - 1, pos.x + 1).toFloat()
                val mappedY: Float = (MathUtils.map(movementNode.speed.toDouble(), -2.0, getAverageHeight().toDouble(), pos.y + this.getHeight() - 1, pos.y + 1) + this.getHeight() / 2).toFloat()

                // set node's mapped coordinates
                movementNode.mappedX = mappedX
                movementNode.mappedY = mappedY

                // rect on top of bar
                if (lastNode != null)
                    RenderUtils2D.drawLine(movementNode.mappedX, movementNode.mappedY, lastNode.mappedX, lastNode.mappedY, 1.0f, -1)

                // draw graph line
                RenderUtils2D.drawBlurredRect(Vec2d((movementNode.mappedX - movementNode.size).toDouble(), (movementNode.mappedY - movementNode.size).toDouble()), Vec2d((movementNode.mappedX + movementNode.size).toDouble(), (movementNode.mappedY + movementNode.size).toDouble()),3, Color(movementNode.color.red, movementNode.color.green, movementNode.color.blue))

                // draw text
                if (i == movementNodes.size - 1) {
                    val textToDraw: String = decimalFormat.format(movementNode.speed) + "bps"
                    GL11.glPushMatrix()
                    FontRenderer.drawString(textToDraw, movementNode.mappedX - FontRenderer.getStringWidth(textToDraw, Fonts.DEFAULT), movementNode.mappedY + 3, true, Color(-0x555556), 1f, Fonts.DEFAULT)
                    GL11.glPopMatrix()
                }

                // draw hover
                if (dragX >= movementNode.mappedX && dragX <= movementNode.mappedX + movementNode.size && dragY >= pos.y && dragY <= pos.y + this.getHeight()) {
                    // hover bar
                    RenderUtils2D.drawRect(movementNode.mappedX - movementNode.size, pos.y.toFloat(), movementNode.mappedX + movementNode.size, (pos.y + this.getHeight()).toFloat(), 0x40101010)
                    // hover red dot
                    RenderUtils2D.drawRect(movementNode.mappedX - movementNode.size, movementNode.mappedY - movementNode.size, movementNode.mappedX + movementNode.size, movementNode.mappedY + movementNode.size, -0x10000)

                    // set hovered data
                    hoveredData = java.lang.String.format("Speed: %s", decimalFormat.format(movementNode.speed))
                }
                lastNode = movementNode
            }
            // draw hovered data
            if (hoveredData != "") {
                FontRenderer.drawString(hoveredData, (pos.x + 2).toFloat(), (pos.y + this.getHeight() - FontRenderer.getFontHeight(Fonts.DEFAULT) * 2 - 1).toFloat(), true, Color(-0x555556), 1f, Fonts.DEFAULT)
            }

            // disable scissor
            GL11.glDisable(GL11.GL_SCISSOR_TEST)

        } else {
            FontRenderer.drawString("(movement)", pos.x.toFloat(), pos.y.toFloat(), true, Color(-0x555556), 1f, Fonts.DEFAULT)
        }

    }

    private fun getAverageHeight(): Float {
        var totalSpeed = 0f
        for (i in movementNodes.size - 1 downTo 1) {
            val movementNode = movementNodes[i]
            if (movementNodes.size > 11) {
                if (i > movementNodes.size - 10) {
                    totalSpeed += movementNode.speed
                }
            }
        }
        return totalSpeed / 10
    }

    internal class MovementNode(var speed: Float) {
        var size = 0.5f
        var color: Color = Color(255, 255, 255)
        var mappedX = 0f
        var mappedY = 0f

        init {
            color
        }

    }

    override fun getWidth() = this.cWidth + FontRenderer.getStringWidth("Bps Graph", Fonts.DEFAULT_BOLD)
    override fun getHeight() = this.cHeight

}