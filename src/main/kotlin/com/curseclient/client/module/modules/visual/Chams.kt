package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.render.RenderEntityEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*

object Chams : Module(
    "Chams",
    "Specific entity renderer",
    Category.VISUAL
) {
    private val page by setting("Page", Page.Render)

    private val red by setting("Red", 1.0, 0.0, 1.0, 0.05, { page == Page.Render })
    private val green by setting("Green", 1.0, 0.0, 1.0, 0.05, { page == Page.Render })
    private val blue by setting("Blue", 1.0, 0.0, 1.0, 0.05, { page == Page.Render })
    private val alpha by setting("Alpha", 1.0, 0.0, 1.0, 0.05, { page == Page.Render })
    private val throughWall by setting("Through Wall", true, { page == Page.Render })
    private val texture by setting("Texture", false, { page == Page.Render })
    private val lightning by setting("Lightning", false, { page == Page.Render })

    private val all by setting("All", false, { page == Page.Targets })
    private val self by setting("Self", false, { page == Page.Targets && !all })
    private val players by setting("Players", true, { page == Page.Targets && !all })
    private val crystals by setting("Crystals", false, { page == Page.Targets && !all })
    private val items by setting("Items", false, { page == Page.Targets && !all })

    private enum class Page {
        Render,
        Targets
    }

    init {
        safeListener<RenderEntityEvent.Pre>(2000) {
            if (!checkEntity(it.entity)) return@safeListener
            if (throughWall) glDepthRange(0.0, 0.01)
        }

        safeListener<RenderEntityEvent.Peri>(2000) {
            if (!checkEntity(it.entity)) return@safeListener
            if (throughWall) glDepthRange(0.0, 1.0)
        }

        safeListener<RenderEntityEvent.ModelPre> {
            if (!checkEntity(it.entity)) return@safeListener

            if (!texture) glDisable(GL_TEXTURE_2D)
            if (!lightning) glDisable(GL_LIGHTING)
            glColor4d(red, green, blue, alpha)

            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        }

        safeListener<RenderEntityEvent.ModelPost> {
            if (!checkEntity(it.entity)) return@safeListener
            if (!texture) glEnable(GL_TEXTURE_2D)
            if (!lightning) glEnable(GL_LIGHTING)
            GlStateManager.disableBlend()
            glColor4f(1f, 1f, 1f, 1f)
        }
    }


    private fun SafeClientEvent.checkEntity(entity: Entity) =
        (self || entity != player) && (
            all
                || items && entity is EntityItem
                || crystals && entity is EntityEnderCrystal
                || players && entity is EntityPlayer
            )
}