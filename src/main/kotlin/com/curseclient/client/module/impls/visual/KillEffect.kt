package com.curseclient.client.module.impls.visual

import com.curseclient.client.event.events.EntityDeathEvent
import com.curseclient.client.event.events.TotemPopEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.threads.mainThread
import net.minecraft.entity.Entity
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.SoundEvents.ENTITY_LIGHTNING_IMPACT
import net.minecraft.init.SoundEvents.ENTITY_LIGHTNING_THUNDER
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory.MASTER
import kotlin.random.Random


object KillEffect : Module(
    "KillEffect",
    "Add effect when they die/pop",
    Category.VISUAL
){
    private val deaths by setting("Deaths", true)
    private val pops by setting("Pops", false)
    private val thunder by setting("Thunder", true)
    private val fire by setting("Fire", false)
    private val range by setting("Max Range", 30, 0, 150, 1, description = "Set to 0 to disable range check")


    init {
        listener<EntityDeathEvent> { event ->
            if (event.entity != mc.player && deaths) smite(event.entity)
        }

        listener<TotemPopEvent> { event ->
            if (event.player != mc.player && pops) smite(event.player)
        }
    }


    private fun smite(entity: Entity) {
        val player = mc.player ?: return

        if (range != 0.0 && entity.getDistance(player) > range) return

        mainThread {
            if (fire) mc.effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.LAVA)
            if (thunder) mc.world.spawnEntity(EntityLightningBolt(mc.world, entity.posX, entity.posY, entity.posZ, true))
        }

        if (thunder) mc.world?.run {
            playSound(entity.position.up(5), ENTITY_LIGHTNING_THUNDER, MASTER, 10000f, 0.8f + Random.nextFloat() * 0.2f, false)
            playSound(entity.position.up(5), ENTITY_LIGHTNING_IMPACT, MASTER, 2f, 0.5f + Random.nextFloat() * 0.2f, false)
        }
    }

}