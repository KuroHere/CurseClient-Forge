package com.curseclient.client.module.modules.visual

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.client.PerformancePlus
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.ColorUtils.setAlpha
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GLUtils.renderGL
import com.curseclient.client.utility.render.graphic.GLUtils.translateGL
import com.curseclient.client.utility.render.RenderTessellator
import com.curseclient.client.utility.render.RenderUtils3D
import com.curseclient.mixin.accessor.render.AccessorModelEnderCrystal
import com.curseclient.mixin.accessor.render.AccessorRenderEnderCrystal
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityEnderCrystal
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.sin

object CrystalRenderer : Module(
    "CrystalRenderer",
    "Rich crystals",
    Category.VISUAL
) {
    private val page by setting("Page", Page.General)

    private val scaleSetting by setting("Scale", 1.0, 0.1, 2.0, 0.05, { page == Page.General })
    private val bobbingAmount by setting("Bobbing Amount", 1.0, 0.0, 2.0, 0.05, { page == Page.General })
    private val bobbingSpeed by setting("Bobbing Speed", 1.0, 0.0, 5.0, 0.05, { page == Page.General })
    private val spinSpeed by setting("Spin Speed", 1.0, 0.05, 5.0, 0.05, { page == Page.General })
    private val staticSpin by setting("Static Spin", false, { page == Page.General })
    private val height by setting("Height", 0.6, -1.0, 1.0, 0.05, { page == Page.General })
    private val drawCube by setting("Draw Cube", true, { page == Page.General })
    private val original by setting("Original", true, { page == Page.General })

    private val filled by setting("Filled", true, { page == Page.Filled })
    private val filledAlpha by setting("Filled Alpha", 50.0, 0.0, 255.0, 5.0, { page == Page.Filled && filled })
    private val filledThroughWall by setting("Filled Through Wall", true, { page == Page.Filled && filled })

    private val outline by setting("Outline", true, { page == Page.Outline })
    private val outlineAlpha by setting("Outline Alpha", 50.0, 0.0, 255.0, 5.0, { page == Page.Outline && outline })
    private val outlineThroughWall by setting("Outline Through Wall", false, { page == Page.Outline && outline })
    private val outlineWidth by setting("Outline Width", 1.0, 1.0, 5.0, 0.25, { page == Page.Outline })

    private val render by lazy { mc.renderManager.getEntityClassRenderObject<EntityEnderCrystal>(EntityEnderCrystal::class.java) as AccessorRenderEnderCrystal }

    private enum class Page {
        General,
        Filled,
        Outline
    }

    init {
        safeListener<Render3DEvent> {
            renderGL {
                mc.renderManager.renderEngine.bindTexture(render.textures)
                GlStateManager.glLineWidth(outlineWidth.toFloat())

                world.loadedEntityList.forEach {
                    if (it !is EntityEnderCrystal) return@forEach
                    doRender(it)
                }
            }
        }
    }

    private fun SafeClientEvent.doRender(entity: EntityEnderCrystal) {
        val crystalPos = entity.positionVector

        val range = PerformancePlus.getEntityRenderRange(entity) ?: 128.0
        val distCheck = crystalPos.squareDistanceTo(RenderUtils3D.viewerPos) < (range * range)
        if (!distCheck) return

        val pTicks = RenderTessellator.partialTicks
        val model = render.model as AccessorModelEnderCrystal

        val ticks = (if (staticSpin) player.ticksExisted else entity.innerRotation).toFloat() + pTicks

        var f1 = sin(ticks * 0.2f * bobbingSpeed.toFloat()) / 2.0f + 0.5f
        f1 += f1 * f1

        val age = f1 * 0.2f
        val spinAmount = ticks * 3.0f * spinSpeed.toFloat()

        matrix {
            crystalPos.subtract(RenderUtils3D.viewerPos).translateGL()

            if (original) {
                GlStateManager.enableTexture2D()
                drawModel(age, spinAmount, model, false)
                GlStateManager.disableTexture2D()
            }

            if (filled) {
                HUD.getColor().setAlpha(filledAlpha.toInt()).glColor()
                drawModel(age, spinAmount, model, filledThroughWall)
            }

            if (outline) {
                HUD.getColor().setAlpha(outlineAlpha.toInt()).glColor()
                GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
                drawModel(age, spinAmount, model, outlineThroughWall)
                GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
            }
        }
    }

    private fun drawModel(age: Float, limbSwingAmount: Float, model: AccessorModelEnderCrystal, through: Boolean) {
        matrix {
            if (through) {
                GlStateManager.disableDepth()
            } else {
                GlStateManager.enableDepth()
            }

            val cube = model.cube
            val glass = model.glass

            val scale = 0.0625f * scaleSetting.toFloat()
            GlStateManager.scale(2.0f * scaleSetting.toFloat(), 2.0f * scaleSetting.toFloat(), 2.0f * scaleSetting.toFloat())
            GlStateManager.translate(0.0f, -0.5f, 0.0f)

            GlStateManager.rotate(limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.translate(0.0f, 0.3f + height.toFloat() + age * max(0.01, bobbingAmount).toFloat(), 0.0f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            glass.render(scale)

            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(limbSwingAmount, 0.0f, 1.0f, 0.0f)
            glass.render(scale)

            if (drawCube) {
                GlStateManager.scale(0.875f, 0.875f, 0.875f)
                GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
                GlStateManager.rotate(limbSwingAmount, 0.0f, 1.0f, 0.0f)
                cube.render(scale)
            }
        }
    }
}