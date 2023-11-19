package com.curseclient

import com.curseclient.client.Client
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.apache.logging.log4j.LogManager
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@IFMLLoadingPlugin.Name("CurseCoreMod")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class CurseCoreMod : IFMLLoadingPlugin {
    override fun getASMTransformerClass(): Array<String> {
        return emptyArray()
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: Map<String, Any>) {}

    override fun getAccessTransformerClass(): String? {
        return null
    }

    init {
        val logger = LogManager.getLogger(Client.NAME)

        MixinBootstrap.init()
        Mixins.addConfigurations("mixins.curseclient.json", "mixins.baritone.json")

        MixinEnvironment.getDefaultEnvironment().obfuscationContext = "searge"
        logger.info("CurseClient and Baritone mixins initialised. (${MixinEnvironment.getDefaultEnvironment().obfuscationContext})")
    }
}