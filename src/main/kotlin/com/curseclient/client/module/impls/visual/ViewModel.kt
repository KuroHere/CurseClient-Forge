package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.listener.Nameable
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.combat.KillAura
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.mixins.*
import com.curseclient.client.utility.extension.settingName
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.math.MathUtils.lerp
import com.curseclient.client.utility.math.MathUtils.toInt
import com.curseclient.mixin.accessor.render.AccessorItemRenderer
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.init.Items
import net.minecraft.item.EnumAction
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.*

object ViewModel: Module(
    "ViewModel",
    "Fancy sword animations",
    Category.VISUAL
) {
    private val page by setting("Page", Page.Animation)

    // Animation
    private val mode by setting("Animation Mode", Mode.Default, { page == Page.Animation })
    private val static by setting("Static", false, { page == Page.Animation && mode == Mode.Custom })
    private val rotateXPre by setting("Rotate X Pre", 0.0, -180.0, 180.0, 2.0, { page == Page.Animation && mode == Mode.Custom })
    private val rotateYPre by setting("Rotate Y Pre", 0.0, -180.0, 180.0, 2.0, { page == Page.Animation && mode == Mode.Custom })
    private val rotateZPre by setting("Rotate Z Pre", 0.0, -180.0, 180.0, 2.0, { page == Page.Animation && mode == Mode.Custom })
    private val rotateXPost by setting("Rotate X Post", 0.0, -180.0, 180.0, 2.0, { page == Page.Animation && mode == Mode.Custom })
    private val rotateYPost by setting("Rotate Y Post", 0.0, -180.0, 180.0, 2.0, { page == Page.Animation && mode == Mode.Custom })
    private val rotateZPost by setting("Rotate Z Post", 0.0, -180.0, 180.0, 2.0, { page == Page.Animation && mode == Mode.Custom })
    @JvmStatic val swingSpeed by setting("Duration", 2.0, 0.1, 10.0, 0.1, { page == Page.Animation })
    private val pickupAnimation by setting("Pickup Animation", false, { page == Page.Animation })
    private val auraOnly by setting("KillAura Only", false, { page == Page.Animation })

    // Translate
    private val posX by setting("Pos X", 0.0, -5.0, 5.0, 0.05, { page == Page.Position })//
    private val posY by setting("Pos Y", 0.0, -5.0, 5.0, 0.05, { page == Page.Position })//
    private val posZ by setting("Pos Z", 0.0, -5.0, 5.0, 0.05, { page == Page.Position })//
    private val modifyHand by setting("Modify Hand", false, { page == Page.Position })

    // Other
    private val rotateX by setting("Rotate X", 0.0, -180.0, 180.0, 2.0, { page == Page.Other })
    private val rotateY by setting("Rotate Y", 0.0, -180.0, 180.0, 2.0, { page == Page.Other })
    private val rotateZ by setting("Rotate Z", 0.0, -180.0, 180.0, 2.0, { page == Page.Other })
    private val scale by setting("Scale", 1.0, 0.1, 3.0, 0.05, { page == Page.Other })//

    override fun getHudInfo() =
        mode.settingName

    private enum class Mode(override val displayName: String): Nameable {
        Default("Default"),
        Slide1("Slide 1"),
        Slide2("Slide 2"),
        Slide3("Slide 3"),
        Classic1("Classic 1"),
        Classic2("Classic 2"),
        Classic3("Classic 3"),
        Glide("Glide"),
        Custom("Custom"),
        AutoRotate("AutoRotate")
    }

    private enum class Page {
        Animation,
        Position,
        Other
    }

    private fun animate(progress: Double) {
        when(mode) {
            Mode.Glide -> {
                val p = abs(abs((progress * 2.0) - 1.0) - 1.0)

                translate(0.0, (1.0 - p) * 0.15, 0.0)

                rotate(
                    lerp(-86.0, -102.0, p),
                    lerp(32.0, -44.0, p),
                    lerp(56.0, 82.0, p)
                )
            }

            Mode.Slide1 -> {
                val p = abs(abs((progress * 2.0) - 1.0) - 1.0)
                rotate(lerp(-176.0, -180.0, p), lerp(26.0, -70.0, p), lerp(82.0, 90.0, p))
            }

            Mode.Slide2 -> {
                val p = sin(sqrt(progress) * 3.141592)
                rotate(6.0, lerp(-62.0, 20.0, p), lerp(96.0, 92.0, p))
            }

            Mode.Slide3 -> {
                val p = sin(sqrt(progress) * 3.141592)
                rotate(36.0, lerp(-70.0, 18.0, p), 104.0)
            }

            Mode.Classic1 -> {
                val f = sin(progress * progress * Math.PI.toFloat()).toFloat()
                val f1 = sin(sqrt(progress) * 3.141592f).toFloat()
                GlStateManager.rotate((45.0f + f * -20.0f), 0.0f, 1.0f, 0.0f)
                GlStateManager.rotate(f1 * -20.0f, 0.0f, 0.0f, 1.0f)
                GlStateManager.rotate(f1 * -80.0f, 1.0f, 0.0f, 0.0f)
                GlStateManager.rotate(-45.0f, 0.0f, 1.0f, 0.0f)
            }

            Mode.Classic2 -> {
                val p = sin(sqrt(progress) * 3.141592)
                rotate(lerp(0.0, -100.0, p), lerp(0.0, -36.0, p), lerp(0.0, 36.0, p))
            }

            Mode.Classic3 -> {
                val p = sin(sqrt(progress) * 3.141592)
                rotate(lerp(0.0, -90.0, p), lerp(0.0, -30.0, p), lerp(0.0, 18.0, p))
            }

            Mode.Custom -> {
                val p = progress.toFloat().ease()
                rotate(
                    lerp(rotateXPre, rotateXPost, p),
                    lerp(rotateYPre, rotateYPost, p),
                    lerp(rotateZPre, rotateZPost, p)
                )
            }
            Mode.AutoRotate -> {

            }
            else -> {}
        }
    }

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (pickupAnimation) return@safeListener

            mc.itemRenderer.itemStackMainHand = player.heldItemMainhand
            mc.itemRenderer.prevEquippedProgressMainHand = 1.0f
            mc.itemRenderer.equippedProgressMainHand = 1.0f

            mc.itemRenderer.itemStackOffHand = player.heldItemOffhand
            mc.itemRenderer.prevEquippedProgressOffHand = 1.0f
            mc.itemRenderer.equippedProgressOffHand = 1.0f
        }
    }

    @JvmStatic
    fun handleRender(player: AbstractClientPlayer, partialTicks: Float, pitch: Float, hand: EnumHand, swingProgress: Float, stack: ItemStack, equippedProgress: Float) {
        val flag = hand == EnumHand.MAIN_HAND
        val handSide: EnumHandSide = if (flag) player.primaryHand else player.primaryHand.opposite()
        val sideFactor = if (handSide == EnumHandSide.RIGHT) 1f else -1f

        GlStateManager.pushMatrix()

        if (modifyHand || !stack.isEmpty) {
            val isEating = stack.itemUseAction == EnumAction.EAT && player.isHandActive && player.itemInUseCount > 0 && player.activeHand == hand && player.heldItemMainhand.item != Items.EXPERIENCE_BOTTLE
            val usingTicks = (player.itemInUseCount.toFloat() - partialTicks + 1.0f) * isEating.toInt().toDouble()

            // eating animation centering
            val p = clamp(((4.0 - usingTicks) * 0.25) * sqrt(usingTicks + 2.0) - 0.41, 0.0, 1.0)

            translate(posX * 0.5 * p, posY * 0.5, posZ * 0.5, sideFactor)
        }

        val renderer = mc.itemRenderer as AccessorItemRenderer

        if (stack.isEmpty) {
            if (flag && !player.isInvisible)
                renderer.invokeRenderArmFirstPerson(equippedProgress, swingProgress, handSide)

        } else if (stack.item is ItemMap) {
            if (flag && mc.itemRenderer.itemStackOffHand.isEmpty)
                renderer.invokeRenderMapFirstPerson(pitch, equippedProgress, swingProgress)
            else
                renderer.invokeRenderMapFirstPersonSide(equippedProgress, handSide, swingProgress, stack)

        } else {
            if (player.isHandActive && player.itemInUseCount > 0 && player.activeHand == hand) {
                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                when (stack.itemUseAction) {
                    EnumAction.NONE, EnumAction.BLOCK -> {
                        transformSide(sideFactor, equippedProgress)
                    }

                    EnumAction.EAT, EnumAction.DRINK -> {
                        transformEat(partialTicks, sideFactor, stack, player)
                        transformSide(sideFactor, equippedProgress)
                    }

                    EnumAction.BOW -> {
                        transformSide(sideFactor, equippedProgress)
                        transformBow(sideFactor, stack, player, partialTicks)
                    }
                }
            } else {
                transformSide(sideFactor, equippedProgress)

                if (handSide == EnumHandSide.RIGHT) {
                    if (mode == Mode.Default || !checkAura()) {
                        vanilla(swingProgress)
                    } else {
                        if (mode != Mode.Classic1) vanilla(0f)
                        animate(swingProgress.toDouble())
                    }
                }
            }

            rotate(rotateX, rotateY, rotateZ, sideFactor)
            GlStateManager.scale(scale, scale, scale)

            drawItem(renderer, player, stack, sideFactor)
        }

        GlStateManager.popMatrix()
    }

    private fun Float.ease() =
        if (static) abs(abs((this * 2.0) - 1.0) - 1.0)
        else sin(sqrt(this) * 3.141592)

    private fun vanilla(swingProgress: Float) {
        val v = -0.4f * sin(sqrt(swingProgress) * Math.PI.toFloat())
        val v1 = 0.2f * sin(sqrt(swingProgress) * (Math.PI.toFloat() * 2f))
        val v2 = -0.2f * sin(swingProgress * Math.PI.toFloat())
        GlStateManager.translate(v, v1, v2)

        val f = sin(swingProgress * swingProgress * Math.PI.toFloat())
        val f1 = sin(sqrt(swingProgress) * 3.141592f)
        GlStateManager.rotate((45.0f + f * -20.0f), 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(f1 * -20.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.rotate(f1 * -80.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(-45.0f, 0.0f, 1.0f, 0.0f)
    }

    private fun translate(x: Double, y: Double, z: Double) =
        translate(x, y, z, 1.0f)

    private fun translate(x: Double, y: Double, z: Double, sideFactor: Float) {
        GlStateManager.translate(x * sideFactor, y, -z)
    }

    private fun rotate(x: Double, y: Double, z: Double) =
        rotate(x, y, z, 1.0f)

    private fun rotate(x: Double, y: Double, z: Double, sideFactor: Float) {
        GlStateManager.rotate(x.toFloat(), 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(y.toFloat() * sideFactor, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(z.toFloat() * sideFactor, 0.0f, 0.0f, 1.0f)
    }

    private fun checkAura() =
        (KillAura.isEnabled() && KillAura.target != null) || !auraOnly

    private fun transformSide(sideFactor: Float, equippedProgress: Float) {
        GlStateManager.translate(sideFactor * 0.56f, -0.52f + equippedProgress * -0.6f, -0.72f)
    }

    // TODO: Animate
    private fun transformEat(partialTicks: Float, sideFactor: Float, stack: ItemStack, player: AbstractClientPlayer) {
        val f = player.itemInUseCount.toFloat() - partialTicks + 1.0f
        val f1 = f / stack.maxItemUseDuration.toFloat()
        if (f1 < 0.8f) {
            val f2 = abs(cos(f / 4.0f * Math.PI.toFloat()) * 0.1f)
            GlStateManager.translate(0.0f, f2, 0.0f)
        }
        val f3 = 1.0f - f1.toDouble().pow(27.0).toFloat()
        GlStateManager.translate(f3 * 0.6f * sideFactor, f3 * -0.5f, f3 * 0.0f)
        GlStateManager.rotate(sideFactor * f3 * 90.0f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(f3 * 10.0f, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(sideFactor * f3 * 30.0f, 0.0f, 0.0f, 1.0f)
    }

    private fun transformBow(sideFactor: Float, stack: ItemStack, player: AbstractClientPlayer, partialTicks: Float) {
        GlStateManager.translate(sideFactor * -0.2785682f, 0.18344387f, 0.15731531f)
        GlStateManager.rotate(-13.935f, 1.0f, 0.0f, 0.0f)
        GlStateManager.rotate(sideFactor * 35.3f, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(sideFactor * -9.785f, 0.0f, 0.0f, 1.0f)
        val f5: Float = stack.maxItemUseDuration.toFloat() - (player.itemInUseCount - partialTicks + 1.0f)
        var f6 = f5 / 20.0f
        f6 = (f6 * f6 + f6 * 2.0f) / 3.0f

        f6 = min(1f, f6)

        if (f6 > 0.1f) {
            val f7 = sin((f5 - 0.1f) * 1.3f)
            val f3 = f6 - 0.1f
            val f4 = f7 * f3
            GlStateManager.translate(f4 * 0.0f, f4 * 0.004f, f4 * 0.0f)
        }

        GlStateManager.translate(f6 * 0.0f, f6 * 0.0f, f6 * 0.04f)
        GlStateManager.scale(1.0f, 1.0f, 1.0f + f6 * 0.2f)
        GlStateManager.rotate(sideFactor * 45.0f, 0.0f, -1.0f, 0.0f)
    }

    private fun drawItem(renderer: AccessorItemRenderer, player: AbstractClientPlayer, stack: ItemStack, sideFactor: Float) {
        renderer.invokeRenderItemSide(
            player,
            stack,
            if (sideFactor > 0.0)
                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
            else ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
            sideFactor < 0.0)
    }
}