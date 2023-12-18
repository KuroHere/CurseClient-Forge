package com.curseclient.client.utility.render

import baritone.api.utils.Helper.mc
import com.curseclient.client.utility.render.ColorUtils.glColor
import com.curseclient.client.utility.render.font.FontRenderer
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.graphic.GLUtils.draw
import com.curseclient.client.utility.render.graphic.GLUtils.glColor
import com.curseclient.client.utility.render.graphic.GLUtils.matrix
import com.curseclient.client.utility.render.graphic.GlStateUtils
import com.curseclient.client.utility.render.shader.GradientShader.finish
import com.curseclient.client.utility.render.shader.GradientShader.setup
import com.curseclient.client.utility.render.vector.Vec2d
import com.jhlabs.image.GaussianFilter
import com.sun.imageio.plugins.common.ImageUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.*


object RenderUtils2D {

    private val blank = ResourceLocation("textures/blank.png")
    private val blurCache = HashMap<BlurData, Int>()
    private val frustrum = Frustum()

    fun bind(resourceLocation: ResourceLocation) {
        mc.textureManager.bindTexture(resourceLocation)
    }

    private fun bindBlank() {
        bind(blank)
    }


    /*
     * Given to me by igs
     *
     */
    fun checkSetupFBO(framebuffer: Framebuffer?) {
        if (framebuffer != null) {
            if (framebuffer.depthBuffer > -1) {
                setupFBO(framebuffer)
                framebuffer.depthBuffer = -1
            }
        }
    }

