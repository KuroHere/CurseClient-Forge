package com.curseclient.client.gui.impl.mainmenu.elements.alt

import baritone.api.utils.Helper.mc
import com.curseclient.CurseClient
import com.curseclient.client.manager.managers.data.DataManager
import com.curseclient.client.utility.render.font.FontUtils.drawCentreString
import com.curseclient.client.utility.render.font.FontUtils.drawString
import com.curseclient.client.utility.render.font.Fonts
import com.curseclient.client.utility.render.shader.RectBuilder
import com.curseclient.client.utility.render.shader.RoundedUtil.drawRoundTextured
import com.curseclient.client.utility.render.shader.RoundedUtil.endBlend
import com.curseclient.client.utility.render.shader.RoundedUtil.startBlend
import com.curseclient.client.utility.render.vector.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Session
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushMatrix
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.lang.reflect.Field
import java.net.URL
import java.net.URLConnection
import javax.imageio.ImageIO

// Lazy to fix (～￣▽￣)～
class AltGui(private val posX: Int, private val posY: Int, private val name: String) {

    private var head: ResourceLocation? = null
    private val crackedSkin = ResourceLocation("textures/icons/steve.png")

    private val altManager = AltManager()

    init {
        head = Account.getTexture(name, "png")
    }

    companion object {
        fun login(string: String) {
            try {
                val field = Minecraft::class.java.getDeclaredField("field_71449_j") //session
                field.isAccessible = true
                val field2 = Field::class.java.getDeclaredField("modifiers")
                field2.isAccessible = true
                field2.setInt(field, field.modifiers and 0xFFFFFFEF.toInt())
                field.set(mc, Session(string, "", "", "mojang"))
                println("logged in $string")
            } catch (e: Exception) {
                println("Alt manager error!")
                e.printStackTrace()
            }
        }

        fun saveUserAvatar(s: String, nickname: String) {
            try {
                val url = URL(s)
                val openConnection: URLConnection = url.openConnection()
                var check = true

                try {
                    openConnection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
                    )
                    openConnection.connect()

                    if (openConnection.contentLength > 8000000) {
                        println("File size is too big.")
                        check = false
                    }
                } catch (e: Exception) {
                    println("Couldn't create a connection to the link, please recheck the link.")
                    check = false
                    e.printStackTrace()
                }

                if (check) {
                    var img: BufferedImage? = null
                    try {
                        val input: InputStream = BufferedInputStream(openConnection.getInputStream())
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(1024)
                        val n = 0
                        while (-1 != input.read(buffer)) {
                            output.write(buffer, 0, n)
                        }
                        output.close()
                        input.close()
                        val response: ByteArray = output.toByteArray()
                        img = ImageIO.read(ByteArrayInputStream(response))
                    } catch (e: Exception) {
                        println("Couldn't read an image from this link.")
                        e.printStackTrace()
                    }

                    try {
                        ImageIO.write(img, "png", File(CurseClient.DIR, "/heads/$nickname.png"))
                    } catch (e: IOException) {
                        println("Couldn't create/send the output image.")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun render(mouseX: Int, mouseY: Int) {
        val color = Color(0x62000000)
        val selectedColor = Color(0x811D7201.toInt(), true)

        RectBuilder(Vec2d(posX, posY), Vec2d(posX + 210, posY + 100)).apply {
            outlineColor(Color.WHITE)
            width(1.0)
            color(if (mc.session.username == name) selectedColor else color)
            radius(8.0)
            draw()
        }

        renderHead((posX + 5).toDouble(), (posY + 5).toDouble())
        Fonts.DEFAULT_BOLD.drawString(name, Vec2d(posX + 38, posY + 7), false, color = if (mc.session.username == name) Color(-1) else Color(0x7A7A7A))

        RectBuilder(Vec2d(posX + 5.5, posY + 80.0), Vec2d(posX + 40.5, posY + 92.0)).apply {
            color(if (isHovering(mouseX, mouseY, 165, 200, 5, 17)) Color(0x8104F839.toInt(), true) else Color(0x813EFF00.toInt(), true))
            radius(3.0)
            draw()
        }
        Fonts.DEFAULT.drawCentreString(
            "Login",
            Vec2d(posX + 22.5, posY + 85.0),
            color = if (isHovering(mouseX, mouseY, 5, 40, 80, 92)) Color(0x7A7A7A) else Color(-1)
        )

        RectBuilder(Vec2d(posX + 165.0, posY + 80.0), Vec2d(posX + 200.0, posY + 92.0)).apply {
            color(if (isHovering(mouseX, mouseY, 165, 200, 22, 34)) Color(0x81FF0000.toInt(), true) else Color(0x81F60202.toInt(), true))
            radius(3.0)
            draw()
        }
        Fonts.DEFAULT.drawCentreString(
            "Delete",
            Vec2d(posX + 182.5, posY + 85.0),
            color = if (isHovering(mouseX, mouseY, 165, 200, 80, 92)) Color(0x7A7A7A) else Color(-1)
        )

        if (Mouse.isButtonDown(0)) {
            mouseClicked(mouseX, mouseY, 0)
        }
    }

    private fun isHovering(x: Int, y: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Boolean {
        return x >= posX + minX && x <= posX + maxX && y >= posY + minY && y <= posY + maxY
    }

    private fun mouseClicked(x: Int, y: Int, button: Int) {
        if (!altManager.clickTimer.passed(500.0)) {
            return
        }
        when {
            isHovering(x, y, 165, 200, 5, 17) -> {
                login(name)
                altManager.clickTimer.reset()
            }
            isHovering(x, y, 165, 200, 22, 34) -> {
                DataManager.alts.remove(Account(name))
                altManager.clickTimer.reset()
            }
        }
    }

    private fun renderHead(x: Double, y: Double) {
        if (head != null) {
            mc.textureManager.bindTexture(head!!)
        } else {
            mc.textureManager.bindTexture(crackedSkin)
        }
        startBlend()
        glPushMatrix()
        drawRoundTextured(x.toFloat(), y.toFloat(), 30F, 30F, 5F, 1F)
        endBlend()
        glPopMatrix()
    }
}