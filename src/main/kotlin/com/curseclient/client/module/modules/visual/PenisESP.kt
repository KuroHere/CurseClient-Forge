package com.curseclient.client.module.modules.visual

import baritone.api.utils.Helper
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.player.FreeCam
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.mixin.accessor.AccessorRenderManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import org.lwjgl.util.glu.Sphere
import java.awt.Color


object PenisESP: Module(
    "PenisESP",
    "Draw a penis",
    Category.VISUAL
) {
    private val shaft = Cylinder()
    private val ball = Sphere()
    private val tip = Sphere()

    private val selfLength by setting("SelfLength", 0.8, 0.1, 50.0, 0.1)
    private val enemyLength by setting("EnemyLength", 0.4, 0.1, 2.0, 0.1)
    private val uncircumcised by setting("Uncircumcised", false)
    private val selfShaftColor by setting("SelfShaftColor", Color(95, 67, 63, 255))
    private val selfTipColor by setting("SelfTipColor", Color(160, 99, 98, 255))
    private val enemyShaftColor by setting("EnemyShaftColor", Color(95, 67, 63, 255))
    private val enemyTipColor by setting("EnemyTipColor", Color(160, 99, 98, 255))


    init {
        safeListener<Render3DEvent> {
            var id = -133700
            var freecamId = -1

            if (FreeCam.isEnabled()) {
                id = mc.player.entityId
                freecamId = FreeCam.mc.player.entityId
            }

            //for (player in Helper.mc.world.playerEntities) {
            //    if (id != player.entityId) {
            //        val interpolateEntity: Vec3d = Interpolation.interpolateEntity(player)
            //        drawPenis(player, interpolateEntity.x, interpolateEntity.y, interpolateEntity.z, freecamId != -1)
            //    }
            //}
            for (o in mc.world.loadedEntityList) {
                if (o is EntityPlayer) {
                    val player: EntityPlayer = o
                    val n = player.lastTickPosX + (player.posX - player.lastTickPosX) * Helper.mc.timer.renderPartialTicks
                    Helper.mc.renderManager
                    val x: Double = n - (ESP.mc.renderManager  as AccessorRenderManager).renderPosX
                    val n2 = player.lastTickPosY + (player.posY - player.lastTickPosY) * Helper.mc.timer.renderPartialTicks
                    Helper.mc.renderManager
                    val y: Double = n2 - (ESP.mc.renderManager  as AccessorRenderManager).renderPosY
                    val n3 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * Helper.mc.timer.renderPartialTicks
                    Helper.mc.renderManager
                    val z: Double = n3 - (ESP.mc.renderManager  as AccessorRenderManager).renderPosZ

                    drawPenis(player, x, y, z, (freecamId != -1))
                }
            }
        }
    }

    private fun drawPenis(player: EntityPlayer, x: Double, y: Double, z: Double, forceSelf: Boolean) {
        val length = if (player == mc.player || forceSelf) selfLength else enemyLength
        val shaftColor = if (player == mc.player || forceSelf) selfShaftColor else enemyShaftColor
        val tipColor = if (player == mc.player || forceSelf) selfTipColor else enemyTipColor
        shaft.drawStyle = GLU.GLU_FILL
        ball.drawStyle = GLU.GLU_FILL
        tip.drawStyle = GLU.GLU_FILL
        GL11.glPushMatrix()
        RenderHelper.disableStandardItemLighting()
        GL11.glDisable(2896)
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glDisable(2929)
        GL11.glEnable(2848)
        GL11.glDepthMask(false)
        GL11.glTranslated(x, y, z)
        GL11.glRotatef(-mc.player.rotationYaw, 0.0f, mc.player.height, 0.0f)
        GL11.glTranslated(-x, -y, -z)
        GL11.glTranslated(x, y + mc.player.height / 2.0f - 0.22499999403953552, z)
        GL11.glColor4f(shaftColor.red / 255f, shaftColor.green / 255f, shaftColor.blue / 255f, 1.0f)
        GL11.glTranslated(0.0, 0.0, 0.07500000298023224)
        shaft.draw(0.1f, 0.11f, length.toFloat(), 25, 20)
        GL11.glColor4f(shaftColor.red / 255f, shaftColor.green / 255f, shaftColor.blue / 255f, 1.0f)
        GL11.glTranslated(0.0, 0.0, 0.02500000298023223)
        GL11.glTranslated(-0.09000000074505805, 0.0, 0.0)
        ball.draw(0.14f, 10, 20)
        GL11.glTranslated(0.16000000149011612, 0.0, 0.0)
        ball.draw(0.14f, 10, 20)
        GL11.glTranslated(-0.07000000074505806, 0.0, if (uncircumcised) length - 0.15 else length - 0)
        GL11.glColor4f(tipColor.red / 255f, tipColor.green / 255f, tipColor.blue / 255f, 1.0f)
        tip.draw(0.13f, 15, 20)
        GL11.glDepthMask(true)
        GL11.glDisable(2848)
        GL11.glEnable(2929)
        GL11.glDisable(3042)
        GL11.glEnable(2896)
        GL11.glEnable(3553)
        RenderHelper.enableStandardItemLighting()
        GL11.glPopMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }
}