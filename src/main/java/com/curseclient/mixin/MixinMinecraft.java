package com.curseclient.mixin;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.RootEvent;
import com.curseclient.client.gui.impl.mcgui.SplashProgress;
import com.curseclient.client.module.impls.client.MenuShader;
import com.curseclient.client.utility.DeltaTime;
import com.curseclient.client.utility.render.DisplayUtils;
import com.curseclient.client.utility.threads.MainThreadExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Util;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow public GameSettings gameSettings;

    @Inject(method = "setWindowIcon", at = @At("HEAD"), cancellable = true)
    private void onSetWindowIconPre(CallbackInfo ci) {
        if (Util.getOSType() != Util.EnumOS.OSX) {
            final ByteBuffer[] icon = DisplayUtils.Companion.getFavicon();
            if (icon != null) {
                Display.setIcon(icon);
                ci.cancel();
            }
        }
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private long lastFrame = getTime();

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void runGameLoop(final CallbackInfo callbackInfo) {
        final long currentTime = getTime();
        final int deltaTime = (int) (currentTime - lastFrame);
        lastFrame = currentTime;

        DeltaTime.deltaTime = deltaTime;
    }

    @Inject(method={"runGameLoop"}, at={@At(value="HEAD")})
    private void onRunGameLoop(CallbackInfo callbackInfo) {
        RootEvent event = new RootEvent();
        EventBus.INSTANCE.post(event);
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Timer;updateTimer()V", shift = At.Shift.BEFORE))
    public void runGameLoopStart(CallbackInfo ci) {
        MainThreadExecutor.begin();
    }

    @Inject(method = "checkGLError", at = @At("HEAD"), cancellable = true)
    public void onGLCheck(String message, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "getLimitFramerate", at = @At("HEAD"), cancellable = true)
    public void limitFramerate(CallbackInfoReturnable<Integer> cir) {
        MenuShader.handleGetLimitFramerate(cir);
    }

    @Inject(method={"drawSplashScreen"}, at={@At(value="HEAD")}, cancellable=true)
    public void drawSplashScreen(TextureManager textureManager, CallbackInfo callbackInfo) {
        SplashProgress.INSTANCE.drawSplash(textureManager);
        SplashProgress.INSTANCE.setProgress(1, "Starting Game...");
        callbackInfo.cancel();
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", remap=false, target="Lnet/minecraft/client/renderer/texture/TextureMap;<init>(Ljava/lang/String;)V", shift=At.Shift.BEFORE)})
    private void onLoadingTextureMap(CallbackInfo callbackInfo) {
        SplashProgress.INSTANCE.setProgress(2, "Loading Texture Map...");
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/model/ModelManager;<init>(Lnet/minecraft/client/renderer/texture/TextureMap;)V", shift=At.Shift.BEFORE)})
    private void onLoadingModelManager(CallbackInfo callbackInfo) {
        SplashProgress.INSTANCE.setProgress(3, "Loading Model Manager...");
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/RenderItem;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/renderer/block/model/ModelManager;Lnet/minecraft/client/renderer/color/ItemColors;)V", shift=At.Shift.BEFORE)})
    private void onLoadingItemRenderer(CallbackInfo callbackInfo) {
        SplashProgress.INSTANCE.setProgress(4, "Loading Item Renderer...");
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", remap=false, target="Lnet/minecraft/client/renderer/EntityRenderer;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/resources/IResourceManager;)V", shift=At.Shift.BEFORE)})
    private void onLoadingEntityRenderer(CallbackInfo callbackInfo) {
        SplashProgress.INSTANCE.setProgress(5, "Loading Entity Renderer...");
    }
}
