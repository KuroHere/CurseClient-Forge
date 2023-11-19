package com.curseclient.client.event.events

import com.curseclient.client.event.Event
import net.minecraft.entity.Entity

/**
 * Fired when the player attacks an entity.
 *
 * @author Surge
 * @since 18/11/2022
 */
class EntityAttackedEvent(val entity: Entity) : Event