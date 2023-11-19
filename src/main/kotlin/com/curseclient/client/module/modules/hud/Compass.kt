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
        //RenderUtils2D.drawRect(pos.x.toFloat(), pos.y.toFloat(), (pos.x + this.getWidth()).toFloat(), (pos.y + this.getHeight()).toFloat(), 0x75101010)
        RenderUtils2D.drawGradientRect(Vec2d(pos.x.toInt(), pos.y.toInt()), Vec2d((pos.x + this.getWidth() / 2).toInt(), (pos.y + this.getHeight()).toInt()), Color(0,0,0,50), Color(0x75101010), Color(0x75101010), Color(0,0,0,50))
        RenderUtils2D.drawGradientRect(Vec2d(pos.x.toInt() + (this.getWidth() / 2).toInt(), pos.y.toInt()), Vec2d((pos.x + this.getWidth()).toInt(), (pos.y + this.getHeight()).toInt()), Color(0x75101010), Color(0,0,0,50), Color(0,0,0,50), Color(0x75101010))

        if (Helper.mc.world != null) {
            val sr = ScaledResolution(Helper.mc)
            //val host = if (Helper.mc.currentServerData != null) Helper.mc.currentServerData!!.serverIP else "localhost"
            val playerYaw = Helper.mc.player.rotationYaw
            val rotationYaw: Float = MathUtils.wrap(playerYaw)

            // Begin scissor area
            RenderUtils2D.glScissor(pos.x.toFloat(), pos.y.toFloat(), (pos.x + this.getWidth()).toFloat(), (pos.y + this.getHeight()).toFloat(), sr)
            RenderUtils2D.drawLine(pos.x.toFloat(), pos.y.toFloat( ), ((pos.x + this.getWidth()).toFloat()), (pos.y.toFloat()), 0.2f, c1.rgb)

            GL11.glEnable(GL11.GL_SCISSOR_TEST)

            // 0, 0
            val zeroZeroYaw: Float = MathUtils.wrap((atan2(0 - Helper.mc.player.posZ, 0 - Helper.mc.player.posX) * 180.0 / Math.PI).toFloat() - 90.0f)
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 + zeroZeroYaw).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 + zeroZeroYaw).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, c1.rgb) //-0xeff0)

            //// Waypoints
            //if (!WaypointDataList().isEmpty()) {
            //    for (waypointData in WaypointDataList()) {
            //        if (!waypointData.getHost().equals(host) || waypointData.getDimension() !== Helper.mc.player.dimension) continue
            //        val waypointDataYaw: Float = MathUtils.wrap((atan2(waypointData.getZ() - Helper.mc.player.posZ, waypointData.getX() - Helper.mc.player.posX) * 180.0 / Math.PI).toFloat() - 90.0f)
            //        RenderUtils2D.drawTriangle((pos.x - rotationYaw + this.getWidth() / 2 + waypointDataYaw).toFloat(), (pos.y + this.getHeight() / 2).toFloat(), 3f, 180f, ColorUtils.changeAlpha(waypointData.getColor(), 0xFF))
            //    }
            //}

            // North
            //RenderUtils2D.drawLine((pos.x - rotationYaw + (this.getWidth() / 2)) + 180, pos.y, (pos.x - rotationYaw + (this.getWidth() / 2)) + 180, pos.y + this.getHeight(), 2, 0xFFFFFFFF);
            //RenderUtils2D.drawLine((pos.x - rotationYaw + (this.getWidth() / 2)) - 180, pos.y, (pos.x - rotationYaw + (this.getWidth() / 2)) - 180, pos.y + this.getHeight(), 2, 0xFFFFFFFF);
            // East
            //RenderUtils2D.drawLine((pos.x - rotationYaw + (this.getWidth() / 2)) - 90, pos.y, (pos.x - rotationYaw + (this.getWidth() / 2)) - 90, pos.y + this.getHeight(), 2, 0xFFFFFFFF);
            // South
            //RenderUtils2D.drawLine((pos.x - rotationYaw + (this.getWidth() / 2)), pos.y, (pos.x - rotationYaw + (this.getWidth() / 2)), pos.y + this.getHeight(), 2, 0xFFFFFFFF);
            // West
            //RenderUtils2D.drawLine((pos.x - rotationYaw + (this.getWidth() / 2)) + 90, pos.y, (pos.x - rotationYaw + (this.getWidth() / 2)) + 90, pos.y + this.getHeight(), 2, 0xFFFFFFFF);

            // South west
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 + 45).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 + 45).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)
            // South eastf
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 - 45).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 - 45).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)
            // North west
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 + 135).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 + 135).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)
            // North east
            RenderUtils2D.drawLine((pos.x - rotationYaw + this.getWidth() / 2 - 135).toFloat(), (pos.y + 2).toFloat(), (pos.x - rotationYaw + this.getWidth() / 2 - 135).toFloat(), (pos.y + this.getHeight() - 2).toFloat(), 2f, -0x1)

            // Text
            Fonts.DEFAULT_BOLD.drawString("n", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 + 180 - Fonts.DEFAULT_BOLD.getStringWidth("n") / 2.0f).toInt(), pos.y.toInt() + 2), color = c1)
            Fonts.DEFAULT_BOLD.drawString("n", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 - 180 - Fonts.DEFAULT_BOLD.getStringWidth("n") / 2.0f).toInt(), pos.y.toInt() + 2), color = c1)
            Fonts.DEFAULT_BOLD.drawString("e", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 - 90 - Fonts.DEFAULT_BOLD.getStringWidth("e") / 2.0f).toInt(), pos.y.toInt() + 2), color = c1)
            Fonts.DEFAULT_BOLD.drawString("s", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 - Fonts.DEFAULT_BOLD.getStringWidth("s") / 2.0f).toInt(), pos.y.toInt() + 2), color = c1)
            Fonts.DEFAULT_BOLD.drawString("w", Vec2d((pos.x - rotationYaw + this.getWidth() / 2 + 90 - Fonts.DEFAULT_BOLD.getStringWidth("w") / 2.0f).toInt(), pos.y.toInt() + 2), color = c1)

            // Centered line
            RenderUtils2D.drawLine((pos.x + this.getWidth() / 2).toFloat(), (pos.y + 1).toFloat(), (pos.x + this.getWidth() / 2).toFloat(), (pos.y + this.getHeight() - 1).toFloat(), 2f, -0x6f6f70)

            // Details ৻
            RenderUtils2D.drawTriangle((pos.x + this.getWidth() / 2).toFloat(), (pos.y + this.getHeight() / 4).toFloat(), 2.1f, 180f, ColorUtils.changeAlpha(c1.rgb, 0xFF))
            RenderUtils2D.drawTriangle((pos.x + this.getWidth() - 2).toFloat(), (pos.y + this.getHeight() / 2).toFloat(), 2f, 90f, ColorUtils.changeAlpha(Color(-0x555556).rgb, 0xFF))
            RenderUtils2D.drawTriangle((pos.x + 2).toFloat(), (pos.y + this.getHeight() / 2).toFloat(), 2f, -90f, ColorUtils.changeAlpha(Color(-0x555556).rgb, 0xFF))

            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        } else {
            Fonts.DEFAULT.drawString("(compass)", Vec2d((pos.x + this.getWidth() / 2.0f - Fonts.DEFAULT.getStringWidth("(compass)") / 2.0f).toInt(), pos.y.toInt()), color = Color(-0x555556))
        }
    }

    override fun getWidth() = 120.0
    override fun getHeight() = 9.0
}
