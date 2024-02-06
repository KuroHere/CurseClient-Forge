package com.curseclient.client.utility.player

import com.curseclient.client.manager.managers.FriendManager.isFriend
import com.curseclient.client.module.impls.combat.AntiBot.isBot
import com.curseclient.client.module.impls.combat.KillAura
import com.curseclient.client.module.impls.visual.ESP
import com.curseclient.client.utility.player.RotationUtils.getEyePosition
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntityShulker
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.player.EntityPlayer

object TargetingUtils {
    val mc: Minecraft = Minecraft.getMinecraft()

    fun getItems(
        items: Boolean = ESP.items,
    ): ArrayList<Entity> {
        return ArrayList(mc.player.world.loadedEntityList.filterIsInstance<Entity>().filter { e ->
            if (items && e.isItems) return@filter true
            return@filter false
        })
    }

    fun getTargetList(
        players: Boolean = KillAura.players,
        friends: Boolean = KillAura.friends,
        hostile: Boolean = KillAura.hostileMobs,
        animal: Boolean = KillAura.animals,
        invisible: Boolean = KillAura.invisible,

    ): ArrayList<EntityLivingBase> {
        return ArrayList(mc.player.world.loadedEntityList.filterIsInstance<EntityLivingBase>().filter { e ->
            if ((e.isBot()) ||
                (e == mc.renderViewEntity) ||
                (e == mc.player) ||
                (e.isInvisible && !invisible) ||
                e.isDead ||
                (e.health <= 0)
            ) return@filter false

            if ((e is EntityPlayer) && (players && !e.isSpectator && (!e.isFriend() || friends))) return@filter true
            if (hostile && e.isHostile) return@filter true
            if (animal && e.isPassive) return@filter true
            return@filter false
        })
    }

    val Entity.isItems
        get() = this is EntityItem

    val EntityLivingBase.isPassive
        get() = this is EntityAnimal
            || this is EntityAgeable
            || this is EntityAmbientCreature
            || this is EntitySquid

    val EntityLivingBase.isHostile
        get() = this is EntityMob
            || this is EntityShulker
            || this is EntityIronGolem
            || this is EntityDragon
            || this is EntityGhast

    fun getTarget(reach: Double, ignoreWalls: Boolean): EntityLivingBase?{
        if (mc.player.isDead) return null

        return getTargetList()
            .filter { (it.getDistance(mc.player) < reach) && (mc.player.canEntityBeSeen(it) || ignoreWalls) }
            .minByOrNull { it.getDistance(getEyePosition().x, getEyePosition().y, getEyePosition().z) }
    }

}