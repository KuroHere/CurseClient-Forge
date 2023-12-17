package com.curseclient.mixin.accessor.client

import net.minecraft.util.ScreenShotHelper
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Invoker
import java.io.File

@Mixin(ScreenShotHelper::class)
interface IScreenShotHelper {
    companion object {
        @JvmStatic
        @Invoker("getTimestampedPNGFileForDirectory")
        fun getTimestampedPNGFileForDirectory0(
            file1 : File
        ) : File {
            throw AssertionError()
        }
    }

}

