package com.curseclient.client.event.events.render

import com.curseclient.client.event.Cancellable
import com.curseclient.client.event.ICancellable
import net.minecraft.client.model.ModelBase
import net.minecraft.entity.Entity

class RenderModelEntityEvent(
    val modelBase: ModelBase, val entity: Entity, val limbSwing: Float, val limbSwingAmount: Float, val ageInTicks: Float, val netHeadYaw: Float, val headPitch: Float, val scale: Float
) : ICancellable by Cancellable()

