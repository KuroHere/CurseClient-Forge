package com.curseclient.mixin.render;

import com.curseclient.client.event.EventBus;
import com.curseclient.client.event.events.render.PreScreenshotEvent;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.File;

/**
 * @author SooStrator1136
 */
@Mixin(ScreenShotHelper.class)
public class MixinScreenShotHelper {

    /**
     * @author SooStrator1136
     * @reason Firing the event is needed and @Inject would end up with the same or a bigger mixin
     */
    @Overwrite
    public static ITextComponent saveScreenshot(
        File gameDirectory,
        int width,
        int height,
        Framebuffer buffer
    ) {
        final PreScreenshotEvent event = new PreScreenshotEvent(null);
        EventBus.INSTANCE.post(event);
        return event.getCancelled() ? event.getResponse() : ScreenShotHelper.saveScreenshot(gameDirectory, null, width, height, buffer);
    }

}
