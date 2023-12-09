package com.curseclient.client.utility.render.model.diver

import com.curseclient.client.module.modules.visual.CustomModel
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.FMLClientHandler
import org.lwjgl.opengl.GL11
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly


@SideOnly(Side.CLIENT)
class OxygenMask : ModelBase(), LayerRenderer<AbstractClientPlayer> {

    private val location: ResourceLocation = ResourceLocation("textures/cosmetic/oxygen.png")
    private val oxygenMask: ModelRenderer

    init {
        val scaleFactor = 1.0F
        oxygenMask = ModelRenderer(this, 0, 0).apply {
            setTextureSize(128, 64)
            addBox(-8.0F, -16.0F, -8.0F, 16, 16, 16, scaleFactor)
            setRotationPoint(0.0F, 0.0F, 0.0F)
        }
    }

    fun renderMask(player: AbstractClientPlayer, scale: Double) {
        if (CustomModel.isEnabled() && !player.isInvisible) {
            FMLClientHandler.instance().client.renderEngine.bindTexture(location)

            val yawOffset = Math.toRadians(player.rotationYawHead.toDouble())
            val pitchOffset = Math.toRadians(player.rotationPitch.toDouble())

            val rotationYaw = yawOffset - Math.toRadians(player.renderYawOffset.toDouble())
            val rotationPitch = -pitchOffset

            val sneakingOffset = if (player.isSneaking) {
                val factor = if (rotationPitch < 0) 0.36 else -0.36
                0.8 + rotationPitch * factor
            } else 0.0

            GlStateManager.enableRescaleNormal()
            GlStateManager.pushMatrix()
            GL11.glScaled(-scale, scale, scale)
            GlStateManager.scale(0.5F, 0.5F, 0.5F)

            GL11.glPushMatrix()
            GL11.glRotatef(-Math.toDegrees(rotationYaw).toFloat(), 0.0f, 1.0f, 0.0f)
            GL11.glRotatef(-Math.toDegrees(rotationPitch).toFloat(), 1.0f, 0.0f, 0.0f)
            GL11.glTranslatef(0.0f, sneakingOffset.toFloat(), 0.0f)

            GL11.glScalef(1.05F, 1.05F, 1.05F)
            oxygenMask.render(0.0895f)
            GL11.glScalef(1F, 1F, 1F)
            GL11.glPopMatrix()

            GlStateManager.popMatrix()
        }
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