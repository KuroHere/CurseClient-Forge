package com.curseclient.client.module.modules.visual

import baritone.api.utils.Helper
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.shader.RoundedUtil.color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.ArrayList
import kotlin.math.sin


object SimsESP: Module(
    "SimsESP",
    "ESP designed like the game Sims",
    Category.VISUAL
) {

    private val color by setting("Color", Color(0, 255, 0))
    private val players by setting("Players", true)
    private val items by setting("Items", true)
    private val hostiles by setting("Hostiles", false)
    private val animals by setting("Animals", false)

    private val entities = ArrayList<Entity>()

    init {
        safeListener<Render3DEvent> {
            GL11.glPushMatrix()
            GL11.glDisable(3553)
            GL11.glEnable(2848)
            GL11.glEnable(2832)
            GL11.glEnable(3042)
            GL11.glBlendFunc(770, 771)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glHint(3153, 4354)
            GL11.glDepthMask(false)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glFrontFace(GL11.GL_CW)
            var i = 0
            //        GlStateManager.disableCull();
            for (entity in mc.world.loadedEntityList) {
                entities.clear()
                if (entity.isInvisible) continue
                if (entity == mc.player && mc.gameSettings.thirdPersonView == 0) continue
                if (entity is EntityItem && items) {
                    entities.add(entity)
                }
                if (entity is EntityAnimal && animals) {
                    entities.add(entity)
                }
                if (entity is EntityPlayer && players) {
                    entities.add(entity)
                }
                if (entity is EntityMob && hostiles) {
                    entities.add(entity)
                }
                i++
                var color = Color(color.r, color.g, color.b)
                if (entity.hurtResistantTime > 0) color = Color.RED
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
                color(color.rgb)
                val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * Helper.mc.timer.renderPartialTicks - Helper.mc.renderManager.viewerPosX
                val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * Helper.mc.timer.renderPartialTicks - Helper.mc.renderManager.viewerPosY + entity.getEyeHeight() + .4 + sin((System.currentTimeMillis() % 1000000 / 333f + i).toDouble()) / 10
                val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * Helper.mc.timer.renderPartialTicks - Helper.mc.renderManager.viewerPosZ
                color(color.darker().darker().rgb)
                GL11.glVertex3d(x, y, z)
                GL11.glVertex3d(x - 0.1, y + 0.3, z - 0.1)
                GL11.glVertex3d(x - 0.1, y + 0.3, z + 0.1)
                color(color.rgb)
                GL11.glVertex3d(x + 0.1, y + 0.3, z)
                color(color.darker().darker().rgb)
                GL11.glVertex3d(x, y, z)
                color(color.darker().darker().darker().rgb)
                GL11.glVertex3d(x + 0.1, y + 0.3, z)
                GL11.glVertex3d(x - 0.1, y + 0.3, z - 0.1)
                GL11.glEnd()
            }
            GL11.glShadeModel(GL11.GL_FLAT)
            GL11.glFrontFace(GL11.GL_CCW)
            GL11.glDepthMask(true)
            GL11.glEnable(2929)
            GL11.glCullFace(GL11.GL_BACK)
            GlStateManager.enableCull()
            GL11.glDisable(2848)
            GL11.glEnable(2832)
            GL11.glEnable(3553)
            GL11.glPopMatrix()
            GL11.glColor3f(255f, 255f, 255f)
        }
    }
}