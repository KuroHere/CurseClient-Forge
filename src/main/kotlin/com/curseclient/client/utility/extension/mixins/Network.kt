package com.curseclient.client.utility.extension.mixins

import com.curseclient.mixin.accessor.network.AccessorCPacketUseEntity
import com.curseclient.mixin.accessor.network.AccessorSPacketEntityStatus
import com.curseclient.mixin.accessor.network.AccessorSPacketEntityVelocity
import com.curseclient.mixin.accessor.network.AccessorSPacketPosLook
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketEntityStatus
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
        (this as AccessorCPacketUseEntity).setAction(value)
    }

var SPacketEntityStatus.useEntityId: Int
    get() = (this as AccessorSPacketEntityStatus).id
    set(value) {
        (this as AccessorSPacketEntityStatus).id = value
    }

var SPacketEntityVelocity.entityVelocityMotionX: Int
    get() = this.motionX
    set(value) {
        (this as AccessorSPacketEntityVelocity).setMotionX(value)
    }
var SPacketEntityVelocity.entityVelocityMotionY: Int
    get() = this.motionY
    set(value) {
        (this as AccessorSPacketEntityVelocity).setMotionY(value)
    }
var SPacketEntityVelocity.entityVelocityMotionZ: Int
    get() = this.motionZ
    set(value) {
        (this as AccessorSPacketEntityVelocity).setMotionZ(value)
    }

var SPacketPlayerPosLook.playerPosLookYaw: Float
    get() = this.yaw
    set(value) {
        (this as AccessorSPacketPosLook).setYaw(value)
    }
var SPacketPlayerPosLook.playerPosLookPitch: Float
    get() = this.pitch
    set(value) {
        (this as AccessorSPacketPosLook).setPitch(value)
    }