package com.curseclient.client.utility.world

import baritone.api.utils.Helper.mc
import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.utility.math.MathUtils.ceilToInt
import com.curseclient.client.utility.math.MathUtils.floorToInt
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round


object CrystalUtils {
    fun getBlockPosInSphere(center: Vec3d, radius: Float): ArrayList<BlockPos> {
        val squaredRadius = radius.pow(2)
        val posList = ArrayList<BlockPos>()

        fun getAxisRange(d1: Double, d2: Float): IntRange {
            return IntRange((d1 - d2).floorToInt(), (d1 + d2).ceilToInt())
        }

        for (x in getAxisRange(center.x, radius)) for (y in getAxisRange(center.y, radius)) for (z in getAxisRange(center.z, radius)) {
            /* Valid position check */
            val blockPos = BlockPos(x, y, z)
            if (blockPos.distanceSqToCenter(center.x, center.y, center.z) > squaredRadius) continue
            posList.add(blockPos)
        }
        return posList
    }

    fun SafeClientEvent.getCrystalList(center: Vec3d, range: Float): List<EntityEnderCrystal> =
        world.loadedEntityList.toList()
            .filterIsInstance<EntityEnderCrystal>()
            .filter { entity -> entity.isEntityAlive && entity.positionVector.distanceTo(center) <= range }


    /* End of position finding */

    /* Damage calculation */

    fun SafeClientEvent.calcCrystalDamage(pos: BlockPos, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox) =
        calcCrystalDamage(Vec3d(pos).add(0.5, 1.0, 0.5), entity, entityPos, entityBB)

    fun SafeClientEvent.calcCrystalDamage(pos: Vec3d, entity: EntityLivingBase, entityPos: Vec3d? = entity.positionVector, entityBB: AxisAlignedBB? = entity.entityBoundingBox): Float {
        // Return 0 directly if entity is a player and in creative mode
        if (entity is EntityPlayer && entity.isCreative) return 0.0f

        // Calculate raw damage (based on blocks and distance)
        var damage = calcRawDamage(pos, entityPos ?: entity.positionVector, entityBB ?: entity.entityBoundingBox)

        // Calculate damage after armor, enchantment, resistance effect absorption
        damage = calcDamage(entity, damage, getDamageSource(pos))

        // Multiply the damage based on difficulty if the entity is player
        if (entity is EntityPlayer) damage *= world.difficulty.id * 0.5f

        return max(damage, 0.0f)
    }

    private fun getBlastReduction(entity: EntityLivingBase, damageInput: Float, explosion: Explosion?): Float {
        var damage = damageInput
        if (entity is EntityPlayer) {
            val ep = entity
            val ds = DamageSource.causeExplosionDamage(explosion)
            damage = CombatRules.getDamageAfterAbsorb(damage, ep.getTotalArmorValue().toFloat(), ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())
            var k = 0
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.armorInventoryList, ds)
            } catch (ignored: java.lang.Exception) {
            }
            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            damage *= (1.0f - f / 25.0f)
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4
            }
            damage = max(damage, 0.0f)
            return damage
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, entity.getTotalArmorValue().toFloat(), entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat())
        return damage
    }

    private fun getDamageMultiplied(damage: Float): Float {
        val diff = mc.world!!.difficulty.id
        return damage * when (diff) {
            0 -> 0f
            2 -> 1f
            1 -> 0.5f
            else -> 1.5f
        }
    }


    private fun getEntityPosVec(entity: Entity, ticks: Int): Vec3d {
        return entity.positionVector.add(getMotionVec(entity, ticks))
    }

    private fun getMotionVec(entity: Entity, ticks: Int): Vec3d {
        val dX = entity.posX - entity.prevPosX
        val dZ = entity.posZ - entity.prevPosZ

        val entityMotionPosX: Double = dX * ticks
        val entityMotionPosZ: Double = dZ * ticks

        return Vec3d(entityMotionPosX, 0.0, entityMotionPosZ)
    }

    fun calculateDamage(crystal: EntityEnderCrystal, entity: Entity): Float {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity)
    }

    fun calculateDamage(pos: BlockPos, entity: Entity): Float {
        return calculateDamage(pos.x + 0.5, (pos.y + 1).toDouble(), pos.z + 0.5, entity)
    }

    fun calculateDamage(posX: Double, posY: Double, posZ: Double, entity: Entity): Float {
        val doubleExplosionSize = 12.0f
        val distancedsize: Double
        val entityPosVec: Vec3d = getEntityPosVec(entity, 0)
        distancedsize = entityPosVec.distanceTo(Vec3d(posX, posY, posZ)) / doubleExplosionSize.toDouble()
        val vec3d = Vec3d(posX, posY, posZ)
        var blockDensity = 0.0
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox()).toDouble()
        } catch (ignored: Exception) {
        }
        val v = (1.0 - distancedsize) * blockDensity
        val damage = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
        var finald = 1.0
        if (entity is EntityLivingBase) {
            finald = getBlastReduction(entity, getDamageMultiplied(damage), Explosion(mc.world, mc.player, posX, posY, posZ, 6f, false, true)).toDouble()
        }
        return finald.toFloat()
    }


    private fun calcDamage(entity: EntityLivingBase, damageIn: Float = 100f, source: DamageSource = DamageSource.GENERIC, roundDamage: Boolean = false): Float {
        if (entity is EntityPlayer && entity.isCreative) return 0.0f

        val armorValue = entity.totalArmorValue.toFloat()
        val toughness = entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()

        val armorValues = armorValue to toughness

        var damage = CombatRules.getDamageAfterAbsorb(damageIn, armorValues.first, armorValues.second)

        if (source != DamageSource.OUT_OF_WORLD) {
            entity.getActivePotionEffect(MobEffects.RESISTANCE)?.let {
                damage *= max(1.0f - (it.amplifier + 1) * 0.2f, 0.0f)
            }
        }

        damage *= getProtectionModifier(entity, source)

        return if (roundDamage) round(damage) else damage
    }

    private fun getProtectionModifier(entity: EntityLivingBase, damageSource: DamageSource): Float {
        var modifier = 0

        for (armor in entity.armorInventoryList.toList()) {
            if (armor.isEmpty) continue
            val nbtTagList = armor.enchantmentTagList
            for (i in 0 until nbtTagList.tagCount()) {
                val compoundTag = nbtTagList.getCompoundTagAt(i)

                val id = compoundTag.getInteger("id")
                val level = compoundTag.getInteger("lvl")

                Enchantment.getEnchantmentByID(id)?.let {
                    modifier += it.calcModifierDamage(level, damageSource)
                }
            }
        }

        modifier = modifier.coerceIn(0, 20)

        return 1.0f - modifier / 25.0f
    }

    fun SafeClientEvent.calcRawDamage(pos: Vec3d, entityPos: Vec3d, entityBB: AxisAlignedBB): Float {
        val distance = pos.distanceTo(entityPos)
        val v = (1.0 - (distance / 12.0)) * world.getBlockDensity(pos, entityBB)
        return ((v * v + v) / 2.0 * 84.0 + 1.0).toFloat()
    }

    private fun SafeClientEvent.getDamageSource(damagePos: Vec3d) =
        DamageSource.causeExplosionDamage(Explosion(world, player, damagePos.x, damagePos.y, damagePos.z, 6F, false, true))
    /* End of damage calculation */
}