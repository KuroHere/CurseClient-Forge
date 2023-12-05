package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.event.listener.tryGetOrNull
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.entity.interpolatedPosition
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.roundToPlaces
import com.curseclient.client.utility.math.MathUtils.toIntSign
import com.curseclient.client.utility.render.vector.Vec2d
import com.curseclient.client.utility.player.TargetingUtils.getTargetList
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.RenderUtils2D
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.FontUtils.getHeight
import com.curseclient.client.utility.render.font.FontUtils.getStringWidth
import com.curseclient.client.utility.render.font.Fonts
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.max

object Nametags : Module(
    "Nametags",
    "Better than minecraft nametags.",
    Category.VISUAL
) {
    private val page by setting("Page", Page.Targets)

    private val range by setting("Range", 100.0, 20.0, 1000.0, 5.0, { page == Page.Targets })
    private val players by setting("Players", true, { page == Page.Targets })
    private val animals by setting("Animals", true, { page == Page.Targets })
    private val hostileMobs by setting("Hostile", true, { page == Page.Targets })
    private val invisible by setting("Invisible", true, { page == Page.Targets })

    private val healthBarWidth by setting("Health Bar Width", 1.0, 0.0, 3.0, 0.2, { page == Page.Content })
    private val healthText by setting("Health Text", true, { page == Page.Content })
    private val pingText by setting("Ping Text", true, { page == Page.Content })
    private val distanceText by setting("Distance Text", false, { page == Page.Content })
    private val armor by setting("Armor", false, { page == Page.Content })
    private val mainHand by setting("Main Hand", false, { page == Page.Content })
    private val offHand by setting("Off Hand", false, { page ==Page.Content })
    private val invertHands by setting("Invert Hands", false, { page == Page.Content && (mainHand || offHand) })
    private val itemHeight by setting("Item Height", 1.0, 0.0, 10.0, 0.1, { page == Page.Content })

    private val scale by setting("Scale", 1.0, 0.5, 2.0, 0.1, { page == Page.Render })
    private val offset by setting("Offset", 0.5, 0.0, 2.0, 0.1, { page == Page.Render })

    private enum class Page {
        Targets,
        Content,
        Render
    }

    private fun SafeClientEvent.getEntityList() =
        getTargetList(players, players, hostileMobs, animals, invisible).filter { player.getDistance(it) < range }

    init {
        safeListener<Render3DEvent> {
            val list = getEntityList()

            val viewerPos = RenderUtils3D.viewerPos

            renderGL {
                list.map { it to it.interpolatedPosition.add(0.0, it.height + offset, 0.0) }.sortedBy { -it.second.distanceTo(viewerPos) }.forEach {
                    val pos = it.second.subtract(viewerPos)

                    matrix {
                        GlStateManager.disableDepth()
                        glTranslated(pos.x, pos.y, pos.z)
                        glNormal3f(0.0f, 1.0f, 0.0f)
                        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                        glRotatef((mc.gameSettings.thirdPersonView != 2).toIntSign().toFloat() * mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)

                        val distance = 1.0 + max(4.0, viewerPos.distanceTo(it.second))
                        val scaleFactor = distance * scale * 0.005
                        glScaled(-scaleFactor, -scaleFactor, scaleFactor)

                        drawNametag(it.first)
                    }
                }
            }
        }
    }

    private fun SafeClientEvent.drawNametag(entity: EntityLivingBase) {
        var text = entity.displayName.formattedText

        val health = entity.health.toDouble().roundToPlaces(1)
        val healthProgress = clamp(health / entity.maxHealth, 0.0, 1.0)
        val healthColor = Color.getHSBColor(healthProgress.toFloat() * 0.3f, 1f, 1f)
        val healthColorCode =
            if (healthProgress < 0.3) "§c"
            else if (healthProgress < 0.6) "§6"
            else "§a"

        if (healthText)
            text += " ${healthColorCode}${health}HP§r"

        if (entity is EntityOtherPlayerMP && pingText) {
            val ping = tryGetOrNull { connection.getPlayerInfo(entity.uniqueID).responseTime } ?: 0
            val pingColorCode =
                if (ping < 100) "§a"
                else if (ping < 300) "§6"
                else "§c"

            text += " ${pingColorCode}${ping}ms§r"
        }

        val distance = player.getDistance(entity).toDouble().roundToPlaces(1)

        if (distanceText)
            text += " ${distance}m§r"

        val width = Fonts.DEFAULT.getStringWidth(text) + 4.0
        val height = Fonts.DEFAULT.getHeight() + 4.0

        val pos1 = Vec2d(-width / 2.0, -height / 2.0)
        val pos2 = Vec2d(width / 2.0, height / 2.0)

        val items = getItems(entity)

        // background
        RenderUtils2D.drawRect(pos1, pos2.plus(0.0, healthBarWidth), Color(0, 0, 0, 100))

        // health bar
        RenderUtils2D.drawRect(
            Vec2d(pos1.x, pos2.y),
            Vec2d(lerp(pos1.x, pos2.x, healthProgress), pos2.y + healthBarWidth),
            healthColor
        )

        // text
        Fonts.DEFAULT.drawString(text, Vec2d(-Fonts.DEFAULT.getStringWidth(text) / 2.0, 0.0))

        var itemX = 0.0
        itemX -= (16.0 + 2.0) * items.count() / 2.0

        items.forEach {
            glPushMatrix()
            drawItem(it, itemX, pos1.y - 16.0 - itemHeight)
            itemX += 16.0 + 2.0
            glPopMatrix()
        }
    }

    private fun drawItem(itemStack: ItemStack, x: Double, y: Double) {
        GlStateManager.enableTexture2D()

        glPushAttrib(GL_SCISSOR_BIT)
        glDisable(GL_SCISSOR_TEST)
        GlStateManager.clear(GL_DEPTH_BUFFER_BIT)
        glPopAttrib()

        GlStateManager.disableDepth()
        GlStateManager.disableAlpha()

        matrix {
            glTranslated(x, y, 0.0)

            mc.renderItem.zLevel = -150f
            RenderHelper.enableGUIStandardItemLighting()
            glEnable(GL_ALPHA_TEST)
            mc.renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0)
            RenderHelper.disableStandardItemLighting()
            mc.renderItem.zLevel = 0f
        }

        GlStateManager.enableDepth()
        GlStateManager.enableAlpha()
    }

    private fun getItems(entity: EntityLivingBase): ArrayList<ItemStack> {
        val items = ArrayList<ItemStack>()

        val main = if (entity.heldItemMainhand.isEmpty) null else entity.heldItemMainhand
        val off = if (entity.heldItemOffhand.isEmpty) null else entity.heldItemOffhand

        if (mainHand && invertHands) main?.let { items.add(it) }
        if (offHand && !invertHands) off?.let { items.add(it) }

        if (armor) items.addAll(entity.armorInventoryList.filter { !it.isEmpty }.reversed())

        if (mainHand && !invertHands) main?.let { items.add(it) }
        if (offHand && invertHands) off?.let { items.add(it) }

        return items
    }
}