package com.curseclient.client.module.impls.misc

import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.runSafeR
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import net.minecraft.world.GameType
import net.minecraftforge.fml.common.gameevent.TickEvent

object FakeGameMode : Module(
    "FakeGameMode",
     "Fakes your current gamemode client side.",
    Category.MISC
) {
    private val gamemode by setting("Mode", GameMode.CREATIVE)

    @Suppress("UNUSED")
    private enum class GameMode(val gameType: GameType) {
        SURVIVAL(GameType.SURVIVAL),
        CREATIVE(GameType.CREATIVE),
        ADVENTURE(GameType.ADVENTURE),
        SPECTATOR(GameType.SPECTATOR)
    }

    private var prevGameMode: GameType? = null

    init {
        safeListener<TickEvent.ClientTickEvent> {
            playerController.setGameType(gamemode.gameType)
        }
    }

    override fun onEnable() {
        runSafeR {
            prevGameMode = playerController.currentGameType
        } ?: onDisable()
    }


    override fun onDisable() {
        runSafe {
            prevGameMode?.let { playerController.setGameType(it) }
        }
    }
}
