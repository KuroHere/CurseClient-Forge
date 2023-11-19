package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.Render2DEvent;
import com.curseclient.client.manager.managers.ModuleManager;
import com.curseclient.client.module.modules.client.PerformancePlus;
import com.curseclient.client.module.modules.player.NoEntityTrace;
import com.curseclient.client.module.modules.visual.*;
import com.curseclient.client.utility.render.MotionBlurUtil;
import com.curseclient.client.utility.render.ProjectionUtils;
import com.curseclient.client.utility.render.Screen;
import com.curseclient.mixin.accessor.render.AccessorEntityRenderer;
import com.jhlabs.vecmath.Vector3f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("")
@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

    @Shadow
    public float thirdPersonDistancePrev;
    @Shadow
    public boolean cloudFog;

    private final ShaderGroup theShaderGroup;
    @Shadow
    private boolean useShader;
    @Shadow
    @Final
    private Minecraft mc;

    public MixinEntityRenderer(ShaderGroup theShaderGroup) {
        this.theShaderGroup = theShaderGroup;
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    public void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        ProjectionUtils.INSTANCE.updateMatrix();
        Screen.INSTANCE.pushRescale();
        EventBus.INSTANCE.post(new Render2DEvent());
        Screen.INSTANCE.popRescale();

        List<ShaderGroup> shaders = new ArrayList<>();
        if (this.theShaderGroup != null && this.useShader) shaders.add(this.theShaderGroup);
        ShaderGroup motionBlur = MotionBlurUtil.Companion.getInstance().getShader();

        if (MotionBlur.INSTANCE.isEnabled()) {
            if (motionBlur != null) shaders.add(motionBlur);
            for (ShaderGroup shader : shaders) {
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                shader.render(partialTicks);
                GlStateManager.popMatrix();
            }
        }

    }

    @Inject(method = "updateShaderGroupSize(II)V", at = @At(value = "RETURN"))
    public void updateShaderGroupSize(int width, int height, CallbackInfo ci) {
        if (mc.world == null) return;
        if (OpenGlHelper.shadersSupported) {
            ShaderGroup motionBlur = MotionBlurUtil.Companion.getInstance().getShader();
            if (motionBlur != null) {
                motionBlur.createBindFramebuffers(width, height);
            }
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtCameraEffect(float partialTicks, CallbackInfo ci) {
        if(NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getHurt()) ci.cancel();
    }

    @Inject(method = "applyBobbing", at = @At("HEAD"), cancellable = true)
    private void onCameraBob(float partialTicks, CallbackInfo ci) {
        if(NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getBobbing()) ci.cancel();
    }

    @Inject(method = "drawNameplate", at = @At("HEAD"), cancellable = true)
    private static void onNametagRender(FontRenderer fontRendererIn, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking, CallbackInfo ci) {
        if (Nametags.INSTANCE.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderWorldPass", at = @At("RETURN"))
    private void renderWorldPassPost(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        HandShader.drawArm(partialTicks, pass);
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;prevTimeInPortal:F"))
    public float prevTimeInPortalHook(final EntityPlayerSP entityPlayerSP) {
        return NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.getPortal() ? -3.4028235E38f : entityPlayerSP.prevTimeInPortal;
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;inGameHasFocus:Z"))
    public boolean updateCameraAndRender(Minecraft minecraft) {
        return PerspectiveMod.INSTANCE.overrideMouse();
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
    public float getRotationYaw(Entity entity) {
        return PerspectiveMod.perspectiveToggled ? PerspectiveMod.cameraYaw : entity.rotationYaw;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F"))
    public float getPrevRotationYaw(Entity entity) {
        return PerspectiveMod.perspectiveToggled ? PerspectiveMod.cameraYaw : entity.prevRotationYaw;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"))
    public float getRotationPitch(Entity entity) {
        return PerspectiveMod.perspectiveToggled ? PerspectiveMod.cameraPitch : entity.rotationPitch;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"))
    public float getPrevRotationPitch(Entity entity) {
        return PerspectiveMod.perspectiveToggled ? PerspectiveMod.cameraPitch : entity.prevRotationPitch;
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 0, argsOnly = true)
    public EntityRenderer orientCamera$ModifyVariable$0$STORE$0(EntityRenderer value) {
        if (ThirdPersonCamera.INSTANCE.isEnabled()) {
            return null;
        } else {
            return value;
        }
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    public double orientCamera$ModifyVariable$3$STORE$0(double value) {
        if (ThirdPersonCamera.INSTANCE.isEnabled()) {
            return ThirdPersonCamera.INSTANCE.getDistance();
        } else {
            return value;
        }
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    public RayTraceResult rayTraceBlocks(WorldClient world, Vec3d start, Vec3d end) {
        return ThirdPersonCamera.INSTANCE.isEnabled() && ThirdPersonCamera.INSTANCE.getNoClip() ? null : world.rayTraceBlocks(start, end);
    }

    @Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
    void renderRainSnowInject(float partialTicks, CallbackInfo ci) {
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        if (!PerformancePlus.INSTANCE.getHideWeatherEffects()) return;
        ci.cancel();
    }

    @Inject(method = "addRainParticles", at = @At("HEAD"), cancellable = true)
    void addRainParticlesInject(CallbackInfo ci) {
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        if (!PerformancePlus.INSTANCE.getHideWeatherEffects()) return;
        ci.cancel();
    }

    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    void onLightMapUpdate(CallbackInfo ci) {
        EntityRenderer instance = (EntityRenderer) (Object) this;
        if (!((AccessorEntityRenderer)instance).getLightmapUpdateNeeded()) return;
        boolean flag = PerformancePlus.shouldUpdateLightMap();
        if (!flag) ci.cancel();
    }

    @Shadow
    @Final
    private int[] lightmapColors;

    @Inject(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V", shift = At.Shift.BEFORE))
    private void updateTextureHook(float partialTicks, CallbackInfo ci) {
        Ambience ambience = ModuleManager.INSTANCE.getModuleByClass(Ambience.class);
        assert ambience != null;
        if (ambience.isEnabled()) {
            for (int i = 0; i < this.lightmapColors.length; ++i) {
                Color ambientColor = new Color(ambience.getLightMap().getRed(), ambience.getLightMap().getGreen(), ambience.getLightMap().getBlue());
                int alpha = ambientColor.getAlpha();
                float modifier = (float) alpha / 255.0f;
                int color = this.lightmapColors[i];
                int[] bgr = toRGBAArray(color);
                Vector3f values = new Vector3f((float) bgr[2] / 255.0f, (float) bgr[1] / 255.0f, (float) bgr[0] / 255.0f);
                Vector3f newValues = new Vector3f((float) ambientColor.getRed() / 255.0f, (float) ambientColor.getGreen() / 255.0f, (float) ambientColor.getBlue() / 255.0f);
                Vector3f finalValues = mix(values, newValues, modifier);
                int red = (int) (finalValues.x * 255.0f);
                int green = (int) (finalValues.y * 255.0f);
                int blue = (int) (finalValues.z * 255.0f);
                this.lightmapColors[i] = 0xFF000000 | red << 16 | green << 8 | blue;
            }
        }
    }

    private int[] toRGBAArray(int colorBuffer) {
        return new int[]{colorBuffer >> 16 & 0xFF, colorBuffer >> 8 & 0xFF, colorBuffer & 0xFF};
    }

    private Vector3f mix(Vector3f first, Vector3f second, float factor) {
        return new Vector3f(first.x * (1.0f - factor) + second.x * factor, first.y * (1.0f - factor) + second.y * factor, first.z * (1.0f - factor) + first.z * factor);
    }

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;", shift = At.Shift.BEFORE), cancellable = true)
    public void getEntitiesInAABBexcluding(float partialTicks, CallbackInfo ci) {
        if (!NoEntityTrace.isActive()) return;

        ci.cancel();
        Minecraft.getMinecraft().profiler.endSection();
    }
}
