package com.curseclient.client.module.modules.hud

import baritone.api.utils.Helper
import com.curseclient.client.module.DraggableHudModule
import com.curseclient.client.module.HudCategory
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.vector.Vec2d
import com.jhlabs.image.ImageMath.clamp
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import java.awt.Color


// TODO: tone of thing need to be done
object NewOverlay: DraggableHudModule(
    "NewOverlay",
    "New way to render hp, armor, hunger bar(Not done yet)",
    HudCategory.HUD
) {
    private val scale by setting("Scale", 1.5, 1.5, 2.5, 0.1)

    private var maxHealth = 20.0f
    private var healthProgress = 0.0
    private var previousHealthProgress = 0.0

    private const val w = 150.0
    private const val h = 80.0
    val xValues = listOf(7.3125, 7.8, 8.2875, 8.775, 9.2625, 9.75, 10.2375, 10.725, 11.2125, 11.7, 12.1875)

    enum class ArmorType(val slot: EntityEquipmentSlot?, val xOffset: Double) {
        HELMET(EntityEquipmentSlot.HEAD, getWidth() * 0.4),
        CHESTPLATE(EntityEquipmentSlot.CHEST, getWidth() * 0.5),
        LEGGINGS(EntityEquipmentSlot.LEGS, getWidth() * 0.6),
        BOOTS(EntityEquipmentSlot.FEET, getWidth() * 0.7)
    }

    override fun onRender() {
        val pos1 = Vec2d(pos.x, pos.y)
        val pos2 = Vec2d(pos.x + getWidth(), pos.y + getHeight())

        updateHealthBar()
        drawHealthBar(pos1, pos2)
        drawRectForArmor(ArmorType.HELMET)
        drawRectForArmor(ArmorType.CHESTPLATE)
        drawRectForArmor(ArmorType.LEGGINGS)
        drawRectForArmor(ArmorType.BOOTS)
    }


    private fun drawHealthBar(pos1: Vec2d, pos2: Vec2d) {
        val fr = Fonts.DEFAULT

        val healthBarWidth = lerp(0.0, (pos2.x - pos1.x) * healthProgress, 1.0).toFloat()
        val lerpedHealthBarWidth = lerp(0.0, (pos2.x - pos1.x) * previousHealthProgress, 1.0).toFloat()

        var healthPercentage = healthProgress * 100

        val entity: EntityPlayer = mc.player
        val effects: Collection<PotionEffect> = entity.activePotionEffects
        if (!effects.isEmpty()) {
            for (effect in effects) {
                val potionID = Potion.getIdFromPotion(effect.potion)
                if (potionID == 22) {
                    healthPercentage = healthProgress * 150
                }
            }
        }
        val processColor = if (healthPercentage <= 40) Color(200, 0, 0) else if (healthPercentage > 100) Color(255, 200, 0) else Color.WHITE

        // background
        RectBuilder(pos1.plus(0.0, getHeight()), Vec2d(pos1.x + getWidth(), pos2.y - 10)).apply {
            color(Color.WHITE.setAlpha(60))
            draw()
        }
        // health process
        RectBuilder(pos1.plus(0.0, getHeight()), Vec2d(pos1.x + lerpedHealthBarWidth, pos2.y - 10)).apply {
            color(Color.WHITE.setAlpha(100))
            draw()
        }
        RectBuilder(pos1.plus(0.0, getHeight()), Vec2d(pos1.x + healthBarWidth, pos2.y - 10)).apply {
            color(processColor)
            draw()
        }

        fr.drawString(
            "%.0f%%".format(healthPercentage),
            pos1.plus(0.0, getHeight() - 25),
            color = processColor,
            scale = 1.8 * scale * 0.65
        )
    }

    private fun updateHealthBar() {
        val health = Helper.mc.player.health
        val newHealthProgress = clamp(health / maxHealth, 0.0f, 1.0f)

        healthProgress = lerp(healthProgress.toFloat(), newHealthProgress, GLUtils.deltaTimeFloat() * 7.0f).toDouble()
        previousHealthProgress = lerp(previousHealthProgress.toFloat(), newHealthProgress, GLUtils.deltaTimeFloat() * 2.0f).toDouble()
    }

    private fun drawRectForArmor(armorType: ArmorType) {
        val pos1 = Vec2d(pos.x + armorType.xOffset, pos.y + getHeight() - 15)

        val index = ((scale - 1.5) / 0.1).toInt()
        val x = if (index in xValues.indices) xValues[index] else 0.0

        val boxWidth = getWidth() / x
        val boxHeight = 8.0

        val playerArmorList: MutableList<ItemStack> = NewOverlay.mc.player.armorInventoryList.toMutableList()

        val color = getArmorSetColorForSlot(playerArmorList, armorType)

        drawArmorRect(pos1, pos1.minus(boxWidth, boxHeight), color)
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
        } ?: Color(150, 150, 150, 50)
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

    // I Don't want to remove all of this so just keep in there gonna use in future or maybe not 🫥

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

    override fun getWidth() = w * scale * 0.65
    override fun getHeight() = h * scale * 0.65
}