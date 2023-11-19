package com.curseclient.client.utility.render.model

import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.module.modules.visual.CustomModel
import com.curseclient.client.utility.render.shader.GradientUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import net.minecraft.client.renderer.GlStateManager.resetColor
import kotlin.math.cos
import kotlin.math.sin

class WingModel : ModelBase(), LayerRenderer<AbstractClientPlayer> {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private val location: ResourceLocation = ResourceLocation("textures/model/wings.png")
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
        if (CustomModel.isEnabled() && !player.isInvisible) {
            val c1 = HUD.getColor(0)
            val c2 = HUD.getColor(5)
            GL11.glPushMatrix()
            GL11.glScaled(-scale, -scale, scale)
            GL11.glTranslated(0.0, -1.45, 0.0)
            GL11.glTranslated(0.0, 1.3, 0.2 / scale)
            if (player.isSneaking) {
                GlStateManager.translate(0.0, -0.142, -0.0178)
            }
            GL11.glRotated(180.0, 1.0, 0.0, 0.0)
            GL11.glRotated(180.0, 0.0, 1.0, 0.0)
            if (CustomModel.colorM == CustomModel.Mode.Client) {
                resetColor()
                GradientUtil.applyGradientHorizontal(0f, 0f, 300f, 300f, 1f, c1, c2) {
                    setAlphaLimit(0f)
                    mc.textureManager.bindTexture(location)
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                }
            }
            else {
                GlStateManager.color(CustomModel.color.red / 255.0f, CustomModel.color.green / 255.0f, CustomModel.color.blue / 255.0f, 1.0f)
                mc.textureManager.bindTexture(location)
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
        p_177141_2_: Float,
        p_177141_3_: Float,
        partialTicks: Float,
        p_177141_5_: Float,
        p_177141_6_: Float,
        p_177141_7_: Float,
        scale: Float
    ) {
        // TODO Auto-generated method stub
    }
}