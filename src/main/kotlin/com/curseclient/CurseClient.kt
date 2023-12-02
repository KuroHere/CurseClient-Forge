package com.curseclient

import baritone.api.utils.Helper.mc
import com.curseclient.client.Client
import com.curseclient.client.Loader
import com.curseclient.client.extension.Thingy
import com.curseclient.client.extension.tracker.Tracker
import com.curseclient.client.manager.managers.ModuleManager
import com.curseclient.client.manager.managers.SongManager
import com.curseclient.client.utility.SoundUtils
import com.curseclient.client.utility.render.IconUtils
import com.curseclient.client.utility.render.SplashProgress
import com.curseclient.client.utility.render.text.TitleUtils
import net.minecraft.client.Minecraft
import net.minecraft.crash.CrashReport
import net.minecraft.util.Util
import net.minecraft.util.Util.EnumOS
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.Display
import java.io.File
import java.nio.ByteBuffer


@Suppress("UNUSED_PARAMETER")
@Mod(
    modid = Client.ID,
    name = Client.NAME,
    version = Client.VERSION,
    dependencies = CurseClient.DEPENDENCIES

)
class CurseClient {
    companion object {
        var DIRECTORY_PATH = "Curse"
        lateinit var directory: File
        const val DEPENDENCIES = "required-after:forge@[14.23.5.2860,);"
        const val DIR = "CurseClient"
        var initTime: Long = 0
        val LOG: Logger = LogManager.getLogger(Client.NAME)
        var instance: CurseClient? = null
        lateinit var songManager: SongManager
        var moduleManager: ModuleManager? = null
        var hwidManager: Thingy? = null
        var tracker: Tracker? = null
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        initTime = System.currentTimeMillis()
        LOG.info("Pre init started")
        val t = System.currentTimeMillis()

        tracker = Tracker()
        songManager = SongManager
        moduleManager = ModuleManager
        Display.setTitle(Client.displayName + " Loading... ")
        instance = this
        Loader.onPreLoad()

        LOG.info("Pre init completed, took: ${(System.currentTimeMillis() - t)}ms")
        initTime = System.currentTimeMillis()
    }

    fun getDirectory(): File {
        return directory
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        try {
            LOG.info("Init started")
            val t = System.currentTimeMillis()
            SplashProgress.setProgress(1, "Initializing CurseClient...")
            hwidManager = Thingy()
            SplashProgress.setProgress(2, "Initializing HWID List...")
            songManager = SongManager
            SplashProgress.setProgress(3, "Initializing sounds...")
            moduleManager = ModuleManager
            SplashProgress.setProgress(4, "Initializing Module...")

            SoundUtils.playSound(.9) { "opening.wav" }
            Loader.onLoad()
            LOG.info("Init completed, took: ${(System.currentTimeMillis() - t)}ms")
        }catch (t: Throwable) {
            // If an issue is caught, crash the game
            Minecraft.getMinecraft().crashed(CrashReport("CurseClient Startup Failure", t))
        }

    }

    private fun setCurseIcon() {
        if (Util.getOSType() != EnumOS.OSX) {
            val icon: Array<ByteBuffer> = IconUtils.getFavicon()
            Display.setIcon(icon)
        }
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        directory = File(System.getProperty("user.home"), DIRECTORY_PATH)
        if (!directory.exists()) LOG.info(String.format("%s client directory.", if (directory.mkdir()) "Created" else "Failed to create"))

        LOG.info("Post init started")
        val t = System.currentTimeMillis()
        SplashProgress.update()

        setCurseIcon()
        SplashProgress.setProgress(5, "Initializing Icon, Display name...")

        Loader.onPostLoad()
        SplashProgress.setProgress(6, "Loading completed...")
        Display.setTitle(Client.displayName + " | " + mc.session.username)
        MinecraftForge.EVENT_BUS.register(TitleUtils())
        LOG.info("Post init completed, took: ${( System.currentTimeMillis() - t)}ms")
    }

}
