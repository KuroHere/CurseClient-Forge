package com.curseclient.client.utility.render.shader

import com.curseclient.client.module.impls.client.MenuShader
import com.curseclient.client.utility.math.MathUtils

object Shaders {
    private const val VERTEX_SHADER = "/assets/shaders/vertex.vsh"
    private const val VERTEX_SHADER_GUI = "/assets/shaders/vertexGui.vsh"

    private val shaderNames = mapOf(
        1 to "BlueGrid",
        2 to "BlueLandscape",
        3 to "Circuits",
        4 to "City",
        5 to "CubeCave",
        6 to "CyberPunk",
        7 to "Green",
        8 to "Magic",
        9 to "Main",
        10 to "Matrix",
        11 to "Particle",
        12 to "PurpleNoise",
        13 to "Rainbow",
        14 to "RectWaves",
        15 to "RedLandscape",
        16 to "Starguy",
        17 to "Starnest",
        18 to "Tube",
        19 to "Cloud",
        20 to "DropofDistortion",
        21 to "Demon",
        22 to "MindTrap",
        23 to "Gyroid",
        24 to "Swastika",
        25 to "RainRoad",
        26 to "TheAbyss",
        27 to "MandelbrotPatternDecoration",
        28 to "CookiePaper"
    )

    private val randomShaderName = shaderNames[MathUtils.random(1.0, 28.0).toInt()] ?: "DropofDistortion"

    val menuShader = if (MenuShader.type == MenuShader.ShaderType.Random) {
        val randomShaderPath = "/assets/shaders/menu/$randomShaderName.fsh"
        FragmentShader(randomShaderPath, VERTEX_SHADER)
    } else {
        val shaderPath = "/assets/shaders/menu/" + MenuShader.type.toString() + ".fsh"
        FragmentShader(shaderPath, VERTEX_SHADER)
    }

    val rectShader = FragmentShader("/assets/shaders/gui/rect.frag", VERTEX_SHADER_GUI)
}