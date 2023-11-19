package com.curseclient.client.utility.render.graphic

import com.curseclient.client.utility.render.ColorExtend
import com.curseclient.client.utility.render.RenderUtils2D
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.VertexFormat

val tessellator: Tessellator = Tessellator.getInstance()
val buffer: BufferBuilder = tessellator.buffer

fun BufferBuilder.pos(x: Number, y: Number, z: Number = 0.0, colour: ColorExtend? = null, tex: Pair<Number, Number>? = null) = this.pos(x.toDouble(), y.toDouble(), z.toDouble()).apply {
    if (colour != null) {
        val (_) = colour
        color(colour.r,colour.g, colour.b, colour.a)
    }
    if (tex != null) tex(tex.first.toDouble(), tex.second.toDouble())
    endVertex()
}

@BufferDSL
inline infix fun Int.withVertexFormat(vertexFormat: VertexFormat) = this to vertexFormat

@BufferDSL
inline infix fun Pair<Int, VertexFormat>.drawBuffer(elements: BufferBuilder.() -> Unit) {
    buffer.begin(this.first, this.second)
    elements(buffer)
    tessellator.draw()
}


enum class Dimension {
    TwoD {
        override operator fun invoke(elements: () -> Unit) {
            RenderUtils2D.enableGL2D()
            elements()
            RenderUtils2D.disableGL2D()
        }
    },
    ThreeD {
        override operator fun invoke(elements: () -> Unit) {
            RenderUtils2D.prepareGL3D()
            elements()
            RenderUtils2D.releaseGL3D()
        }
    };
    abstract operator fun invoke(elements: () -> Unit)
}

@DslMarker annotation class BufferDSL