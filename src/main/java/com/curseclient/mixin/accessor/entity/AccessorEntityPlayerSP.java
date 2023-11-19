package com.curseclient.mixin.accessor.entity;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPlayerSP.class)
public interface AccessorEntityPlayerSP {

    @Accessor("lastReportedYaw")
    float getLastReportedYaw();

    @Accessor("lastReportedYaw")
    void setLastReportedYaw(float value);

    @Accessor("lastReportedPitch")
    float getLastReportedPitch();

    @Accessor("lastReportedPitch")
    void setLastReportedPitch(float value);
}
