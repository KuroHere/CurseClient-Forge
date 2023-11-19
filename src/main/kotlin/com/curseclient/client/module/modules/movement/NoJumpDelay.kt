package com.curseclient.client.module.modules.movement

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.utility.extension.mixins.jumpTicks
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object NoJumpDelay: Module("NoJumpDelay", "Removes delay between jumps", Category.MOVEMENT) { init { safeListener<ClientTickEvent> { player.jumpTicks = 0 } } } // epic