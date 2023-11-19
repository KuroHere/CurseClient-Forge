package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.FriendManager.isFriend
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.player.TargetingUtils.getTargetList
import com.curseclient.client.utility.player.TargetingUtils.isHostile
import com.curseclient.client.utility.render.RenderUtils3D.drawTrace
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

object Tracers: Module(
    "Tracers",
    "Draws lines to targets",
    Category.VISUAL
) {
    private val page by setting("Page", Page.Targets)

    private val players by setting("Players", false, visible = { page == Page.Targets })
    private val friends by setting("Friends", false, visible = { page == Page.Targets && players })
    private val hostiles by setting("Hostiles", false, visible = { page == Page.Targets })
    private val animals by setting("Animals", false, visible = { page == Page.Targets })

    private val lineWidth by setting("Line Width", 1.0, 1.0, 3.0, 0.1, visible = { page == Page.Colors })
    private val playerColor by setting("Player Color", Color(200, 20, 20, 250), visible = { page == Page.Colors })
    private val friendsColor by setting("Friends Color", Color(200, 200, 20, 250), visible = { page == Page.Colors })
    private val hostileColor by setting("Hostile Color", Color(20, 200, 20, 250), visible = { page == Page.Colors })
    private val animalColor by setting("Animal Color", Color(20, 20, 200, 250), visible = { page == Page.Colors })

    private enum class Page {
        Targets,
        Colors
    }

    init {
        safeListener<Render3DEvent> {
            for(e in getTargetList(players, friends, hostiles, animals, true)){
                drawTrace(e, mc.renderPartialTicks, e.getColor(), lineWidth.toFloat())
            }
        }
    }

    private fun EntityLivingBase.getColor(): Color{
        if (this is EntityPlayer) {
            return if(!this.isFriend()) playerColor else friendsColor
        }
        if (this.isHostile) return hostileColor
        return animalColor
    }
}