package com.curseclient.client.utility.render.shader

import com.curseclient.client.module.modules.client.MenuShader
import java.util.*

object Shaders {
    private const val VERTEX_SHADER = "/assets/shaders/vertex.vsh"
    private const val VERTEX_SHADER_GUI = "/assets/shaders/vertexGui.vsh"


    val menuShader = FragmentShader("/assets/shaders/menu/" + MenuShader.type.toString() + ".fsh", VERTEX_SHADER)
    val rectShader = FragmentShader("/assets/shaders/gui/rect.frag", VERTEX_SHADER_GUI)
}