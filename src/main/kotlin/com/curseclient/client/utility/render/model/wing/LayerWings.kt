package com.curseclient.client.utility.render.model.wing

import baritone.api.utils.Helper.mc
import com.curseclient.client.module.modules.visual.CustomModel
import com.curseclient.client.utility.extension.mixins.timer
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import kotlin.math.cos

import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly


@SideOnly(Side.CLIENT)
class LayerWings(private val renderPlayer: RenderPlayer) : LayerRenderer<AbstractClientPlayer> {

    private val wingTextures = textures.values().associateWith { ResourceLocation(it.texture) }

    enum class textures(val texture: String) {
        Feather("textures/cosmetic/wing_feather.png"),
        Demon("textures/cosmetic/wing_demon.png"),
        ButterFly("textures/cosmetic/wing_butterfly.png"),
        Golden("textures/cosmetic/wing_golden.png"),
        Bat("textures/cosmetic/wing_bat.png"),
        Chicken("textures/cosmetic/wing_chicken.png")
    }


    fun renderWing(player: AbstractClientPlayer) {

        GlStateManager.color(1f, 1f, 1f, 1f)

        GlStateManager.pushMatrix()
        run {
            val bipedBody = renderPlayer.mainModel.bipedBody
            bipedBody.postRender(0.0625f)

            val selectedWingTexture = wingTextures[CustomModel.wingLayer]
            if (selectedWingTexture != null) {
                Minecraft.getMinecraft().textureManager.bindTexture(selectedWingTexture)
            }
            val v = (bipedBody.cubeList[0].posZ2 - bipedBody.cubeList[0].posZ1) / 2

            GlStateManager.translate(0.0f, if (player.isSneaking) 0.125f else 0f, 0.0625f * v)

            val a: Float

            val isFlying = player.capabilities?.isFlying ?: false
            a = (1 + cos(mc.timer.renderPartialTicks / 4.0) * (if (isFlying) 20 else 2) + 25).toFloat()

            var displayList = 0
            if (displayList == 0) {
                val instance = Tessellator.getInstance()
                val t = instance.buffer
                displayList = GLAllocation.generateDisplayLists(2)
                GlStateManager.glNewList(displayList, GL11.GL_COMPILE)
                GlStateManager.color(1f, 1f, 1f, 1f)
                GlStateManager.translate(0.0f, -0.25f - 0.0625f, 0f)
                t.begin(7, DefaultVertexFormats.POSITION_TEX)
                t.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex()
                t.pos(0.0, 1.0, 0.0).tex(0.0, 1.0).endVertex()
                t.pos(1.0, 1.0, 0.0).tex(1.0, 1.0).endVertex()
                t.pos(1.0, 0.0, 0.0).tex(1.0, 0.0).endVertex()
                instance.draw()
                GlStateManager.glEndList()

                GlStateManager.glNewList(displayList + 1, GL11.GL_COMPILE)
                t.begin(7, DefaultVertexFormats.POSITION_TEX)
                GlStateManager.translate(0.0f, -0.25f - 0.0625f, 0f)
                t.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).endVertex()
                t.pos(0.0, 1.0, 0.0).tex(0.0, 1.0).endVertex()
                t.pos(-1.0, 1.0, 0.0).tex(1.0, 1.0).endVertex()
                t.pos(-1.0, 0.0, 0.0).tex(1.0, 0.0).endVertex()
                instance.draw()
                GlStateManager.glEndList()
            }

            GlStateManager.pushMatrix()
            run {
                GlStateManager.rotate(-a, 0f, 1f, 0f)
                GlStateManager.callList(displayList)
            }
            GlStateManager.popMatrix()

            GlStateManager.pushMatrix()
            run {
                GlStateManager.rotate(a, 0f, 1f, 0f)
                GlStateManager.callList(displayList + 1)
            }
            GlStateManager.popMatrix()
        }
        GlStateManager.popMatrix()

    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }

    override fun doRenderLayer(
        entitylivingbaseIn: AbstractClientPlayer,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        scale: Float) {

    }
}