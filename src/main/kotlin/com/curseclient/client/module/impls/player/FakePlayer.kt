package com.curseclient.client.module.impls.player

import com.curseclient.client.event.SafeClientEvent
import com.curseclient.client.event.events.AttackEvent
import com.curseclient.client.event.events.ConnectionEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.listener
import com.curseclient.client.event.listener.onMainThreadSuspend
import com.curseclient.client.event.listener.runSafe
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.managers.PacketManager
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.module.impls.combat.Criticals
import com.curseclient.client.setting.setting
import com.curseclient.client.utility.extension.transformIf
import com.curseclient.client.utility.math.MathUtils.clamp
import com.curseclient.client.utility.render.vector.Conversion.dist
import com.curseclient.client.utility.threads.runAsync
import com.curseclient.client.utility.world.CrystalUtils.calcCrystalDamage
import com.mojang.authlib.GameProfile
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameType
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*


object FakePlayer : Module(
    "FakePlayer",
    "Spawns fake player to test your config.",
    Category.PLAYER,
    alwaysListenable = true
) {
    private val nickname by setting("Nickname", "Kuro")
    private val attack by setting("Hit", true)
    private val attackHealth by setting("Hit Damage", 4.0, 0.5, 20.0, 0.5)
    private val crystal by setting("Crystal", true)
    private val crystalDamageAmount by setting("Crystal Damage", 0.4, 0.01, 1.0, 0.01)
    private val healDelay by setting("Heal Delay", 10.0, 1.0, 30.0, 1.0)
    private val healAmount by setting("Heal Amount", 1.0, 0.1, 10.0, 0.1)
    private val maxHealth by setting("Max Health", 20.0, 5.0, 20.0, 0.5)
    private val armor by setting("Armor", true)

    var fakePlayer: EntityOtherPlayerMP? = null; private set
    private var lastDamageTime = 0L

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@safeListener

            fakePlayer?.let { fp ->
                var health = fp.health.toDouble()
                if (player.ticksExisted % healDelay.toInt() == 0) health += healAmount
                fp.health = clamp(health, 1.0, maxHealth).toFloat()
            }
        }

        safeListener<AttackEvent.Post> {
            fakePlayer?.let { fp ->
                if (!attack) return@safeListener
                if (it.entity.entityId != fp.entityId) return@safeListener

                val critical = ((PacketManager.lastReportedPosY < PacketManager.prevReportedPos.y && !PacketManager.lastReportedOnGround) || Criticals.isEnabled()) &&
                    !player.isInWater &&
                    !player.isInLava &&
                    !player.isRiding &&
                    !player.isOnLadder &&
                    !PacketManager.lastReportedSprinting

                if (critical)
                    sound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT)
                else if (player.getCooledAttackStrength(0.5f) > 0.9)
                    sound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG)
                else
                    sound(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK)

                fp.damage(attackHealth.toFloat().transformIf(critical) { it * 1.5f })
            }
        }

        safeListener<PacketEvent.Receive> {
            if (!crystal) return@safeListener

            runAsync {
                fakePlayer?.let { fp ->
                    val packet = (it.packet as? SPacketSoundEffect) ?: return@runAsync
                    if (packet.category != SoundCategory.BLOCKS && packet.sound != SoundEvents.ENTITY_GENERIC_EXPLODE) return@runAsync

                    val vec = Vec3d(packet.x, packet.y, packet.z)
                    if (vec dist fp.positionVector > 10.0) return@runAsync
                    val damage = calcCrystalDamage(vec, fp) * crystalDamageAmount.toFloat()

                    onMainThreadSuspend {
                        fp.damage(damage)
                    }
                }
            }
        }

        listener<ConnectionEvent.Connect> { if (isEnabled()) toggle() }
        listener<ConnectionEvent.Disconnect> { if (isEnabled()) toggle() }
    }

    override fun onEnable() {
        runSafe {
            val gameProfile = GameProfile(UUID.randomUUID(), nickname)

            fakePlayer = EntityOtherPlayerMP(world, gameProfile)

            fakePlayer?.let { fp ->
                fp.entityId = -1882
                fp.copyLocationAndAnglesFrom(player)
                fp.rotationYawHead = player.rotationYawHead
                fp.setGameType(GameType.SURVIVAL)
                fp.health = maxHealth.toFloat()
                fp.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack(Items.TOTEM_OF_UNDYING))

                if (armor) fp.addArmor()

                lastDamageTime = 0L
                world.addEntityToWorld(fp.entityId, fp)
            }
        }
    }

    override fun onDisable() {
        runSafe {
            fakePlayer?.let { world.removeEntityFromWorld(it.entityId) }
        }
    }

    private fun SafeClientEvent.sound(sound: SoundEvent) {
        val vec = player.positionVector
        world.playSound(player, vec.x, vec.y, vec.z, sound, player.soundCategory, 1.0f, 1.0f)
    }

    private fun EntityOtherPlayerMP.damage(amount: Float) {
        val fp = this
        if (System.currentTimeMillis() - lastDamageTime < 500L) return

        runSafe {
            val health = fp.health - amount
            if (health < 0.0f) {
                val vec = fp.positionVector
                fakePlayer?.let { mc.effectRenderer.emitParticleAtEntity(it, EnumParticleTypes.TOTEM, 30) };
                world.playSound(player, vec.x, vec.y, vec.z, SoundEvents.ITEM_TOTEM_USE, player.soundCategory, 1.0f, 1.0f)
                fp.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack(Items.TOTEM_OF_UNDYING))
                runAsync {
                    Thread.sleep(100L)

                    onMainThreadSuspend {
                        fp.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY)
                        val newHealth = clamp(fp.health + 4.0f, 1.0f, maxHealth)
                        fp.health = newHealth
                    }
                }
            } else {
                fp.health = clamp(health, 1.0f, FakePlayer.maxHealth.toFloat())
            }
        }
        lastDamageTime = System.currentTimeMillis()
    }

    private fun EntityPlayer.addArmor() {
        inventory.armorInventory[3] = ItemStack(Items.DIAMOND_HELMET).apply {
            addMaxEnchantment(Enchantments.PROTECTION)
            addMaxEnchantment(Enchantments.UNBREAKING)
            addMaxEnchantment(Enchantments.RESPIRATION)
            addMaxEnchantment(Enchantments.AQUA_AFFINITY)
            addMaxEnchantment(Enchantments.MENDING)
        }

        inventory.armorInventory[2] = ItemStack(Items.DIAMOND_CHESTPLATE).apply {
            addMaxEnchantment(Enchantments.PROTECTION)
            addMaxEnchantment(Enchantments.UNBREAKING)
            addMaxEnchantment(Enchantments.MENDING)
        }

        inventory.armorInventory[1] = ItemStack(Items.DIAMOND_LEGGINGS).apply {
            addMaxEnchantment(Enchantments.BLAST_PROTECTION)
            addMaxEnchantment(Enchantments.UNBREAKING)
            addMaxEnchantment(Enchantments.MENDING)
        }

        inventory.armorInventory[0] = ItemStack(Items.DIAMOND_BOOTS).apply {
            addMaxEnchantment(Enchantments.PROTECTION)
            addMaxEnchantment(Enchantments.FEATHER_FALLING)
            addMaxEnchantment(Enchantments.DEPTH_STRIDER)
            addMaxEnchantment(Enchantments.UNBREAKING)
            addMaxEnchantment(Enchantments.MENDING)
        }
    }

    private fun ItemStack.addMaxEnchantment(enchantment: Enchantment) {
        addEnchantment(enchantment, enchantment.maxLevel)
    }
}