package com.curseclient.client.module.modules.hud

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.timer
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.player.TargetingUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.ColorUtils.setAlphaD
import com.curseclient.client.utility.render.ColorUtils.setDarkness
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.RenderUtils2D.initStencilToWrite
import com.curseclient.client.utility.render.RenderUtils2D.readStencilBuffer
import com.curseclient.client.utility.render.RenderUtils2D.uninitStencilBuffer
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityWaterMob
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.abs


object Radar : DraggableHudModule(
    "Radar",
    "Draw sexy radar on your screen.",
    HudCategory.HUD
) {
    private val page by setting("Page", Page.General)

    // General
    private val size by setting("Size", 90, 75, 125, 1, { page == Page.General})

    private val rounded by setting("RoundedRadius", 6.0, 0.0, 10.0, 0.1, { page == Page.General})

    private val shadow by setting("Shadow", true, { page == Page.General})
    private val shadowSize by setting("ShadowRadius", 5, 0, 20, 1, { page == Page.General})

    private val outline by setting("Outline", true, { page == Page.General})
    private val outlineColor by setting("OutlineColor", Color.WHITE, { page == Page.General})
    private val thickness by setting("Thickness", 1.0, 0.0, 3.0, 0.1, { page == Page.General })

    private val alpha by setting("Alpha", 255, 0, 255, 1, { page == Page.General})
    private val darkness1 by setting("L-Darkness", 255, 0, 255, 1, { page == Page.General})
    private val darkness2 by setting("R-Darkness", 255, 0, 255, 1, { page == Page.General})

    // Target
    private val playerColor by setting("Player Color", Color.RED, { page == Page.Target})
    private val mobColor by setting("Mob Color", Color.ORANGE, { page == Page.Target})
    private val animalColor by setting("Animal Color", Color.BLUE, { page == Page.Target})
    private val itemColor by setting("Item Color", Color.YELLOW, { page == Page.Target})

    private val players by setting("Players", true, { page == Page.Target})
    private val hostileMobs by setting("Mobs", true, { page == Page.Target})
    private val animals by setting("Animals", true, { page == Page.Target})
    private val items by setting("Items", true, { page == Page.Target})

    private val entities = ArrayList<Entity>()

    enum class Page {
        General,
        Target
    }
    override fun getHeight() = size
    override fun getWidth() = size

    override fun onRender() {
        getEntities()
        val x = pos.x.toFloat()
        val y = pos.y.toFloat()
        val radarSize = size.toFloat()
        val middleX = x + radarSize / 2f
        val middleY = y + radarSize / 2f

        val c1 = HUD.getColor(0)
        val c2 = HUD.getColor(10)

        // Shadow
        if (shadow) RenderUtils2D.drawBlurredShadow(x - 1, y - 1, radarSize + 1, radarSize + 1, shadowSize.toInt(), c1.setDarkness(darkness1.toInt()))

        RoundedUtil.drawGradientRound(x, y, radarSize, radarSize, rounded.toFloat(), c1.setDarkness(darkness1.toInt()).setAlpha(alpha.toInt()), c1.setDarkness(darkness1.toInt()).setAlpha(alpha.toInt()), c2.setDarkness(darkness2.toInt()).setAlpha(alpha.toInt()), c2.setDarkness(darkness2.toInt()).setAlpha(alpha.toInt()))

        // Outline
        if (outline) RoundedUtil.drawRoundOutline(x - 1f, y - 1f, radarSize + 1.9f, radarSize + 1.9f, rounded.toFloat(), thickness.toFloat(), Color.BLACK.setAlpha(0), outlineColor)

        // Stencil
        initStencilToWrite()
        RectBuilder(Vec2d(x.toDouble(), y.toDouble()), Vec2d(pos.x + radarSize, pos.y + radarSize)).apply {
            color(Color.BLACK)
            radius(6.0)
            draw()
        }
        readStencilBuffer(1)

        // Point
        GlStateManager.pushMatrix();
        GlStateManager.translate(middleX.toDouble(), middleY.toDouble(), 0.0)
        GlStateManager.rotate(mc.player.rotationYaw, 0f, 0f, -1f)
        GlStateManager.translate(-middleX.toDouble(), -middleY.toDouble(), 0.0)

        for (entity in entities) {
            val xDiff = MathUtils.interpolate(entity.prevPosX, entity.posX, mc.timer.renderPartialTicks.toDouble()) - MathUtils.interpolate(mc.player.prevPosX, mc.player.posX, mc.timer.renderPartialTicks.toDouble())
            val zDiff = MathUtils.interpolate(entity.prevPosZ, entity.posZ, mc.timer.renderPartialTicks.toDouble()) - MathUtils.interpolate(mc.player.prevPosZ, mc.player.posZ, mc.timer.renderPartialTicks.toDouble())
            if ((abs(xDiff) + abs(zDiff)) < radarSize / 2f) {
                val translatedX = middleX - xDiff.toFloat()
                val translatedY = middleY - zDiff.toFloat()
                // Other entities point
                RoundedUtil.drawRound(translatedX, translatedY, 2f, 2f, 0.5f, getColor(entity))
                // Middle white point(Player)
                RoundedUtil.drawRound(middleX, middleY, 3f, 3f, 1f, Color.WHITE)
            }
        }

        GlStateManager.popMatrix();
        uninitStencilBuffer()

    }

    private fun getEntities() {
        entities.clear()
        for (entity in Helper.mc.world.loadedEntityList) {
            if (entity is EntityPlayer && players) {
                if (entity !== Helper.mc.player && !entity.isInvisible()) {
                    entities.add(entity)
                }
            }
            if ((entity is EntityMob || entity is EntityWaterMob) && hostileMobs) {
                entities.add(entity)
            }
            if (entity is EntityAnimal && animals) {
                entities.add(entity)
            }
            if (entity is EntityItem && items) {
                entities.add(entity)
            }
        }
    }

    private fun getColor(entity: Entity): Color {
        var color = Color.WHITE

        if (entity is EntityPlayer) {
            color = playerColor
        }
        if (entity is EntityMob || entity is EntityWaterMob) {
            color = mobColor
        }

        if (entity is EntityAnimal) {
            color = animalColor
        }

        if (entity is EntityItem) {
            color = itemColor
        }

        return color
    }

}
