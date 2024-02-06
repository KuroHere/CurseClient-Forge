package com.curseclient.client.utility.render.model.wing

import com.curseclient.client.module.impls.visual.Cosmetic
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import kotlin.math.cos

@SideOnly(Side.CLIENT)
class CrystalWings : ModelBase(), LayerRenderer<AbstractClientPlayer> {

    private val model: ModelRenderer
    private val resourceLocation: ResourceLocation = ResourceLocation("textures/cosmetic/crystal.png")

    init {
        val i = 30
        val j = 24
        model = ModelRenderer(this)
        model.setTextureSize(i, j)
        model.setTextureOffset(0, 8)
        model.setRotationPoint(-0.0f, 1.0f, 0.0f)
        model.addBox(0.0f, -3.0f, 0.0f, 14, 7, 1)
        model.isHidden = true
        val modelrenderer = ModelRenderer(this)
        modelrenderer.setTextureSize(i, j)
        modelrenderer.setTextureOffset(0, 16)
        modelrenderer.setRotationPoint(-0.0f, 0.0f, 0.2f)
        modelrenderer.addBox(0.0f, -3.0f, 0.0f, 14, 7, 1)
        model.addChild(modelrenderer)
        val modelrenderer1 = ModelRenderer(this)
        modelrenderer1.setTextureSize(i, j)
        modelrenderer1.setTextureOffset(0, 0)
        modelrenderer1.setRotationPoint(-0.0f, 0.0f, 0.2f)
        modelrenderer1.addBox(0.0f, -3.0f, 0.0f, 14, 7, 1)
        modelrenderer.addChild(modelrenderer1)
    }

    fun render(entityIn: AbstractClientPlayer, walkingSpeed: Float, tickValue: Float, scale: Float) {
        if (entityIn == Minecraft.getMinecraft().player && !entityIn.isInvisible) {
            val f = (cos((tickValue / 10.0f).toDouble()) / 20.0f - 0.03f - walkingSpeed / 20.0f).toFloat()
            val modelrenderer = model.childModels[0] as ModelRenderer
            val modelrenderer1 = modelrenderer.childModels[0] as ModelRenderer
            model.rotateAngleZ = f * 3.0f
            modelrenderer.rotateAngleZ = f / 2.0f
            modelrenderer1.rotateAngleZ = f / 2.0f
            model.rotateAngleY = -0.3f - walkingSpeed / 3.0f
            model.rotateAngleX = 0.3f

            GlStateManager.pushMatrix()
            GlStateManager.scale(1.6, 1.6, 1.0)
            GlStateManager.translate(0.0, 0.05000000074505806, 0.05000000074505806)
            if (entityIn.isSneaking) {
                GlStateManager.translate(0.0, 0.07999999821186066, 0.029999999329447746)
                GlStateManager.rotate(20.0f, 1.0f, 0.0f, 0.0f)
                model.rotateAngleZ = 0.8f
                modelrenderer.rotateAngleZ = 0.0f
                modelrenderer1.rotateAngleZ = 0.0f
            } else {
                val rendermanager = Minecraft.getMinecraft().renderManager
                GlStateManager.rotate(rendermanager.playerViewX / 3.0f, 1.0f, 0.0f, 0.0f)
            }
            model.isHidden = false
            for (i in -1..1 step 2) {
                GlStateManager.pushMatrix()
                GlStateManager.color(1.0f, 1.0f, 1.0f, 0.3f)
                GlStateManager.depthMask(false)
                GlStateManager.enableBlend()
                GlStateManager.blendFunc(770, 771)
                GlStateManager.alphaFunc(516, 0.003921569f)

                GlStateManager.disableLighting()
                GL11.glColor4f(Cosmetic.color.red.toFloat(), Cosmetic.color.green.toFloat(), Cosmetic.color.blue.toFloat(), Cosmetic.color.alpha.toFloat())

                Minecraft.getMinecraft().textureManager.bindTexture(resourceLocation)
                if (i == 1) GlStateManager.scale(-1.0f, 1.0f, 1.0f)
                GlStateManager.translate(0.05, 0.0, 0.0)
                model.render(scale)
                GlStateManager.enableLighting()
                GlStateManager.disableBlend()
                GlStateManager.alphaFunc(516, 0.1f)
                GlStateManager.depthMask(true)
                GlStateManager.popMatrix()
            }
            model.isHidden = true
            GlStateManager.popMatrix()
        }
    }

    override fun doRenderLayer(
        entitylivingbaseIn: AbstractClientPlayer,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        scale: Float
    ) {
    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }
}
