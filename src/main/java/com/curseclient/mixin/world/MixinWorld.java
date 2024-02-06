package com.curseclient.mixin.world;

import com.curseclient.client.module.impls.client.PerformancePlus;
import com.curseclient.client.module.impls.visual.Ambience;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = World.class, priority = Integer.MAX_VALUE)
public class MixinWorld {
    private Double counter = 0.0;

    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    public void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        if (Ambience.INSTANCE.isEnabled()) {
            long time = 0;
            switch (Ambience.INSTANCE.getTimeOfDay().name()) {
                case "Day":
                    time = 1000;
                    break;
                case "Sunset":
                    time = 12000;
                    break;
                case "Dawn":
                    time = 23000;
                    break;
                case "Night":
                    time = 13000;
                    break;
                case "Midnight":
                    time = 18000;
                    break;
                case "Noon":
                    time = 6000;
                    break;
                case "Custom":
                    counter += Ambience.INSTANCE.getTimeSpeed();
                    if (counter > 24000.0) counter = 0.0;
                    if (Ambience.INSTANCE.getCustomTimeSpeed())
                        time = (long) (Ambience.INSTANCE.getTime() + counter);
                    else
                        time = (long) Ambience.INSTANCE.getTime();
                    break;
            }
            if (Ambience.INSTANCE.getTimeOfDay() == Ambience.Time.None) {
                cir.getReturnValue();
            } else
                cir.setReturnValue((long) ((int) time));
        }
    }


    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    void getRawLightInject(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        World instance = (World) (Object) this;
        if (!(instance instanceof WorldClient)) return;
        if (!PerformancePlus.INSTANCE.isEnabled()) return;
        if (!PerformancePlus.INSTANCE.getFastLight()) return;
        cir.setReturnValue(false);
    }
}
