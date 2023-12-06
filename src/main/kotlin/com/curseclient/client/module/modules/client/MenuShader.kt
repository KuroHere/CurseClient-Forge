package com.curseclient.client.module.modules.client

import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.misc.NotificationType
import com.curseclient.client.utility.misc.NotificationUtils
import com.curseclient.client.utility.render.shader.Shaders
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.awt.Color

object MenuShader : Module(
    "MenuShader",
    "Potato pc killer",
    Category.CLIENT,
    enabledByDefault = true
) {

    var type by setting("Type", ShaderType.CyberPunk)
    private val fpsLimit by setting("Fps Limit", 60.0, 10.0,240.0, 10.0)

    enum class ShaderType {
        BlueGrid,
        BlueLandscape,
        Circuits,
        City,
        CubeCave,
        CyberPunk,
        DayNightSwitches,
        Green,
        Main,
        Matrix,
        Meteor,
        Mickey,
        Particle,
        PurpleNoise,
        Rainbow,
        RectWaves,
        RedLandscape,
        Tube,
        Starnest
    }
    var initTime: Long = 0x22

    private val timeUniform by lazy { Shaders.menuShader.getUniform("time") }
    private val mouseUniform by lazy { Shaders.menuShader.getUniform("mouse") }
    private val resolutionUniform by lazy { Shaders.menuShader.getUniform("resolution") }

    @JvmStatic
    fun handleGetLimitFramerate(cir: CallbackInfoReturnable<Int>) {
        if (mc.world == null && mc.currentScreen != null) {
            cir.returnValue = fpsLimit.toInt()
        }
    }

    override fun onEnable() {
        NotificationUtils.notify("MenuShader", "Require restart when change shader", NotificationType.ERROR, descriptionColor = Color.RED)

    }

    @JvmStatic
    fun draw() {
        val width = mc.displayWidth.toFloat()
        val height = mc.displayHeight.toFloat()
        val mouseX = Mouse.getX() - 1.0f
        val mouseY = height - Mouse.getY() - 1.0f

        with(Shaders.menuShader) {
            begin()

            uniformf(timeUniform, ((System.currentTimeMillis() - initTime) / 1000.0).toFloat())
            uniformf(mouseUniform, mouseX / width, (height - 1.0f - mouseY) / height)
            uniformf(resolutionUniform, width, height)

            val tessellator = Tessellator.getInstance()
            val buffer = tessellator.buffer

            with(buffer) {
                begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)

                pos(-1.0, -1.0, 0.0)
                endVertex()

                pos(1.0, -1.0, 0.0)
                endVertex()

                pos(1.0, 1.0, 0.0)
                endVertex()

                pos(-1.0, 1.0, 0.0)
                endVertex()
            }

            tessellator.draw()

            end()
        }
    }
}