    /**
     * @implNote Sets up the Framebuffer for Stencil use
     */
    fun setupFBO(framebuffer: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthBuffer)
        val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
    }

    /**
     * @implNote Initializes the Stencil Buffer to write to
     */
    fun initStencilToWrite() {
        //init
        mc.framebuffer.bindFramebuffer(false)
        checkSetupFBO(mc.framebuffer)
        glClear(GL_STENCIL_BUFFER_BIT)
        glEnable(GL_STENCIL_TEST)
        glStencilFunc(GL_ALWAYS, 1, 1)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glColorMask(false, false, false, false)
    }

    /**
     * @param ref (usually 1)
     * @implNote Reads the Stencil Buffer and stencils it onto everything until
     * @see StencilUtil.uninitStencilBuffer
     */
    fun readStencilBuffer(ref: Int) {
        glColorMask(true, true, true, true)
        glStencilFunc(GL_EQUAL, ref, 1)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
    }

    fun uninitStencilBuffer() {
        glDisable(GL_STENCIL_TEST)
    }

    /**
     * Starts scissoring a rect
     *
     * @param x The X coordinate of the scissored rect
     * @param y The Y coordinate of the scissored rect
     * @param width The width of the scissored rect
     * @param height The height of the scissored rect
     */
    fun pushScissor(x: Float, y: Float, width: Float, height: Float) {
        var shadowX = x.toInt()
        var shadowY = y.toInt()
        var shadowWidth = width.toInt()
        var shadowHeight = height.toInt()

        shadowWidth = shadowWidth.coerceAtLeast(0)
        shadowHeight = shadowHeight.coerceAtLeast(0)

        glPushAttrib(GL_SCISSOR_BIT)
        run {
            val sr = ScaledResolution(mc)
            val scale = sr.scaleFactor

            shadowY = sr.scaledHeight - shadowY
            shadowX *= scale
            shadowY *= scale
            shadowWidth *= scale
            shadowHeight *= scale

            glScissor(shadowX, (shadowY - shadowHeight), shadowWidth, shadowHeight)
            glEnable(GL_SCISSOR_TEST)
        }
    }

    /**
     * Stops scissoring a rect
     */
    fun popScissor() {
        glDisable(GL_SCISSOR_TEST)
        glPopAttrib()
    }

    fun glScissor(x: Float, y: Float, x1: Float, y1: Float, sr: ScaledResolution) {
        glScissor((x * sr.scaleFactor).toInt(), (Minecraft.getMinecraft().displayHeight - y1 * sr.scaleFactor).toInt(), ((x1 - x) * sr.scaleFactor).toInt(), ((y1 - y) * sr.scaleFactor).toInt())
    }

    fun rectGuiTexSmooth(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color?) {
        setup()
        bindBlank()
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glColor(color!!)
        glBegin(GL_POLYGON)

        val x1 = x.toDouble()
        val y1 = y.toDouble()
        val x2 = (x + width).toDouble()
        val y2 = (y + height).toDouble()

        val degree = Math.PI / 180

        // Draw the smooth rounded corners
        val segments = 90
        for (i in 0..segments) {
            val angle = (i * 90.0 / segments)

            glVertex2d(x2 - radius + sin(angle * degree) * radius, y2 - radius + cos(angle * degree) * radius)
        }

        for (i in 0..segments) {
            val angle = ((i + segments) * 90.0 / segments)

            glVertex2d(x2 - radius + sin(angle * degree) * radius, y1 + radius + cos(angle * degree) * radius)
        }

        for (i in 0..segments) {
            val angle = ((i + 2 * segments) * 90.0 / segments)

            glVertex2d(x1 + radius + sin(angle * degree) * radius, y1 + radius + cos(angle * degree) * radius)
        }

        for (i in 0..segments) {
            val angle = ((i + 3 * segments) * 90.0 / segments)

            glVertex2d(x1 + radius + sin(angle * degree) * radius, y2 - radius + cos(angle * degree) * radius)
        }

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
        finish()
    }


    fun isInViewFrustrum(entity: Entity): Boolean {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck
    }

    private fun isInViewFrustrum(bb: AxisAlignedBB): Boolean {
        val current: Entity? = mc.renderViewEntity
        if (current != null) {
            frustrum.setPosition(current.posX, current.posY, current.posZ)
        }
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    /**
     * Translates, scales, and rotates around a location
     *
     * @param location The location of the nametag
     * @param scaled Whether the nametag is scaled by distance
     * @param defaultScale The minimum scale of the nametag
     * @param block The code to run when drawing the nametag
     */
    @JvmStatic
    fun drawNametag(location: Vec3d, scaled: Boolean, defaultScale: Double = 0.2, block: () -> Unit) {
        val distance = mc.player.getDistance(location.x, location.y, location.z)

        var scale = defaultScale / 5

        if (scaled) {
            scale = max(defaultScale / 5, distance / 50) / 5
        }

        glPushMatrix()
        RenderHelper.enableStandardItemLighting()
        glDisable(GL_LIGHTING)

        GlStateManager.translate(
            location.x - mc.renderManager.viewerPosX,
            location.y - mc.renderManager.viewerPosY,
            location.z - mc.renderManager.viewerPosZ
        )

        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)

        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)

        // Rotate based on the view
        GlStateManager.rotate(
            (if (mc.gameSettings.thirdPersonView == 2) -1 else 1).toFloat() * mc.player.rotationPitch,
            1f,
            0f,
            0f
        )
        GlStateManager.scale(-scale, -scale, scale)

        glDisable(GL_DEPTH_TEST)

        block.invoke()

        glEnable(GL_DEPTH_TEST)
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        glPopMatrix()
    }

    /**
     * Scales whatever is currently being drawn
     *
     * @param x The X to scale from
     * @param y The Y to scale from
     * @param z The Z to scale from
     * @param scaleFacX How much to scale by on the X axis
     * @param scaleFacY How much to scale by on the Y axis
     * @param scaleFacZ How much to scale by on the Z axis
     * @param block The code to run during scaling
     */
    inline fun scaleTo(x: Float, y: Float, z: Float, scaleFacX: Double, scaleFacY: Double, scaleFacZ: Double, block: () -> Unit) {
        glPushMatrix()
        glTranslatef(x, y, z)
        glScaled(scaleFacX, scaleFacY, scaleFacZ)
        glTranslatef(-x, -y, -z)
        block()
        glPopMatrix()
    }

    fun drawItem(itemStack: ItemStack, x: Double, y: Double, text: String? = null, drawOverlay: Boolean = true, color: Color = Color.WHITE, scale: Float = 1f) {
        glPushMatrix()

        GlStateManager.enableBlend()
        GlStateManager.enableDepth()

        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.translate(x, y, 0.0)

        mc.renderItem.zLevel = 0.0f
        mc.renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0)
        if (drawOverlay)
            renderItemOverlayIntoGUI(FontRenderer, itemStack, 0, 0, text, color, scale)
        mc.renderItem.zLevel = 0.0f

        RenderHelper.disableStandardItemLighting()
        GlStateManager.translate(-x, -y, 0.0)

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.disableDepth()
        GlStateManager.enableTexture2D()
        glPopMatrix()
    }

    private fun renderItemOverlayIntoGUI(fr: FontRenderer, stack: ItemStack, xPosition: Int, yPosition: Int, text: String?, color: Color, scale: Float) {
        if (!stack.isEmpty) {
            if (stack.count != 1 || text != null) {
                val s = text ?: stack.count.toString()
                GlStateManager.disableLighting()
                GlStateManager.disableDepth()
                GlStateManager.disableBlend()
                fr.drawString(s, (xPosition + 19 - 2 - fr.getStringWidth(s, Fonts.DEFAULT)), (yPosition + 6 + 3).toFloat(), true, color, scale, Fonts.DEFAULT)
                GlStateManager.enableLighting()
                GlStateManager.enableDepth()
                // Fixes opaque cooldown overlay a bit lower
                // TODO: check if enabled blending still screws things up down the line.
                GlStateManager.enableBlend()
            }
        }
    }

    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        val alpha = (color shr 24 and 255).toFloat() / 255.0f
        val red = (color shr 16 and 255).toFloat() / 255.0f
        val green = (color shr 8 and 255).toFloat() / 255.0f
        val blue = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(left.toDouble(), bottom.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(right.toDouble(), bottom.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(right.toDouble(), top.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(left.toDouble(), top.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun drawRect(x: Float, y: Float, width: Float, height: Float, colour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        colour.glColor()

        glBegin(GL_QUADS)

        glVertex2f(x, y)
        glVertex2f(x, y + height)
        glVertex2f(x + width, y + height)
        glVertex2f(x + width, y)

        glEnd()

        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    fun drawBorder(x: Float, y: Float, width: Float, height: Float, border: Float, colour: Color) {
        drawRect(x - border, y - border, width + (border * 2f), border, colour)
        drawRect(x - border, y, border, height, colour)
        drawRect(x - border, y + height, width + (border * 2f), border, colour)
        drawRect(x + width, y, border, height, colour)
    }

    fun drawBorderedRect(x: Float, y: Float, endX: Float, endY: Float, lineWidth: Float, colorLine: Color, colorRect: Color) {
        drawRectOutline(x, y, endX, endY, colorLine, lineWidth)
        drawRectFilled(x, y, endX, endY, colorRect)
    }

    private fun drawRectOutline(x: Float, y: Float, endX: Float, endY: Float, color: Color, lineWidth: Float = 1F) {
        prepareGl()
        glLineWidth(lineWidth)
        VertexHelper.begin(GL_LINE_LOOP)

        VertexHelper.put(x.toDouble(), y.toDouble(), color)
        VertexHelper.put(endX.toDouble(), y.toDouble(), color)
        VertexHelper.put(endX.toDouble(), endY.toDouble(), color)
        VertexHelper.put(x.toDouble(), endY.toDouble(), color)

        VertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    private fun drawRectFilled(x: Float, y: Float, endX: Float, endY: Float, color: Color) {
        val pos1 = Vec2d(x.toDouble(), y.toDouble())
        val pos2 = Vec2d(endX.toDouble(), y.toDouble()) // Top right
        val pos3 = Vec2d(endX.toDouble(), endY.toDouble())
        val pos4 = Vec2d(x.toDouble(), endY.toDouble()) // Bottom left
        drawQuad(pos1, pos2, pos3, pos4, color)
    }

    private fun drawQuad(pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, pos4: Vec2d, color: Color) {
        val vertices = arrayOf(pos1, pos2, pos4, pos3)
        drawTriangleStrip(vertices, color)
    }

    private fun drawTriangleStrip(vertices: Array<Vec2d>, color: Color) {
        prepareGl()

        VertexHelper.begin(GL_TRIANGLE_STRIP)
        for (vertex in vertices) {
            VertexHelper.put(vertex, color)
        }
        VertexHelper.end()

        releaseGl()
    }

    private fun prepareGl() {
        GlStateUtils.texture2d(false)
        GlStateUtils.blend(true)
        GlStateUtils.smooth(true)
        GlStateUtils.lineSmooth(true)
        GlStateUtils.cull(false)
    }

    private fun releaseGl() {
        GlStateUtils.texture2d(true)
        GlStateUtils.smooth(false)
        GlStateUtils.lineSmooth(false)
        GlStateUtils.cull(true)
    }

    fun drawRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int) {
        drawRoundedRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    fun drawRoundedOutline(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        glColor4f(red, green, blue, alpha)
        glBegin(GL_LINE_LOOP)

        val degree = Math.PI / 180
        var i = 0.0

        // Draw the rounded corners
        while (i <= 90) {
            glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }

        while (i <= 180) {
            glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
            i += 1.0
        }

        while (i <= 270) {
            glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
            i += 1.0
        }

        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int, popPush: Boolean) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()
        if (popPush) glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)
        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }

    // rTL = radius top left, rTR = radius top right, rBR = radius bottom right, rBL = radius bottom left
    fun customRounded(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, rTL: Float, rTR: Float, rBR: Float, rBL: Float, color: Int) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val xTL = (paramXStart + rTL).toDouble()
        val yTL = (paramYStart + rTL).toDouble()
        val xTR = (paramXEnd - rTR).toDouble()
        val yTR = (paramYStart + rTR).toDouble()
        val xBR = (paramXEnd - rBR).toDouble()
        val yBR = (paramYEnd - rBR).toDouble()
        val xBL = (paramXStart + rBL).toDouble()
        val yBL = (paramYEnd - rBL).toDouble()
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1F)
        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)
        val degree = Math.PI / 180
        if (rBR <= 0) glVertex2d(xBR, yBR) else {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(xBR + sin(i * degree) * rBR, yBR + cos(i * degree) * rBR)
                i += 1.0
            }
        }
        if (rTR <= 0) glVertex2d(xTR, yTR) else {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(xTR + sin(i * degree) * rTR, yTR + cos(i * degree) * rTR)
                i += 1.0
            }
        }
        if (rTL <= 0) glVertex2d(xTL, yTL) else {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(xTL + sin(i * degree) * rTL, yTL + cos(i * degree) * rTL)
                i += 1.0
            }
        }
        if (rBL <= 0) glVertex2d(xBL, yBL) else {
            var i = 270.0
            while (i <= 360) {
                glVertex2d(xBL + sin(i * degree) * rBL, yBL + cos(i * degree) * rBL)
                i += 1.0
            }
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }


    fun originalRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }
        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }
        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(red, green, blue, alpha)
        builder.begin(GL_POLYGON, DefaultVertexFormats.POSITION)
        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                builder.pos(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                builder.pos(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                builder.pos(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            builder.pos(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()
            i += 1.0
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawRect(pos1: Vec2d, pos2: Vec2d, color: Color) {
        drawRect(Vec2f(pos1.x.toFloat(), pos1.y.toFloat()), Vec2f(pos2.x.toFloat(), pos2.y.toFloat()), color)
    }

    private fun drawRect(pos1: Vec2f, pos2: Vec2f, color: Color) {
        var x1 = pos1.x
        var y1 = pos1.y
        var x2 = pos2.x
        var y2 = pos2.y

        if (x1 < x2) {
            val i = x1
            x1 = x2
            x2 = i
        }
        if (y1 < y2) {
            val j = y1
            y1 = y2
            y2 = j
        }

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y1.toDouble())
        glVertex2d(x1.toDouble(), y1.toDouble())
        glVertex2d(x1.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        GlStateManager.resetColor()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawGradientRect(posBegin: Vec2d, posEnd: Vec2d, colorLeftTop: Color, colorRightTop: Color, colorLeftBottom: Color, colorRightBottom: Color) {
        drawGradientRect(posBegin.toVec2f(), posEnd.toVec2f(), colorLeftTop, colorRightTop, colorLeftBottom, colorRightBottom)
    }

    fun drawGradientRect(pos1: Vec2f, pos2: Vec2f, color1: Color, color2: Color, color3: Color, color4: Color) {
        val x1 = min(pos1.x, pos2.x)
        val y1 = min(pos1.y, pos2.y)
        val x2 = max(pos1.x, pos2.x)
        val y2 = max(pos1.y, pos2.y)

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glHint(3154, 4354)
        glHint(3155, 4354)

        glShadeModel(7425)
        glBegin(7)

        color1.glColor()
        glVertex2f(x1, y2)

        color2.glColor()
        glVertex2f(x2, y2)

        color3.glColor()
        glVertex2f(x2, y1)

        color4.glColor()
        glVertex2f(x1, y1)

        glEnd()
        glShadeModel(7424)

        glHint(3154, 4352)
        glHint(3155, 4352)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawPlayer(player: EntityPlayer, playerScale: Float, x: Float, y: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.shadeModel(7424)
        GlStateManager.enableAlpha()
        GlStateManager.enableDepth()
        GlStateManager.rotate(0.0f, 0.0f, 5.0f, 0.0f)
        GlStateManager.enableColorMaterial()
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 50.0f)
        GlStateManager.scale(-50.0f * playerScale, 50.0f * playerScale, 50.0f * playerScale)
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate((-atan((y / 40.0f).toDouble())).toFloat() * 20.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.translate(0.0f, 0.0f, 0.0f)
        val renderManager = mc.renderManager
        renderManager.setPlayerViewY(180.0f)
        renderManager.isRenderShadow = false
        try {
            renderManager.renderEntity(player, 0.0, 0.0, 0.0, 0.0f, 1.0f, false)
        } catch (ignored: Exception) {
        }
        renderManager.isRenderShadow = true
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.depthFunc(515)
        GlStateManager.resetColor()
        GlStateManager.disableDepth()
        GlStateManager.popMatrix()
    }

    fun drawTexture(x: Float, y: Float, textureX: Float, textureY: Float, width: Float, height: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex((textureX * f).toDouble(), ((textureY + height) * f1).toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(((textureX + width) * f).toDouble(), ((textureY + height) * f1).toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((textureX + width) * f).toDouble(), (textureY * f1).toDouble()).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).tex((textureX * f).toDouble(), (textureY * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawTexture(x: Float, y: Float, width: Float, height: Float, u: Float, v: Float, t: Float, s: Float) {
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX)
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(t.toDouble(), v.toDouble()).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).tex(u.toDouble(), v.toDouble()).endVertex()
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(u.toDouble(), s.toDouble()).endVertex()
        bufferbuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex(u.toDouble(), s.toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(t.toDouble(), s.toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(t.toDouble(), v.toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawImage(image: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
    }

    fun drawImage(posX: Float, posY: Float, width: Float, height: Float) {
        glPushMatrix()
        glTranslatef(posX, posY, 0.0f)
        glBegin(7)
        glTexCoord2f(0.0f, 0.0f)
        glVertex3f(0.0f, 0.0f, 0.0f)
        glTexCoord2f(0.0f, 1.0f)
        glVertex3f(0.0f, height, 0.0f)
        glTexCoord2f(1.0f, 1.0f)
        glVertex3f(width, height, 0.0f)
        glTexCoord2f(1.0f, 0.0f)
        glVertex3f(width, 0.0f, 0.0f)
        glEnd()
        glPopMatrix()
    }

    fun glColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255.0f
        val red = (hex shr 16 and 0xFF) / 255.0f
        val green = (hex shr 8 and 0xFF) / 255.0f
        val blue = (hex and 0xFF) / 255.0f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(redRGB: Int, greenRGB: Int, blueRGB: Int, alphaRGB: Int) {
        val red = 0.003921569f * redRGB
        val green = 0.003921569f * greenRGB
        val blue = 0.003921569f * blueRGB
        val alpha = 0.003921569f * alphaRGB
        GlStateManager.color(red, green, blue, alpha)
    }

    fun drawTriangle(x: Float, y: Float, size: Float, color: Int, blur: Boolean, blurRadius: Int, blurAlpha: Int) {
        if (blur)
            drawBlurredShadow((x - size * 0.85).toFloat(), y, ((x + size * 0.85) - (x - size * 0.85)).toFloat(), size, blurRadius, ColorUtils.injectAlpha(Color(color), blurAlpha));
        drawTriangle(x, y, size, color)
    }

    fun drawTriangle(x: Float, y: Float, size: Float, color: Int, rotateAngle: Float) {
        val blend = glIsEnabled(GL_BLEND)
        glEnable(GL_BLEND)
        val depth = glIsEnabled(GL_DEPTH_TEST)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glPushMatrix()
        glColor(color)
        glTranslatef(x, y, 0.0f)
        glRotatef(rotateAngle, 0.0f, 0.0f, 1.0f)
        glBegin(GL_TRIANGLE_FAN)
        glVertex2d(0.0, 0.0)
        glVertex2d(-size * 0.425, size * 0.75)
        glVertex2d(size * 0.425, size * 0.75)
        glEnd()
        glPopMatrix()
        glEnable(GL_TEXTURE_2D)
        if (!blend) glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (depth) glEnable(GL_DEPTH_TEST)
    }

    fun drawTriangle(x: Float, y: Float, size: Float, color: Int) {
        val blend = glIsEnabled(GL_BLEND)
        glEnable(GL_BLEND)
        val depth = glIsEnabled(GL_DEPTH_TEST)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glPushMatrix()
        glColor(color)
        glBegin(7)
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x - size * 0.85, (y + size).toDouble())
        glVertex2d(x.toDouble(), y + size - 3.0)
        glVertex2d(x.toDouble(), y.toDouble())
        glEnd()
        glBegin(7)
        glVertex2d(x.toDouble(), y.toDouble()) //top
        glVertex2d(x.toDouble(), y + size - 3.0) //midle
        glVertex2d(x + size * 0.85, (y + size).toDouble()) // left right
        glVertex2d(x.toDouble(), y.toDouble()) //top
        glEnd()
        glBegin(7)
        glVertex2d(x - size * 0.85, (y + size).toDouble())
        glVertex2d(x + size * 0.85, (y + size).toDouble()) // left right
        glVertex2d(x.toDouble(), y + size - 3.0) //midle
        glVertex2d(x - size * 0.85, (y + size).toDouble())
        glEnd()
        glPopMatrix()
        glEnable(GL_TEXTURE_2D)
        if (!blend) glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (depth) glEnable(GL_DEPTH_TEST)
    }

    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, thickness: Float, hex: Int) {
        val red = (hex shr 16 and 0xFF) / 255.0f
        val green = (hex shr 8 and 0xFF) / 255.0f
        val blue = (hex and 0xFF) / 255.0f
        val alpha = (hex shr 24 and 0xFF) / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.shadeModel(GL_SMOOTH)
        glLineWidth(thickness)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x1.toDouble(), y1.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(GL_FLAT)
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }


    fun drawLine(start: Vec2d, end: Vec2d, width: Float, color: Color) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        glVertex2d(start.x, start.y)
        glVertex2d(end.x, end.y)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun drawBlurredRect(posBegin: Vec2d, posEnd: Vec2d, blurRadius: Int, color: Color){
        val x = min(posBegin.x, posEnd.x).toFloat()
        val y = min(posBegin.y, posEnd.y).toFloat()
        val width = max(posBegin.x, posEnd.x).toFloat() - x
        val height = max(posBegin.y, posEnd.y).toFloat() - y

        drawBlurredShadow(x, y, width, height, blurRadius, color)
    }

    data class BlurData(val width: Float, val height: Float, val blurRadius: Int){
        override fun equals(other: Any?): Boolean {
            if (other !is BlurData) return false

            return width == other.width &&
                height == other.height &&
                blurRadius == other.blurRadius
        }

        override fun hashCode(): Int {
            var result = width.hashCode()
            result = 31 * result + height.hashCode()
            result = 31 * result + blurRadius
            return result
        }
    }

    fun drawBlurredShadow(xIn: Float, yIn: Float, widthIn: Float, heightIn: Float, blurRadiusIn: Int, colorIn: Color) {
        matrix {
            GlStateManager.alphaFunc(GL_GREATER, 0.01f)

            val x = xIn - blurRadiusIn
            val y = yIn - blurRadiusIn

            val width = widthIn + blurRadiusIn * 2
            val height = heightIn + blurRadiusIn * 2

            val id = BlurData(widthIn, heightIn, blurRadiusIn)

            GlStateManager.enableTexture2D()
            glDisable(GL_CULL_FACE)
            glDisable(GL_ALPHA_TEST)
            GlStateManager.enableBlend()

            val texture = blurCache[id] ?: run {
                val w = max(1, width.toInt())
                val h = max(1, height.toInt())
                val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE)

                image.graphics.apply {
                    color = Color.WHITE
                    fillRect(blurRadiusIn, blurRadiusIn, (w - blurRadiusIn * 2), (h - blurRadiusIn * 2))
                    dispose()
                }

                val blurred = GaussianFilter(blurRadiusIn.toFloat()).filter(image, null)
                val textureId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false)
                blurCache[id] = textureId

                textureId
            }

            GlStateManager.bindTexture(texture)

            colorIn.glColor()

            draw(GL_QUADS) {
                glTexCoord2f(0f, 0f) // top left
                glVertex2f(x, y)

                glTexCoord2f(0f, 1f) // bottom left
                glVertex2f(x, y + height)

                glTexCoord2f(1f, 1f) // bottom right
                glVertex2f(x + width, y + height)

                glTexCoord2f(1f, 0f) // top right
                glVertex2f(x + width, y)
            }

            GlStateManager.disableBlend()
            GlStateManager.resetColor()

            glEnable(GL_CULL_FACE)
        }
    }

    fun drawBlurredShadow2(x: Float, y: Float, width: Float, height: Float, blurRadius: Int, color: Color) {
        glAlphaFunc(GL_GREATER, 0.01f)

        val newWidth = width + blurRadius * 2
        val newHeight = height + blurRadius * 2
        val newX = x - blurRadius
        val newY = y - blurRadius

        val _X = newX - 0.25f
        val _Y = newY + 0.25f

        val identifier = BlurData(newWidth, newHeight, blurRadius)

        val text2d = glIsEnabled(GL_TEXTURE_2D)
        val cface = glIsEnabled(GL_CULL_FACE)
        val atest = glIsEnabled(GL_ALPHA_TEST)
        val blend = glIsEnabled(GL_BLEND)

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)

        var texId = -1
        if (blurCache.containsKey(identifier)) {
            texId = blurCache[identifier]!!
            glBindTexture(GL_TEXTURE_2D, texId)
        } else {
            val original = BufferedImage(newWidth.toInt(), newHeight.toInt(), BufferedImage.TYPE_INT_ARGB)

            val g = original.graphics
            g.color = color
            g.fillRect(blurRadius, blurRadius, (newWidth - blurRadius * 2).toInt(), (newHeight - blurRadius * 2).toInt())
            g.dispose()

            val op = GaussianFilter(blurRadius.toFloat())
            val blurred = op.filter(original, null)

            texId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false)
            blurCache[identifier] = texId
        }

        glColor4f(1f, 1f, 1f, 1f)
        glBegin(GL_QUADS)
        glTexCoord2f(0f, 0f)
        glVertex2f(_X, _Y)
        glTexCoord2f(0f, 1f)
        glVertex2f(_X, _Y + newHeight)
        glTexCoord2f(1f, 1f)
        glVertex2f(_X + newWidth, _Y + newHeight)
        glTexCoord2f(1f, 0f)
        glVertex2f(_X + newWidth, _Y)
        glEnd()

        GlStateManager.resetColor()
        if (!blend)
            glDisable(GL_BLEND)
        if (!atest)
            glDisable(GL_ALPHA_TEST)
        if (!cface)
            glEnable(GL_CULL_FACE)
        if (!text2d)
            glDisable(GL_TEXTURE_2D)
    }

    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer? {
        return createFrameBuffer(framebuffer, false)
    }

    fun createFrameBuffer(framebuffer: Framebuffer?, depth: Boolean): Framebuffer? {
        if (needsNewFramebuffer(framebuffer)) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, depth)
        }
        return framebuffer
    }

    private fun needsNewFramebuffer(framebuffer: Framebuffer?): Boolean {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight
    }
}
