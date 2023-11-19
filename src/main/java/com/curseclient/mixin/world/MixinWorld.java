package com.curseclient.mixin.world;

import com.curseclient.client.module.modules.client.PerformancePlus;
import com.curseclient.client.module.modules.misc.AntiWeather;
import com.curseclient.client.module.modules.visual.NoRender;
import com.curseclient.client.module.modules.visual.WorldTime;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class, priority = Integer.MAX_VALUE)
public class MixinWorld {
    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    public void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        if (WorldTime.INSTANCE.isEnabled())
            cir.setReturnValue((long) ((int) WorldTime.INSTANCE.getTime()));
    }

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    void getRawLightInject(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World instance = (World) (Object) this;
        if (!(instance instanceof WorldClient)) return;
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        if (!PerformancePlus.INSTANCE.getFastLight()) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void getThunderStrengthHead(float delta, CallbackInfoReturnable<Float> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(0.0f);
        }
    }


    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    private void getRainStrengthHead(float delta, CallbackInfoReturnable<Float> cir) {
        if (AntiWeather.INSTANCE.isEnabled()) {
            cir.setReturnValue(0.0f);
        }
    }
}
