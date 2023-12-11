package com.curseclient.client.module.modules.visual

import baritone.api.utils.Helper
import com.curseclient.CurseClient
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
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.shader.Framebuffer
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
    private val seeThroughWalls by setting("ThroughWalls", false)
    private val top by setting("Top", false)
    private val players by setting("Players", true)
    private val items by setting("Items", true)
    private val hostiles by setting("Hostiles", false)
    private val animals by setting("Animals", false)

    private var renderNameTags = true
    private val entities = ArrayList<Entity>()
    private val frustum2 = Frustum()

    var framebuffer: Framebuffer? = null

    init {
        safeListener<Render3DEvent> { event ->
            GL11.glPushMatrix()
            GL11.glDisable(3553)
            GL11.glEnable(2848)
            GL11.glEnable(2832)
            GL11.glEnable(3042)
            GL11.glBlendFunc(770, 771)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glHint(3153, 4354)
            if (seeThroughWalls) {
                GL11.glDepthMask(false)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
            }
            GL11.glFrontFace(GL11.GL_CW)
            collectEntities()
            framebuffer?.let {
                it.framebufferClear()
                it.bindFramebuffer(true)
                renderEntities(event.partialTicks)
                it.unbindFramebuffer()
            }
            GL11.glShadeModel(GL11.GL_FLAT)
            GL11.glFrontFace(GL11.GL_CCW)
            if (seeThroughWalls) {
                GL11.glDepthMask(true)
            }
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

    private fun renderEntities(ticks: Float) {
        entities.forEach { entity ->
            if (entity != null) {
                try {
                    renderNameTags = false
                    mc.renderManager.renderEntityStatic(entity, ticks, false)
                    renderNameTags = true
                } catch (e: Exception) {
                    CurseClient.LOG.debug("Crash rồi nhớ gửi crash log cho Kuro_Here nhé")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isInView(ent: Entity): Boolean {
        frustum2.setPosition(
            mc.renderViewEntity!!.posX,
            mc.renderViewEntity!!.posY,
            mc.renderViewEntity!!.posZ)
        return frustum2.isBoundingBoxInFrustum(ent.entityBoundingBox) || ent.ignoreFrustumCheck
    }

    private fun collectEntities() {
        entities.clear()
        var i = 0
        for (entity in mc.world.loadedEntityList) {
            if (!isInView(entity)) continue
            if (entity == mc.player && mc.gameSettings.thirdPersonView == 0) continue
            if (entity is EntityItem && items) {
                entities.add(entity)
                onRender(i++.toFloat(), entity)
            }
            if (entity is EntityAnimal && animals) {
                entities.add(entity)
                onRender(i++.toFloat(), entity)
            }
            if (entity is EntityPlayer && players) {
                entities.add(entity)
                onRender(i++.toFloat(), entity)
            }
            if (entity is EntityMob && hostiles) {
                entities.add(entity)
                onRender(i++.toFloat(), entity)
            }

        }
    }

    fun onRender(i: Float, entity: Entity) {
        var color = Color(color.r, color.g, color.b)
        if (entity.hurtResistantTime > 0) color = Color.RED

        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * Helper.mc.timer.renderPartialTicks - Helper.mc.renderManager.viewerPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * Helper.mc.timer.renderPartialTicks - Helper.mc.renderManager.viewerPosY + entity.getEyeHeight() + .4 + sin((System.currentTimeMillis() % 1000000 / 333f + i).toDouble()) / 10
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * Helper.mc.timer.renderPartialTicks - Helper.mc.renderManager.viewerPosZ

        val topColor = color
        val bottomColor = color

        renderCone(x, y + 0.3, z, topColor)
        if (top)
            renderInvertedCone(x, y + 0.9, z, bottomColor)
    }

    private fun renderCone(x: Double, y: Double, z: Double, color: Color) {
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        color(color.rgb)
        GL11.glVertex3d(x, y, z)
        GL11.glVertex3d(x - 0.1, y + 0.3, z - 0.1)
        GL11.glVertex3d(x - 0.1, y + 0.3, z + 0.1)
        color(color.rgb)
        GL11.glVertex3d(x + 0.1, y + 0.3, z)
        color(color.rgb)
        GL11.glVertex3d(x, y, z)
        color(color.rgb)
        GL11.glVertex3d(x + 0.1, y + 0.3, z)
        GL11.glVertex3d(x - 0.1, y + 0.3, z - 0.1)
        GL11.glEnd()
    }

    private fun renderInvertedCone(x: Double, y: Double, z: Double, color: Color) {
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        color(color.rgb)
        GL11.glVertex3d(x, y, z)
        GL11.glVertex3d(x - 0.1, y - 0.3, z - 0.1)
        GL11.glVertex3d(x - 0.1, y - 0.3, z + 0.1)
        color(color.rgb)
        GL11.glVertex3d(x + 0.1, y - 0.3, z)
        color(color.rgb)
        GL11.glVertex3d(x, y, z)
        color(color.rgb)
        GL11.glVertex3d(x + 0.1, y - 0.3, z)
        GL11.glVertex3d(x - 0.1, y - 0.3, z - 0.1)
        GL11.glEnd()
    }

}