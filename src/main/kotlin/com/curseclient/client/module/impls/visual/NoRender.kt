package com.curseclient.client.module.impls.visual

import baritone.api.utils.Helper
import com.curseclient.client.event.events.ArmorRenderEvent
import com.curseclient.client.event.events.PacketEvent
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.module.Category
import com.curseclient.client.module.Module
import com.curseclient.client.setting.setting
import com.google.common.collect.Sets
import net.minecraft.client.tutorial.TutorialSteps
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityBat
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.network.play.server.*
import net.minecraft.util.SoundEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


object NoRender : Module(
    "NoRender",
    "Removes unuseful graphic",
    Category.VISUAL
) {
    private val BAT_SOUNDS: Set<SoundEvent> = Sets.newHashSet(
        SoundEvents.ENTITY_BAT_AMBIENT,
        SoundEvents.ENTITY_BAT_DEATH,
        SoundEvents.ENTITY_BAT_HURT,
        SoundEvents.ENTITY_BAT_LOOP,
        SoundEvents.ENTITY_BAT_TAKEOFF
    )

    private val page by setting("Page", Page.General)

    //GENERAL
    val hurt by setting("NoHurtCam", false, visible = { page == Page.General })
    val bobbing by setting("NoBob", false, visible = { page == Page.General })

    //OVERLAYS
    private val fire by setting("Fire", false, visible = { page == Page.Overlays })
    private val water by setting("Water", false, visible = { page == Page.Overlays })
    private val blocks by setting("Blocks", false, visible = { page == Page.Overlays })
    private val pumpkin by setting("Pumpkin", false, visible = { page == Page.Overlays })
    val portal by setting("Portal", false, visible = { page == Page.Overlays })
    private val vignette by setting("Vignette", false, visible = { page == Page.Overlays })

    //ENVIRONMENT
    private val noBats by setting("NoBats", true, visible = { page == Page.Environment })
    private val mob by setting("Mob", false, visible = { page == Page.Environment })
    private val `object` by setting("Object", false, visible = { page == Page.Environment })
    private val xp by setting("XP", true, visible = { page == Page.Environment })
    private val explosion by setting("Explosions", true, visible = { page == Page.Environment })
    private val fireworks by setting("Fireworks", false, visible = { page == Page.Environment })
    private val item by setting("Item", false, visible = { page == Page.Environment })

    //ARMOR
    private val head by setting("Head", true, visible = { page == Page.Armor })
    private val chestplate by setting("Chestplate", false, visible = { page == Page.Armor })
    private val leggings by setting("Leggings", false, visible = { page == Page.Armor })
    private val boots by setting("Boots", false, visible = { page == Page.Armor })

    //POTIONS
    private val blindness by setting("Blindness", false, visible = { page == Page.Potions })
    private val nausea by setting("Nausea", false, visible = { page == Page.Potions })

    //HUD
    private val tutorialOverlay by setting("Tutorial Overlay", false, visible = { page == Page.HUD })
    private val potionIcons by setting("Potion Icons", false, visible = { page == Page.HUD })
    private val health by setting("Health", false, visible = { page == Page.HUD })
    private val food by setting("Food", false, visible = { page == Page.HUD })
    private val armor by setting("Armor", false, visible = { page == Page.HUD })
    private val experience by setting("Experience", false, visible = { page == Page.HUD })

    private enum class Page {
        General,
        Overlays,
        Environment,
        Armor,
        Potions,
        HUD
    }

    override fun onEnable() {
        purgeBats()
    }

    init {
        safeListener<PacketEvent.Receive> { event ->
            if ((event.packet is SPacketSpawnMob && mob) ||
                (event.packet is SPacketSpawnObject && NoRender.`object`) ||
                (event.packet is SPacketSpawnExperienceOrb && xp) ||
                (event.packet is SPacketExplosion && explosion) ||
                (event.packet is SPacketSpawnObject && item && event.packet.type == 2) ||
                (event.packet is SPacketSpawnObject && fireworks && event.packet.type == 76))
                event.cancel();
        }

        safeListener<RenderGameOverlayEvent.Pre> {
            it.isCanceled = when (it.type) {
                RenderGameOverlayEvent.ElementType.HELMET -> pumpkin
                RenderGameOverlayEvent.ElementType.POTION_ICONS -> potionIcons
                RenderGameOverlayEvent.ElementType.PORTAL -> portal
                RenderGameOverlayEvent.ElementType.VIGNETTE -> vignette
                RenderGameOverlayEvent.ElementType.HEALTH -> health
                RenderGameOverlayEvent.ElementType.FOOD -> food
                RenderGameOverlayEvent.ElementType.ARMOR -> armor
                RenderGameOverlayEvent.ElementType.EXPERIENCE -> experience

                else -> {
                    it.isCanceled
                }
            }
        }

        safeListener<RenderBlockOverlayEvent> {
            it.isCanceled = when (it.overlayType){
                RenderBlockOverlayEvent.OverlayType.FIRE -> fire
                RenderBlockOverlayEvent.OverlayType.WATER -> water
                RenderBlockOverlayEvent.OverlayType.BLOCK -> blocks
                else -> it.isCanceled
            }
        }

        safeListener<ArmorRenderEvent> { event ->
            if (event.getSlot() === EntityEquipmentSlot.HEAD && head) {
                event.cancel()
            } else if (event.getSlot() === EntityEquipmentSlot.CHEST && chestplate) {
                event.cancel()
            } else if (event.getSlot() === EntityEquipmentSlot.LEGS && leggings) {
                event.cancel()
            } else if (event.getSlot() === EntityEquipmentSlot.FEET && boots) {
                event.cancel()
            }

        }

        safeListener<TickEvent.ClientTickEvent> {
            if (blindness) player.removeActivePotionEffect(MobEffects.BLINDNESS)
            if (nausea) player.removeActivePotionEffect(MobEffects.NAUSEA)
            if (tutorialOverlay) mc.gameSettings.tutorialStep = TutorialSteps.NONE
        }
    }

    private fun purgeBats() {
        if (noBats && Helper.mc.player != null && Helper.mc.world != null) {
            Helper.mc.world.getLoadedEntityList().stream().filter { entity: Entity? -> entity is EntityBat }.forEach { entity: Entity -> Helper.mc.world.removeEntity(entity) }
        }
    }
}