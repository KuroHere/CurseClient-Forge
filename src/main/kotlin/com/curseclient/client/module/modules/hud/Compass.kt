package com.curseclient.client.module.modules.hud

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.atan2


object Compass: DraggableHudModule(
    "DirectionRadar",
    "Draw direction on your HUD",
    HudCategory.HUD
) {

    override fun onRender() {
        super.onRender()
        val c1 =  HUD.getColor(0)

        // Background
        RenderUtils2D.drawGradientRect(Vec2d(pos.x.toInt(), pos.y.toInt()), Vec2d((pos.x + this.getWidth() / 2).toInt(), (pos.y + this.getHeight()).toInt()), Color(0,0,0,20), Color(0, 0, 0, 150), Color(0, 0, 0, 150), Color(0,0,0,20))
        RenderUtils2D.drawGradientRect(Vec2d(pos.x.toInt() + (this.getWidth() / 2).toInt(), pos.y.toInt()), Vec2d((pos.x + this.getWidth()).toInt(), (pos.y + this.getHeight()).toInt()), Color(0, 0, 0, 150), Color(0,0,0,20), Color(0,0,0,50), Color(0, 0, 0, 150))

        if (mc.world != null) {
            val sr = ScaledResolution(mc)
            //val host = if (mc.currentServerData != null) mc.currentServerData!!.serverIP else "localhost"
            val playerYaw = mc.player.rotationYaw
            val rotationYaw: Float = MathUtils.wrap(playerYaw)

            // Begin scissor area
            RenderUtils2D.glScissor(pos.x.toFloat(), pos.y.toFloat(), (pos.x + this.getWidth()).toFloat(), (pos.y + this.getHeight()).toFloat(), sr)
            RenderUtils2D.drawLine(pos.x.toFloat(), pos.y.toFloat(), ((pos.x + this.getWidth()).toFloat()), (pos.y.toFloat()), 0.4f, c1.rgb)

            GL11.glEnable(GL11.GL_SCISSOR_TEST)

            // 0, 0
            val zeroZeroYaw: Float = MathUtils.wrap((atan2(0 - mc.player.posZ, 0 - mc.player.posX) * 180.0 / Math.PI).toFloat() - 90.0f)
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 + zeroZeroYaw).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 + zeroZeroYaw).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, c1.rgb)

            // South west
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 + 45).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 + 45).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)
            // South eastf
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 - 45).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 - 45).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)
            // North west
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 + 135).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 + 135).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)
            // North east
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 - 135).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 - 135).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)

            // Text
            Fonts.DEFAULT_BOLD.drawString("N", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 + 180 - Fonts.DEFAULT_BOLD.getStringWidth("N") / 2.0f).toInt(), pos.y.toInt() + 5), color = c1)
            Fonts.DEFAULT_BOLD.drawString("N", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 - 180 - Fonts.DEFAULT_BOLD.getStringWidth("N") / 2.0f).toInt(), pos.y.toInt() + 5), color = c1)
            Fonts.DEFAULT_BOLD.drawString("E", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 - 90 - Fonts.DEFAULT_BOLD.getStringWidth("E") / 2.0f).toInt(), pos.y.toInt() + 5), color = c1)
            Fonts.DEFAULT_BOLD.drawString("S", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 - Fonts.DEFAULT_BOLD.getStringWidth("S") / 2.0f).toInt(), pos.y.toInt() + 5), color = c1)
            Fonts.DEFAULT_BOLD.drawString("W", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 + 90 - Fonts.DEFAULT_BOLD.getStringWidth("W") / 2.0f).toInt(), pos.y.toInt() + 5), color = c1)

            // Centered line
            //RenderUtils2D.drawLine((pos.x + this.getWidth() / 2).toFloat(), (pos.y + 1).toFloat(), (pos.x + this.getWidth() / 2).toFloat(), (pos.y + this.getHeight() - 1).toFloat(), 2f, -0x6f6f70)

            // Details ৻
            RenderUtils2D.drawTriangle((pos.x + this.getWidth() / 2).toFloat(), (pos.y + this.getHeight() / 4).toFloat(), 2.1f, 180f, ColorUtils.changeAlpha(c1.rgb, 0xFF))
            RenderUtils2D.drawTriangle((pos.x + this.getWidth() - 2).toFloat(), (pos.y + this.getHeight() / 2).toFloat(), 2f, 90f, ColorUtils.changeAlpha(Color(-0x555556).rgb, 0xFF))
            RenderUtils2D.drawTriangle((pos.x + 2).toFloat(), (pos.y + this.getHeight() / 2).toFloat(), 2f, -90f, ColorUtils.changeAlpha(Color(-0x555556).rgb, 0xFF))

            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }

    override fun getWidth() = 140.0
    override fun getHeight() = 10.0
}
