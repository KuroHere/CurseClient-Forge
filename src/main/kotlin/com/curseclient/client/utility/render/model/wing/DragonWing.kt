package com.curseclient.client.utility.render.model.wing

import com.curseclient.client.module.impls.visual.Cosmetic
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

import kotlin.math.cos
import kotlin.math.sin

import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly


@SideOnly(Side.CLIENT)
class DragonWing : ModelBase(), LayerRenderer<AbstractClientPlayer> {
    private val wingTextures = textures.values().associateWith { ResourceLocation(it.texture) }

    enum class textures(val texture: String) {
        Wing("textures/cosmetic/wings.png"),
        Wing1("textures/cosmetic/gwings.png"),
        Wing2("textures/cosmetic/gwings1.png")
    }

    private val wing: ModelRenderer
    private val wingTip: ModelRenderer

    init {
        this.setTextureOffset("wing.bone", 0, 0)
        this.setTextureOffset("wing.skin", -10, 8)
        this.setTextureOffset("wingtip.bone", 0, 5)
        this.setTextureOffset("wingtip.skin", -10, 18)
        wing = ModelRenderer(this, "wing")
        wing.setTextureSize(30, 30)
        wing.setRotationPoint(-2.0f, 0.0f, 0.0f)
        wing.addBox("bone", -10.0f, -1.0f, -1.0f, 10, 2, 2)
        wing.addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10)
        wingTip = ModelRenderer(this, "wingtip")
        wingTip.setTextureSize(30, 30)
        wingTip.setRotationPoint(-10.0f, 0.0f, 0.0f)
        wingTip.addBox("bone", -10.0f, -0.5f, -0.5f, 10, 1, 1)
        wingTip.addBox("skin", -10.0f, 0.0f, 0.5f, 10, 0, 10)
        wing.addChild(wingTip)
    }

    fun renderWing(player: AbstractClientPlayer, scale: Double) {
        if (Cosmetic.isEnabled() && !player.isInvisible) {
            GL11.glPushMatrix()
            GL11.glScaled(-scale, -scale, scale)
            GL11.glTranslated(0.0, -1.45, 0.0)
            GL11.glTranslated(0.0, 1.3, 0.2 / scale)
            if (player.isSneaking) {
                GlStateManager.translate(0.0, -0.142, -0.0178)
            }
            GL11.glRotated(180.0, 1.0, 0.0, 0.0)
            GL11.glRotated(180.0, 0.0, 1.0, 0.0)

            GlStateManager.color(Cosmetic.color.red / 255.0f, Cosmetic.color.green / 255.0f, Cosmetic.color.blue / 255.0f, 1.0f)
            val selectedWingTexture = wingTextures[Cosmetic.dragon]
            if (selectedWingTexture != null) {
                Minecraft.getMinecraft().textureManager.bindTexture(selectedWingTexture)
            }
            for (j in 0..1) {
                GL11.glEnable(2884)
                val f11: Float = (System.currentTimeMillis() % 1000L / 1000.0f * 3.1415927f * 2.0f)
                wing.rotateAngleX = (Math.toRadians(-80.0) - cos(f11.toDouble()) * 0.2f).toFloat()
                wing.rotateAngleY = (Math.toRadians(20.0) + sin(f11.toDouble()) * 0.4f).toFloat()
                wing.rotateAngleZ = Math.toRadians(20.0).toFloat()
                wingTip.rotateAngleZ = (-(sin((f11 + 2.0f).toDouble()) + 0.5) * 0.75f).toFloat()
                wing.render(0.0625f)
                GL11.glScalef(-1.0f, 1.0f, 1.0f)
                if (j == 0) {
                    GL11.glCullFace(1028)
                }
            }
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glCullFace(1029)
            GL11.glDisable(2884)
            GL11.glPopMatrix()
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