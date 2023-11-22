package com.curseclient.client.gui.impl.particles.mouse

import com.curseclient.client.utility.render.animation.SimpleAnimation
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

// Tôi phải công nhận rằng tôi ghét điều này....
class Particle(private var x: Double, private var y: Double) {

    private val rand = Random
    private var tick = 0
    private val lifespan: Int = rand.nextInt(20)
    private var dead = false
    private var lastTick: Long = System.currentTimeMillis()
    private val particles: MutableList<Pair<Double, Double>> = ArrayList()
    private val smallParticles: MutableList<Triple<Double, Double, Long>> = ArrayList()
    private val lightTrail: MutableList<Pair<Double, Double>> = ArrayList()
    private var smallParticlesAnimationStart: Long = 0

    private val MAX_TRAIL_LENGTH = 50
    private var prevX: Double = x
    private var prevY: Double = y


    private val animation = SimpleAnimation(0.0f)
    init {
        genParticles()
    }

    private fun genParticles() {
        repeat(rand.nextInt(10)) {
            particles.add(Pair(x + rand.nextInt(5) - 2.0, y + rand.nextInt(5) - 2.0))
        }
    }

    fun updateParticles() {
        if (System.currentTimeMillis() < lastTick + 16)
            return
        lastTick = System.currentTimeMillis()

        tick++

        if (tick > lifespan) {
            dead = true
            particles.clear()
        }

        val progress = tick.toDouble() / lifespan.toDouble()
        val targetValue = 1.0f - progress
        animation.setAnimation(targetValue.toFloat(), 10.0)

        for (i in 0 until particles.size - 1) {
            val pos = particles[i]
            val offsetX = pos.first - x
            val offsetY = pos.second - y

            particles[i] = Pair(pos.first + (offsetX * animation.value), pos.second + (offsetY * animation.value))
        }

        lightTrail.add(Pair(x, y))
        if (lightTrail.size > MAX_TRAIL_LENGTH) {
            lightTrail.removeAt(0)
        }

        updateCoordinates(x + 1, y + 1)
    }

    fun isMoving(): Boolean {
        val movementThreshold = 1.0 // Điều chỉnh ngưỡng dựa trên nhu cầu của bạn
        return abs(x - prevX) > movementThreshold || abs(y - prevY) > movementThreshold
    }

    fun updateCoordinates(newX: Double, newY: Double) {
        prevX = x
        prevY = y
        x = newX
        y = newY
    }

    fun Random.nextGaussian(mean: Double = 0.0, stdDev: Double = 1.0): Double {
        // Sử dụng phương thức nextDouble để tạo số ngẫu nhiên theo phân phối đều
        val u1 = 1.0 - nextDouble()
        val u2 = 1.0 - nextDouble()

        // Chuyển đổi nó thành số ngẫu nhiên theo phân phối chuẩn
        val randStdNormal = kotlin.math.sqrt(-2.0 * kotlin.math.ln(u1)) * kotlin.math.cos(2.0 * kotlin.math.PI * u2)

        // Áp dụng trung bình và độ lệch chuẩn
        return mean + stdDev * randStdNormal
    }


    fun createSmallParticles(): List<Triple<Double, Double, Long>> {
        smallParticles.clear()

        for (i in 0 until 2) {
            val smallParticle = Triple(x + rand.nextGaussian(0.0, 3.0), y + rand.nextGaussian(0.0, 3.0), System.currentTimeMillis())
            smallParticles.add(smallParticle)
            smallParticlesAnimationStart = System.currentTimeMillis()
        }

        return smallParticles
    }

    fun renderSmallParticles(x: Double, y: Double) {
        val animationDuration = 1000L // Thời gian mỗi hạt sống

        smallParticles.forEach { (particleX, particleY, startTime) ->
            val progress = calculateSmallParticlesProgress(animationDuration, startTime)
            val newSize = progress * 10 // Thay đổi giá trị để điều chỉnh kích thước
            val newColor = getColorForAge(0) // Thay đổi giá trị nếu cần

            val fontRenderer = Minecraft.getMinecraft().fontRenderer

            GlStateManager.pushMatrix()
            GlStateManager.scale(newSize.toDouble(), newSize.toDouble(), 1.0)
            GlStateManager.color(newColor.red / 255.0f, newColor.green / 255.0f, newColor.blue / 255.0f, 1.0f)

            val text = generateRandomText()
            val xOffset = -fontRenderer.getStringWidth(text) / 2
            val yOffset = -fontRenderer.FONT_HEIGHT / 2

            fontRenderer.drawStringWithShadow(text, (particleX + xOffset).toFloat(), (particleY + yOffset).toFloat(), 1)

            GlStateManager.popMatrix()
        }

        // Đặt lại kích thước và màu sắc để không ảnh hưởng đến vẽ sau đó
        GlStateManager.scale(1.0, 1.0, 1.0)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun calculateSmallParticlesProgress(animationDuration: Long, startTime: Long): Float {
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - startTime
        return min(1.0f, elapsed.toFloat() / animationDuration)
    }

    private fun generateRandomText(length: Int = 1): String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9') // Include other characters if needed
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun getColorForAge(age: Int): Color {
        // Thay đổi logic để phản ánh sự thay đổi màu sắc theo độ tuổi
        val blueComponent = (20 - age) * 12 // Thay đổi dựa trên gradient mong muốn
        return Color(0, 0, blueComponent)
    }

    fun getParticles(): List<Pair<Double, Double>> {
        return particles
    }

    val isDead: Boolean
        get() = dead

    fun clear() {
        lightTrail.clear()
    }
}