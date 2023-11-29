package com.curseclient.client.module.modules.hud

import baritone.api.utils.Helper
import com.curseclient.CurseClient
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.module.modules.hud.TargetHUD.TargetHUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.player.ChatUtils
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.animation.SimpleAnimation
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.jhlabs.image.ImageMath.clamp
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.math.max
import kotlin.math.min


object NewOverlay: DraggableHudModule(
    "NewOverlay",
    "New way to render hp, armor, hunger bar(Not done yet)",
    HudCategory.HUD
) {
    private val scale by setting("Scale", 1.5, 1.5, 2.5, 0.1)

    private var maxHealth = 20.0f
    private var healthProgress = 0.0
    private var previousHealthProgress = 0.0

    private const val w = 160.0
    private const val h = 80.0
    enum class ArmorType(val slot: EntityEquipmentSlot?, val xOffset: Double, val yOffset: Double) {
        HELMET(EntityEquipmentSlot.HEAD, 0.0, 0.0),
        CHESTPLATE(EntityEquipmentSlot.CHEST, 40.0, 0.0),
        LEGGINGS(EntityEquipmentSlot.LEGS, 0.0, 30.0),
        BOOTS(EntityEquipmentSlot.FEET, 40.0, 30.0)
    }

    // it not updates so just keep in their maybe use in other module
    // [ Recycle trash ☆*: .｡. o(≧▽≦)o .｡.:*☆ ]
    /*init {
        safeListener<LivingEntityUseItemEvent> { event ->
            val entity = event.entityLiving

            val appleItem = Items.APPLE
            val enchantedAppleItem = Items.GOLDEN_APPLE

            try {
                if (entity == Helper.mc.player && entity.heldItemMainhand.item == appleItem) {
                    maxHealth = 24.0f * 1.3f
                }
                else if (entity == Helper.mc.player && entity.heldItemMainhand.item == enchantedAppleItem) {
                    maxHealth = 36.0f * 1.5f
                }
            } catch (e: Exception) {
                ChatUtils.sendMessage("Updated maxHealth to: $maxHealth")
                return@safeListener
            }
        }
    }*/

    override fun onRender() {
        val pos1 = Vec2d(pos.x, pos.y)
        val pos2 = Vec2d(pos.x + getWidth(), pos.y + getHeight())

        updateHealthBar()
        drawHealthBar(pos1, pos2)
        // Armor bar
        drawRectForArmor(ArmorType.HELMET)
        drawRectForArmor(ArmorType.CHESTPLATE)
        drawRectForArmor(ArmorType.LEGGINGS)
        drawRectForArmor(ArmorType.BOOTS)
    }

    private fun drawHealthBar(pos1: Vec2d, pos2: Vec2d) {
        val fr = Fonts.DEFAULT

        RectBuilder(pos1.plus(0.0, getHeight()), Vec2d(pos1.x + getWidth(), pos2.y - 10)).apply {
            color(Color.WHITE.setAlpha(60))
            draw()
        }
        val healthBarWidth = lerp(0.0, (pos2.x - pos1.x) * healthProgress, 1.0).toFloat()

        val lerpedHealthBarWidth = lerp(0.0, (pos2.x - pos1.x) * previousHealthProgress, 1.0).toFloat()

        RectBuilder(pos1.plus(0.0, getHeight()), Vec2d(pos1.x + lerpedHealthBarWidth, pos2.y - 10)).apply {
            color(Color.WHITE.setAlpha(100))
            draw()
        }
        RectBuilder(pos1.plus(0.0, getHeight()), Vec2d(pos1.x + healthBarWidth, pos2.y - 10)).apply {
            color(Color.WHITE)
            draw()
        }

        val healthPercentage = healthProgress * 100

        fr.drawString(
            "%.0f%%".format(healthPercentage),
            pos1.plus(getWidth() - fr.getStringWidth("%.0f%%".format(healthPercentage), 2.0 * scale * 0.65), getHeight() - 40),
            color = if (maxHealth > 20) Color(255, 210, 0) else Color.WHITE,
            scale = 2.0 * scale * 0.65
        )
    }

    private fun updateHealthBar() {
        val health = Helper.mc.player.health
        val newHealthProgress = clamp(health / maxHealth, 0.0f, 1.0f)

        healthProgress = lerp(healthProgress.toFloat(), newHealthProgress, GLUtils.deltaTimeFloat() * 7.0f).toDouble()
        previousHealthProgress = lerp(previousHealthProgress.toFloat(), newHealthProgress, GLUtils.deltaTimeFloat() * 3.0f).toDouble()
    }

    private fun drawRectForArmor(armorType: ArmorType) {
        val pos1 = Vec2d(pos.x + armorType.xOffset, pos.y + armorType.yOffset + 20)

        val boxWidth = getWidth() / 6
        val boxHeight = 20.0
        val playerArmorList: MutableList<ItemStack> = NewOverlay.mc.player.armorInventoryList.toMutableList()

        val color = getArmorSetColorForSlot(playerArmorList, armorType)

        drawArmorRect(pos1, pos1.plus(boxWidth, boxHeight), color)
    }

    private fun getArmorSetColorForSlot(playerArmor: MutableList<ItemStack>, armorType: ArmorType): Color {
        val equippedMaterial = getArmorMaterialEquipped(playerArmor, armorType)

        return equippedMaterial?.let { material ->
            when (material) {
                "diamond" -> Color(95, 210, 255)
                "iron" -> Color(160, 160, 160)
                "gold" -> Color(255, 200, 0)
                "leather" -> Color(100, 60, 55)
                else -> Color.WHITE
            }
        } ?: Color(150, 150, 150, 100)
    }

    private fun getArmorMaterialEquipped(playerArmor: MutableList<ItemStack>, armorType: ArmorType): String? {
        for (itemStack in playerArmor) {
            val item = itemStack.item
            if (item is ItemArmor && item.armorType == armorType.slot) {
                return item.armorMaterial.name.lowercase()
            }
        }
        return null
    }

    private fun drawArmorRect(pos1: Vec2d, pos2: Vec2d, color: Color) {
        RectBuilder(pos1, pos2).apply {
            color(color)
            draw()
        }
    }

    override fun getWidth() = w * scale * 0.65
    override fun getHeight() = h * scale * 0.65
}