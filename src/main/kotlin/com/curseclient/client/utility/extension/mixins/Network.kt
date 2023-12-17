package com.curseclient.client.utility.extension.mixins

import com.curseclient.mixin.accessor.network.AccessorCPacketUseEntity
import com.curseclient.mixin.accessor.network.AccessorSPacketEntityVelocity
import com.curseclient.mixin.accessor.network.AccessorSPacketPosLook
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketPlayerPosLook

var CPacketUseEntity.useEntityId: Int
    get() = (this as AccessorCPacketUseEntity).id
    set(value) {
        (this as AccessorCPacketUseEntity).id = value
    }

var CPacketUseEntity.useEntityAction: CPacketUseEntity.Action
    get() = this.action
    set(value) {
        (this as AccessorCPacketUseEntity).action = value
    }

var SPacketEntityVelocity.entityVelocityMotionX: Int
    get() = this.motionX
    set(value) {
        (this as AccessorSPacketEntityVelocity).motionX = value
    }
var SPacketEntityVelocity.entityVelocityMotionY: Int
    get() = this.motionY
    set(value) {
        (this as AccessorSPacketEntityVelocity).motionY = value
    }
var SPacketEntityVelocity.entityVelocityMotionZ: Int
    get() = this.motionZ
    set(value) {
        (this as AccessorSPacketEntityVelocity).motionZ = value
    }

var SPacketPlayerPosLook.playerPosLookYaw: Float
    get() = this.yaw
    set(value) {
        (this as AccessorSPacketPosLook).yaw = value
    }
var SPacketPlayerPosLook.playerPosLookPitch: Float
    get() = this.pitch
    set(value) {
        (this as AccessorSPacketPosLook).pitch = value
    }