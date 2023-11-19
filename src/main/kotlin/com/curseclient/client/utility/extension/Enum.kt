package com.curseclient.client.utility.extension

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.listener.Nameable
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.util.EnumHand

val Enum<*>.settingName: String get() = (this as? Nameable)?.displayName ?: this.name