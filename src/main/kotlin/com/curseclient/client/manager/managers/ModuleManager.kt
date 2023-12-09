package com.curseclient.client.manager.managers

import GlowESP
import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.Manager
import com.curseclient.client.module.Module
import com.curseclient.client.module.modules.client.*
import com.curseclient.client.module.modules.combat.*
import com.curseclient.client.module.modules.hud.*
import com.curseclient.client.module.modules.hud.Status.Status
import com.curseclient.client.module.modules.hud.TargetHUD.FollowTargetHud
import com.curseclient.client.module.modules.hud.TargetHUD.TargetHUD
import com.curseclient.client.module.modules.hud.graph.FpsGraph
import com.curseclient.client.module.modules.hud.graph.MovementGraph
import com.curseclient.client.module.modules.hud.modulelist.ModuleList
import com.curseclient.client.module.modules.misc.*
import com.curseclient.client.module.modules.movement.*
import com.curseclient.client.module.modules.player.*
import com.curseclient.client.module.modules.visual.*
import com.curseclient.client.module.modules.visual.TwoDESP.TwoDESP
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard


object ModuleManager: Manager("ModuleManager") {

    fun getHudModules() = arrayListOf(
        Radar,
        Model,
        NewOverlay,
        Status,
        Compass,
        MovementGraph,
        FpsGraph,
        ModuleList,
        Armor,
        FPS,
        Notifications,
        PlayerSpeed,
        RenderTest,
        TargetHUD,
        Watermark
    )

    fun getModules() = arrayListOf(
        //client
        GuiClickCircle,
        Animations,
        Cape,
        ClickGui,
        HUD,
        FontSettings,
        HudEditor,
        MenuShader,
        PerformancePlus,
        Welcome,

        //combat
        AntiBot,
        Offhand,
        Burrow,
        Criticals,
        CrystalAura,
        HitboxDesync,
        HoleMiner,
        KillAura,
        TotemPopCounter,
        Velocity,

        //misc
        Panic,
        KickSound,
        ChatMod,
        HitSound,
        SolidWeb,
        MapBounds,
        AutoRespawn,
        ChestStealer,
        FakeGameMode,
        ExplosionSoundFix,
        MiddleClick,
        FakePlayer,
        DeathSounds,
        Surround,
        HoleFiller,

        //movement
        ElytraFlight,
        FastFall,
        Flight,
        GuiWalk,
        HighJump,
        Jesus,
        KeepSprint,
        LongJump,
        NoFall,
        NoJumpDelay,
        NoSlow,
        PearlClip,
        Phase,
        ReverseStep,
        SafeWalk,
        Speed,
        Spider,
        Sprint,
        Step,
        TargetStrafe,

        //player
        AntiHunger,
        AntiRotate,
        AntiServerSlot,
        AutoEat,
        Blink,
        FastUse,
        FreeCam,
        NoEntityTrace,
        PacketLogger,
        PacketMine,
        Reach,
        SwingLimiter,
        Scaffold,
        TickShift,
        Timer,

        //visual
        FollowTargetHud,
        PenisESP,
        TwoDESP,
        CustomSky,
        PerspectiveMod,
        CustomModel,
        Ambience,
        SimsESP,
        DeadEffect,
        HitColour,
        PopChams,
        BetterScreenshot,
        WallHack,
        SuperHeroFX,
        CrossHair,
        TNTESP,
        HungerOverlay,
        MotionBlur,
        GlowESP,
        ViewLock,
        Chams,
        StorageESP,
        ConicalHat,
        CrystalRenderer,
        FovModifier,
        ESP,
        FancyHandshake,
        FogColor,
        FullBright,
        GlintModifier,
        HandShader,
        HealthParticles,
        HitParticles,
        HoleESP,
        ItemPhysics,
        JumpCircles,
        Nametags,
        NoRender,
        SelectionHighlight,
        SmoothCrouch,
        TargetESP,
        ThirdPersonCamera,
        Tracers,
        Trails,
        ViewModel,
        VisualRotations,
        WorldTime
    )


    fun getModuleByName(name: String): Module? {
        for (module in this.getModules()) {
            if (module.name != name) continue
            return module
        }
        return null
    }

    fun <T : Module?> getModuleByClass(clazz: Class<T>): T? {
        for (module in this.getModules()) {
            if (!clazz.isInstance(module)) continue
            return module as T
        }
        return null
    }

    fun load() = getModules().forEach {
        it.onInit()
        if (it.enabledByDefault) it.setEnabled(true)
    }

    fun load2() = getHudModules().forEach {
        it.onInit()
        if (it.enabledByDefault) it.setEnabled(true)
    }

    init {
        safeListener<InputEvent.KeyInputEvent> {
            getModules()
                .filter { it.key != Keyboard.KEY_NONE && it.key == Keyboard.getEventKey() }
                .forEach { it.toggle() }
        }
    }

}