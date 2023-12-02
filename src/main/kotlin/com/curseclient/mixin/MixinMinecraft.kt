package com.curseclient.mixin

import com.curseclient.client.event.EventBus
import com.curseclient.client.events.RootEvent
import com.curseclient.client.module.modules.client.MenuShader
import com.curseclient.client.utility.DeltaTime
import com.curseclient.client.utility.render.IconUtils
import com.curseclient.client.utility.render.SplashProgress
import com.curseclient.client.utility.threads.MainThreadExecutor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.Util
import org.lwjgl.Sys
import org.lwjgl.opengl.Display
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Suppress("FunctionName")
@Mixin(Minecraft::class)
class MixinMinecraft {
    @Shadow
    private val gameSettings : GameSettings? = null

    private var lastFrame = getTime()

    private fun getTime() = Sys.getTime() * 1000L / Sys.getTimerResolution()

    @Inject(
        method = ["setWindowIcon"],
        at = [At("HEAD")],
        cancellable = true
    )
    private fun setWindowIconHeadHook(
        ci : CallbackInfo
    ) {
        if(Util.getOSType() != Util.EnumOS.OSX) {
            val icon = IconUtils.getFavicon()

            Display.setIcon(icon)

            ci.cancel()
        }
    }

    @Inject(
        method = ["runGameLoop"],
        at = [At("HEAD")]
    )
    private fun runGameLoopHeadHook(
        ci : CallbackInfo
    ) {
        val current = getTime()
        val delta = current - lastFrame

        lastFrame = current

        DeltaTime.deltaTime = delta.toInt()

        EventBus.post(RootEvent())
    }

    @Inject(
        method = ["runGameLoop"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/Timer;updateTimer()V",
            shift = At.Shift.BEFORE
        )]
    )
    private fun runGameLoopUpdateTimerInvokeHook(
        ci : CallbackInfo
    ) {
        MainThreadExecutor.begin()
    }

    @Inject(
        method = ["getLimitFramerate"],
        at = [At("HEAD")],
        cancellable = true
    )
    private fun getLimitFramerateHeadHook(
        cir : CallbackInfoReturnable<Int>
    ) {
        MenuShader.handleGetLimitFramerate(cir)
    }

    @Inject(
        method = ["drawSplashScreen"],
        at = [At("HEAD")],
        cancellable = true
    )
    private fun drawSplashScreenHeadHook(
        textureManager : TextureManager,
        ci : CallbackInfo
    ) {
        SplashProgress.drawSplash(textureManager)
        SplashProgress.setProgress(1, "Starting Game...")

        ci.cancel()
    }

    @Inject(
        method = ["init"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureMap;<init>(Ljava/lang/String;)V",
            shift = At.Shift.BEFORE
        )]
    )
    private fun initTextureMap__init__InvokeHook(
        ci : CallbackInfo
    ) {
        SplashProgress.setProgress(2, "Loading Texture Map...")
    }

    @Inject(
        method = ["init"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/model/ModelManager;<init>(Lnet/minecraft/client/renderer/texture/TextureMap;)V",
            shift = At.Shift.BEFORE
        )]
    )
    private fun initModelManager__init__InvokeHook(
        ci : CallbackInfo
    ) {
        SplashProgress.setProgress(3, "Loading Model Manager...")
    }

    @Inject(
        method = ["init"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderItem;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/renderer/block/model/ModelManager;Lnet/minecraft/client/renderer/color/ItemColors;)V",
            shift = At.Shift.BEFORE
        )]
    )
    private fun initRenderItem__init__InvokeHook(
        ci : CallbackInfo
    ) {
        SplashProgress.setProgress(4, "Loading Item Renderer...")
    }

    @Inject(
        method = ["init"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/EntityRenderer;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/resources/IResourceManager;)V",
            shift = At.Shift.BEFORE
        )]
    )
    private fun initEntityRenderer__init__InvokeHook(
        ci : CallbackInfo
    ) {
        SplashProgress.setProgress(5, "Loading Entity Renderer...")
    }

}