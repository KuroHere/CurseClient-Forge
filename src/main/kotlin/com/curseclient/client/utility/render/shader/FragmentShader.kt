package com.curseclient.client.utility.render.shader

import com.curseclient.CurseClient
import com.curseclient.client.utility.render.ColorUtils.a
import com.curseclient.client.utility.render.ColorUtils.b
import com.curseclient.client.utility.render.ColorUtils.g
import com.curseclient.client.utility.render.ColorUtils.r
import net.minecraft.client.renderer.OpenGlHelper
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color

class FragmentShader(fragment: String, vertex: String) {
    private val id: Int

    fun begin() = GL20.glUseProgram(id)
    fun end() = GL20.glUseProgram(0)

    fun render(x: Float, y: Float, width: Float, height: Float) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0.0f, 0.0f)
        GL11.glVertex2f(x, y)
        GL11.glTexCoord2f(0.0f, 1.0f)
        GL11.glVertex2f(x, (y + height))
        GL11.glTexCoord2f(1.0f, 1.0f)
        GL11.glVertex2f((x + width), (y + height))
        GL11.glTexCoord2f(1.0f, 0.0f)
        GL11.glVertex2f((x + width), y)
        GL11.glEnd()
    }

    fun uniformf(loc: Int, vararg args: Float) {
        when (args.size) {
            1 -> GL20.glUniform1f(loc, args[0])
            2 -> GL20.glUniform2f(loc, args[0], args[1])
            3 -> GL20.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL20.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun colorUniform(loc: Int, color: Color) =
        uniformf(loc, color.r, color.g, color.b, color.a)

    fun getUniform(name: String) =
        GL20.glGetUniformLocation(id, name)

    private fun createShader(path: String, type: Int): Int {
        val srcString = javaClass.getResourceAsStream(path)!!.readBytes().decodeToString()
        val id = GL20.glCreateShader(type)

        GL20.glShaderSource(id, srcString)
        GL20.glCompileShader(id)

        val compiled = OpenGlHelper.glGetShaderi(id, GL20.GL_COMPILE_STATUS)
        if (compiled != 0) return id

        CurseClient.LOG.error("Failed to compile shader!")
        CurseClient.LOG.error("Shader: $path")
        CurseClient.LOG.error("GL Output:")
        CurseClient.LOG.error(GL20.glGetShaderInfoLog(id, 1024))
        GL20.glDeleteShader(id)
        throw IllegalStateException("Failed to compile shader: $path")
    }

    init {
        val vertexShaderID = createShader(vertex, GL20.GL_VERTEX_SHADER)
        val fragmentShaderID = createShader(fragment, GL20.GL_FRAGMENT_SHADER)

        val program = GL20.glCreateProgram()
        id = program

        GL20.glAttachShader(id, vertexShaderID)
        GL20.glAttachShader(id, fragmentShaderID)
        GL20.glLinkProgram(id)

        val linked = GL20.glGetProgrami(id, GL20.GL_LINK_STATUS)
        if (linked == 0) {
            CurseClient.LOG.error(GL20.glGetProgramInfoLog(id, 1024))
            GL20.glDeleteProgram(id)
            throw IllegalStateException("Shader failed to link")
        }

        GL20.glDetachShader(id, vertexShaderID)
        GL20.glDetachShader(id, fragmentShaderID)
        GL20.glDeleteShader(vertexShaderID)
        GL20.glDeleteShader(fragmentShaderID)
    }
}