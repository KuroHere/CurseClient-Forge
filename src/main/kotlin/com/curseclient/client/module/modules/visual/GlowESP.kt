import baritone.api.utils.Helper
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
import com.curseclient.client.utility.render.shader.ShaderUtils
import com.curseclient.client.utility.render.shader.ShaderUtils.drawQuads
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
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
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL13
import java.awt.Color
import java.nio.FloatBuffer
import java.util.*


object GlowESP : Module(
    "GlowESP",
    "Glow? do u like it",
    Category.VISUAL
) {

    val radius by setting("Radius", 2, 1, 30, 1)
    val exposure by setting("Exposure", 2.2, 1.0, 3.5, 0.1)
    val separate by setting("Seperate Texture", false)

    val color by setting("Color", Color(70, 100, 255))
    val movingcolors by setting("movingcolors", false)
    val sync by setting("Sync", false, visible = { movingcolors } )
    val color1 by setting("Color1", Color(200, 0, 0), visible = { movingcolors && !sync})
    val color2 by setting("Color2", Color(255, 255, 255), visible = { movingcolors && !sync})
    val hueInterpolation by setting("hueInterpolation", false)

    val players by setting("Players", false)
    val animals by setting("Animals", false)
    val mobs by setting("Mobs", false)
    val items by setting("Items", false)
    val crystal by setting("Crystal", false)

    private var renderNameTags = true
    private lateinit var fadeIn: Animation

    private val outlineShader = ShaderUtils("shaders/client/outline.frag")
    private val glowShader = ShaderUtils("shaders/client/glow.frag")

    var framebuffer: Framebuffer? = null
    var outlineFrameBuffer: Framebuffer? = null
    var glowFrameBuffer: Framebuffer? = null
    private val frustum2 = Frustum()

    private val entities = ArrayList<Entity>()

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
            Helper.mc.framebuffer.bindFramebuffer(true)
            GlStateManager.disableLighting()
        }
        safeListener<Render2DEvent> {
            val sr = ScaledResolution(mc)
            if (framebuffer != null && outlineFrameBuffer != null && entities.size > 0) {
                GlStateManager.enableAlpha()
                GlStateManager.alphaFunc(516, 0.0f)
                GlStateManager.enableBlend()
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

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

    private fun setupGlowUniforms(dir1: Float, dir2: Float) {
        val color = GETTHISCOLORFUCKKOTLIN()
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
        val color = GETTHISCOLORFUCKKOTLIN()
        outlineShader.setUniformi("texture", 0)
        outlineShader.setUniformf("radius", (radius / 1.5f).toFloat())
        outlineShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        outlineShader.setUniformf("direction", dir1, dir2)
        outlineShader.setUniformf("color", color.r, color.g, color.b)
    }

    private fun renderEntities(ticks: Float) {
        entities.forEach { entity ->
            renderNameTags = false
            mc.renderManager.renderEntityStatic(entity, ticks, false)
            renderNameTags = true
        }
    }

    private fun GETTHISCOLORFUCKKOTLIN(): Color {

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