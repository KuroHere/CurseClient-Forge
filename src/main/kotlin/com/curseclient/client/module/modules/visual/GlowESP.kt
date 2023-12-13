import baritone.api.utils.Helper
import com.curseclient.CurseClient
import com.curseclient.client.event.events.render.Render2DEvent
import com.curseclient.client.event.events.render.Render3DEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.HUD
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.math.MathUtils
import com.curseclient.client.utility.render.ColorUtils
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import com.curseclient.client.utility.render.animation.Animation
import com.curseclient.client.utility.render.animation.DecelerateAnimation
import com.curseclient.client.utility.render.shader.RoundedUtil.setAlphaLimit
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.shader.ShaderUtils
import com.curseclient.client.utility.render.shader.ShaderUtils.drawQuads
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.OpenGlHelper.glUniform1
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.pow


object GlowESP : Module(
    "GlowESP",
    "Glow? do u like it",
    Category.VISUAL
) {

    private val glowMode by setting("GlowMode", GlowMode.Glow)
    private val radius by setting("Radius", 2, 1, 30, 1)
    private val exposure by setting("Exposure", 2.2, 1.0, 3.5, 0.1)
    private val offsetSetting by setting("Offset", 4, 2, 10, 1, { glowMode == GlowMode.Bloom})
    private val separate by setting("Seperate Texture", false)

    private val color by setting("Color", Color(70, 100, 255))
    private val movingcolors by setting("movingcolors", false)
    private val sync by setting("Sync", false, visible = { movingcolors } )
    private val color1 by setting("Color1", Color(200, 0, 0), visible = { movingcolors && !sync})
    private val color2 by setting("Color2", Color(255, 255, 255), visible = { movingcolors && !sync})
    private val hueInterpolation by setting("hueInterpolation", false)

    private val players by setting("Players", false)
    private val animals by setting("Animals", false)
    private val mobs by setting("Mobs", false)
    private val items by setting("Items", false)
    private val crystal by setting("Crystal", false)

    private var renderNameTags = true
    private lateinit var fadeIn: Animation

    private val outlineShader = ShaderUtils("shaders/client/outline.frag")
    private val glowShader = ShaderUtils("shaders/client/glow.frag")

    private val kawaseGlowShader: ShaderUtils = ShaderUtils("kawaseDownBloom")
    private val kawaseGlowShader2: ShaderUtils = ShaderUtils("kawaseUpGlow")
    private var currentIterations = 0
    private val framebufferList: MutableList<Framebuffer> = ArrayList()
    var framebuffer: Framebuffer? = null
    private var outlineFrameBuffer: Framebuffer? = null
    private var glowFrameBuffer: Framebuffer? = null
    private val frustum2 = Frustum()

    private val entities = ArrayList<Entity>()

    private enum class GlowMode {
        Glow,
        Bloom
    }

    override fun onEnable() {
        super.onEnable()
        fadeIn = DecelerateAnimation(250, 1.0)
    }

    private fun createFrameBuffers() {
        framebuffer = createFrameBuffer(framebuffer)
        outlineFrameBuffer = createFrameBuffer(outlineFrameBuffer)
        glowFrameBuffer = createFrameBuffer(glowFrameBuffer)
    }

    init {
        safeListener<Render3DEvent> { event ->
            createFrameBuffers()
            collectEntities()
            framebuffer?.let {
                it.framebufferClear()
                it.bindFramebuffer(true)
                renderEntities(event.partialTicks)
                it.unbindFramebuffer()
            }
            mc.framebuffer.bindFramebuffer(true)
            GlStateManager.disableLighting()
        }
        safeListener<Render2DEvent> {
            ScaledResolution(mc)
            if (framebuffer != null && outlineFrameBuffer != null && entities.size > 0) {
                when (glowMode) {
                    GlowMode.Glow -> {
                        GlStateManager.enableAlpha()
                        GlStateManager.alphaFunc(516, 0.0f)
                        GlStateManager.enableBlend()
                        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

                        outlineFrameBuffer?.let {
                            it.framebufferClear()
                            it.bindFramebuffer(true)
                            outlineShader.init()
                            setupOutlineUniforms(0F, 1F)
                            bindTexture(framebuffer!!.framebufferTexture)
                            drawQuads()
                            outlineShader.init()
                            setupOutlineUniforms(1F, 0F)
                            bindTexture(framebuffer!!.framebufferTexture)
                            drawQuads()
                            outlineShader.unload()
                            it.unbindFramebuffer()
                        }
                        GlStateManager.color(1F, 1F, 1F, 1F)
                        glowFrameBuffer?.let {
                            it.framebufferClear()
                            it.bindFramebuffer(true)
                            glowShader.init()
                            setupGlowUniforms(1F, 0F)
                            bindTexture(outlineFrameBuffer!!.framebufferTexture)
                            drawQuads()
                            glowShader.unload()
                            it.unbindFramebuffer()
                        }

                        mc.framebuffer.bindFramebuffer(true)
                        glowShader.init()
                        setupGlowUniforms(0F, 1F)
                        if (separate) {
                            GL13.glActiveTexture(GL13.GL_TEXTURE16)
                            bindTexture(framebuffer!!.framebufferTexture)
                        }
                        GL13.glActiveTexture(GL13.GL_TEXTURE0)
                        bindTexture(glowFrameBuffer!!.framebufferTexture)
                        drawQuads()
                        glowShader.unload()
                    }

                    GlowMode.Bloom -> {
                        setAlphaLimit(0f)
                        startBlend()
                        /*RenderUtil.bindTexture(framebuffer.framebufferTexture)
                          ShaderUtil.drawQuads()
                          framebuffer.framebufferClear()
                          mc.getFramebuffer().bindFramebuffer(false)
                          if(true) return*/

                        outlineFrameBuffer!!.framebufferClear()
                        outlineFrameBuffer!!.bindFramebuffer(false)
                        outlineShader.init()
                        setupOutlineUniforms(0f, 1f)
                        bindTexture(framebuffer!!.framebufferTexture)
                        drawQuads()
                        outlineShader.init()
                        setupOutlineUniforms(1f, 0f)
                        bindTexture(framebuffer!!.framebufferTexture)
                        drawQuads()
                        outlineShader.unload()
                        outlineFrameBuffer!!.unbindFramebuffer()

                        val offset = offsetSetting.toFloat()
                        val iterations = 3

                        if (framebufferList.isEmpty() || currentIterations != iterations || (framebuffer!!.framebufferWidth != mc.displayWidth || framebuffer!!.framebufferHeight != mc.displayHeight)) {
                            initFramebuffers(iterations.toFloat())
                            currentIterations = iterations
                        }
                        setAlphaLimit(0f)

                        glBlendFunc(GL_ONE, GL_ONE)

                        glClearColor(0f, 0f, 0f, 0f)
                        renderFBO(framebufferList[1], outlineFrameBuffer!!.framebufferTexture, kawaseGlowShader, offset)

                        // Downsample
                        for (i in 1 until iterations) {
                            renderFBO(framebufferList[i + 1], framebufferList[i].framebufferTexture, kawaseGlowShader, offset)
                        }

                        // Upsample
                        for (i in iterations downTo 2) {
                            renderFBO(framebufferList[i - 1], framebufferList[i].framebufferTexture, kawaseGlowShader2, offset)
                        }

                        val lastBuffer = framebufferList[0]
                        lastBuffer.framebufferClear()
                        lastBuffer.bindFramebuffer(false)
                        kawaseGlowShader2.init()
                        kawaseGlowShader2.setUniformf("offset", offset.toFloat(), offset.toFloat())
                        kawaseGlowShader2.setUniformi("inTexture", 0)
                        kawaseGlowShader2.setUniformi("check", if (separate) 1 else 0)
                        kawaseGlowShader2.setUniformf("lastPass", 1f)
                        kawaseGlowShader2.setUniformf("exposure", (exposure.toFloat() * fadeIn.output.toFloat()))
                        kawaseGlowShader2.setUniformi("textureToCheck", 16)
                        kawaseGlowShader2.setUniformf("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight)
                        kawaseGlowShader2.setUniformf("iResolution", lastBuffer.framebufferWidth.toFloat(), lastBuffer.framebufferHeight.toFloat())
                        GL13.glActiveTexture(GL13.GL_TEXTURE16)
                        bindTexture(framebuffer!!.framebufferTexture)
                        GL13.glActiveTexture(GL13.GL_TEXTURE0)
                        bindTexture(framebufferList[1].framebufferTexture)

                        drawQuads()
                        kawaseGlowShader2.unload()

                        glClearColor(0f, 0f, 0f, 0f)
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                        framebuffer!!.framebufferClear()
                        resetColor()
                        mc.framebuffer.bindFramebuffer(true)
                        bindTexture(framebufferList[0].framebufferTexture)
                        drawQuads()
                        setAlphaLimit(0f)
                        GlStateManager.bindTexture(0)
                    }
                }
            }
        }
    }

    private fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }

    fun bindTexture(texture: Int) {
        glBindTexture(GL_TEXTURE_2D, texture)
    }

    private fun initFramebuffers(iterations: Float) {
        for (framebuffer in framebufferList) {
            framebuffer.deleteFramebuffer()
        }
        framebufferList.clear()

        //Have to make the framebuffer null so that it does not try to delete a framebuffer that has already been deleted
        framebufferList.add(createFrameBuffer(null).also { glowFrameBuffer = it })
        var i = 1
        while (i <= iterations) {
            val currentBuffer = Framebuffer((Helper.mc.displayWidth / 2.0.pow(i.toDouble())).toInt(), (Helper.mc.displayHeight / 2.0.pow(i.toDouble())).toInt(), true)
            currentBuffer.setFramebufferFilter(GL_LINEAR)
            GlStateManager.bindTexture(currentBuffer.framebufferTexture)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT)
            GlStateManager.bindTexture(0)
            framebufferList.add(currentBuffer)
            i++
        }
    }

    private fun renderFBO(framebuffer: Framebuffer, framebufferTexture: Int, shader: ShaderUtils, offset: Float) {
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(false)
        shader.init()
        bindTexture(framebufferTexture)
        shader.setUniformf("offset", offset, offset)
        shader.setUniformi("inTexture", 0)
        shader.setUniformi("check", 0)
        shader.setUniformf("lastPass", 0F)
        shader.setUniformf("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight)
        shader.setUniformf("iResolution", framebuffer.framebufferWidth.toFloat(), framebuffer.framebufferHeight.toFloat())
        drawQuads()
        shader.unload()
    }

    private fun setupGlowUniforms(dir1: Float, dir2: Float) {
        val color = getGlowColor()
        glowShader.setUniformi("texture", 0)
        if (separate) {
            glowShader.setUniformi("textureToCheck", 16)
        }
        glowShader.setUniformf("radius", radius.toFloat())
        glowShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        glowShader.setUniformf("direction", dir1, dir2)
        glowShader.setUniformf("color", color.r, color.g, color.b)
        glowShader.setUniformf("exposure", (exposure * fadeIn.output).toFloat())
        glowShader.setUniformi("avoidTexture", if (separate) 1 else 0)

        val buffer: FloatBuffer = BufferUtils.createFloatBuffer(256)
        for (i in 1..radius.toInt()) {
            buffer.put(MathUtils.calculateGaussianValue(i.toFloat(), (radius / 2).toFloat()))
        }
        buffer.rewind()

        glUniform1(glowShader.getUniform("weights"), buffer)
    }

    private fun setupOutlineUniforms(dir1: Float, dir2: Float) {
        val color = getGlowColor()
        outlineShader.setUniformi("texture", 0)
        outlineShader.setUniformf("radius", (radius / 1.5f).toFloat())
        outlineShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        outlineShader.setUniformf("direction", dir1, dir2)
        outlineShader.setUniformf("color", color.r, color.g, color.b)
    }

    private fun renderEntities(ticks: Float) {
        entities.forEach { entity ->
            try {
                renderNameTags = false
                mc.renderManager.renderEntityStatic(entity, ticks, false)
                renderNameTags = true
            } catch (e: Exception) {
                CurseClient.LOG.debug("Crash rồi nhớ gửi crash log cho Kuro_Here nhé")
                e.printStackTrace()
            }
        }
    }

    private fun getGlowColor(): Color {

        val c1 = HUD.getColor(0)
        val c2 = HUD.getColor(10)
        return if (!movingcolors && !sync) {
            Color(color.r, color.g, color.b)
        }
        else if (movingcolors && sync) {
            ColorUtils.interpolateColorsBackAndForth(15, 0, c1, c2, hueInterpolation)
        }
        else
            ColorUtils.interpolateColorsBackAndForth(15, 0, color1, color2, hueInterpolation)
    }

    private fun isInView(ent: Entity): Boolean {
        frustum2.setPosition(
            mc.renderViewEntity!!.posX,
            mc.renderViewEntity!!.posY,
            mc.renderViewEntity!!.posZ)
        return frustum2.isBoundingBoxInFrustum(ent.entityBoundingBox) || ent.ignoreFrustumCheck
    }

    private fun collectEntities() {
        entities.clear()
        for (entity in mc.world.loadedEntityList) {
            if (!isInView(entity)) continue
            if (entity == mc.player && mc.gameSettings.thirdPersonView == 0) continue
            if (entity is EntityItem && items) {
                entities.add(entity)
            }
            if (entity is EntityEnderCrystal && crystal) {
                entities.add(entity)
            }
            if (entity is EntityAnimal && animals) {
                entities.add(entity)
            }
            if (entity is EntityPlayer && players) {
                entities.add(entity)
            }
            if (entity is EntityMob && mobs) {
                entities.add(entity)
            }
        }
    }
}