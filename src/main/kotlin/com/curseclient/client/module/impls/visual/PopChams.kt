package com.curseclient.client.module.impls.visual

import baritone.api.event.events.TickEvent
import com.curseclient.client.event.events.TotemPopEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.events.render.RenderModelEntityEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.a
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.mixin.accessor.entity.AccessorEntityPlayer
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object PopChams : Module(
    "PopChams",
    "PopChams duh",
    Category.VISUAL
) {
    private val renderStyle by setting("Style", Style.WIREFRAME)
    private val aliveTime by setting("Time", 2000, 50, 5000, 50, description = "Time a cham will be shown")
    private val move by setting("Move", true, description = "Move the cham after its creation")
    private val direction by setting("Direction", MovementDirection.UP, description = "Direction to move the cham", visible = { move })
    private val movementHeight by setting("Height", 4.5F, 2.0F, 10.0F, 0.1F, description = "Height that the chams will travel", visible = { move })
    private val easing by setting("Easing", Easing.QUINT_IN, description = "Will be used for fading and moving", visible =  { fadeOut || move })
    private val outlineColor by setting("Color", Color(0, 155, 220))
    private val outlineWidth by setting("Width", 3.5, 0.1, 10.0, 0.1)
    private val fadeOut by setting("Fade", true)
    private val self by setting("Self", false)

    init {
        val chams: MutableList<ChamData> = CopyOnWriteArrayList()
        val chamCache: MutableMap<EntityPlayer, ChamData> = ConcurrentHashMap()

        safeListener<TickEvent.Type> {
            chams.removeIf { System.currentTimeMillis() - it.startTime > aliveTime }
        }
        safeListener<TotemPopEvent> { event ->
            if (!self && event.player == PopChams.mc.player) {
                return@safeListener
            }

            val cham = chamCache[event.player] ?: return@safeListener
            chams.add(
                ChamData(
                    cham.model, EntityOtherPlayerMP( //Copying so we don't have new animations or anything
                    cham.entity.world, (cham.entity as AccessorEntityPlayer).hookGetGameProfile()
                ).also { it.copyLocationAndAnglesFrom(cham.entity) }, cham.limbSwing, cham.limbSwingAmount, cham.ageInTicks, cham.netHeadYaw, cham.headPitch, cham.scale
                )
            )
        }
        safeListener<RenderModelEntityEvent> { event ->
            if (event.entity is EntityPlayer) {
                chamCache[event.entity] = ChamData(
                    event.modelBase as ModelPlayer,
                    event.entity,
                    event.limbSwing,
                    event.limbSwingAmount,
                    event.ageInTicks,
                    event.netHeadYaw,
                    event.headPitch,
                    event.scale
                )
            }
        }
        safeListener<Render3DEvent> {
            chams.forEach {
                it.animation.state = true
                val animFac = it.animation.getAnimationFactor()

                PopChams.mc.renderManager.isRenderShadow = false

                glPushMatrix()

                //Positioning
                glTranslated(
                    (PopChams.mc.renderManager.viewerPosX - it.entity.posX) * -1.0, 1.4 + ((PopChams.mc.renderManager.viewerPosY - it.entity.posY) * -1.0), (PopChams.mc.renderManager.viewerPosZ - it.entity.posZ) * -1.0
                )

                if (move) {
                    glTranslated(
                        0.0, (if (direction == MovementDirection.DOWN) -movementHeight else movementHeight) * animFac, 0.0
                    )
                }

                //Flipping and setting the correct rotation and scale
                glRotatef(180F, 1F, 0F, 0F)
                glRotatef(-it.netHeadYaw, 0F, 1F, 0F)
                glScalef(0.95F, 0.95F, 0.95F)

                glAlphaFunc(GL_GREATER, 0.015686274F)

                glPushMatrix()
                glPushAttrib(GL_ALL_ATTRIB_BITS)

                val colour = Color(
                    outlineColor.r, outlineColor.g, outlineColor.b,

                    if (fadeOut) {
                        1.0 - animFac.coerceIn(0.0, 1.0)
                    }
                    else {
                        outlineColor.a
                    }.toFloat()
                )

                glColor4f(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

                glPolygonMode(GL_FRONT_AND_BACK, if (renderStyle == Style.FILL) GL_FILL else GL_LINE_STRIP)
                glDisable(GL_TEXTURE_2D)
                glEnable(GL_LINE_SMOOTH)
                glEnable(GL_BLEND)

                GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
                GlStateManager.glLineWidth(outlineWidth.toFloat())

                glEnable(GL_DEPTH_TEST)
                glDepthMask(false)

                glDepthRange(0.1, 1.0)
                glDepthFunc(GL_GREATER)

                glColor4f(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

                it.model.render(
                    it.entity, it.limbSwing, it.limbSwingAmount, it.ageInTicks, it.netHeadYaw, it.headPitch, it.scale
                )

                glDepthFunc(GL_LESS)
                glDepthRange(0.0, 1.0)
                glEnable(GL_DEPTH_TEST)
                glDepthMask(false)

                glColor4f(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

                it.model.render(
                    it.entity, it.limbSwing, it.limbSwingAmount, it.ageInTicks, it.netHeadYaw, it.headPitch, it.scale
                )

                glPopAttrib()
                glPopMatrix()
                glPopMatrix()
                PopChams.mc.renderManager.isRenderShadow = PopChams.mc.gameSettings.entityShadows
            }
        }
    }



    internal class ChamData(
        val model: ModelPlayer,
        val entity: Entity,
        val limbSwing: Float,
        val limbSwingAmount: Float,
        val ageInTicks: Float,
        val netHeadYaw: Float,
        val headPitch: Float,
        val scale: Float,
        val startTime: Long = System.currentTimeMillis(),
        val animation: Animation = Animation(aliveTime.toFloat(), false, easing),
    )

    internal enum class MovementDirection {
        UP, DOWN
    }

    internal enum class Style {
        WIREFRAME, FILL
    }

}