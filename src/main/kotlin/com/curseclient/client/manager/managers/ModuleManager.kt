package com.curseclient.client.manager.managers

import com.curseclient.client.event.listener.safeListener
import com.curseclient.client.manager.Manager
import com.curseclient.client.module.impls.client.*
import com.curseclient.client.module.impls.combat.*
import com.curseclient.client.module.impls.hud.*
import com.curseclient.client.module.impls.hud.Status.Status
import com.curseclient.client.module.impls.hud.graph.FpsGraph
import com.curseclient.client.module.impls.hud.graph.MovementGraph
import com.curseclient.client.module.impls.hud.modulelist.ModuleList
import com.curseclient.client.module.impls.misc.*
import com.curseclient.client.module.impls.movement.*
import com.curseclient.client.module.impls.player.*
import com.curseclient.client.module.impls.visual.*
import com.curseclient.client.module.modules.visual.StorageESP
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard

object ModuleManager: Manager("ModuleManager") {

    fun getHudModules() = arrayListOf(
        Radar,
        Model,
        GameOverlay,
        Status,
        Compass,
        MovementGraph,
        FpsGraph,
        ModuleList,
        PvpResources,
        Armor,
        FPS,
        Notifications,
        PlayerSpeed,
        TargetHUD,
        Watermark
    )

    fun getModules() = arrayListOf(
        //client
        SoundManager,
        GuiClickCircle,
        Animations,
        Cape,
        ClickGui,
        HUD,
        FontSettings,
        HudEditor,
        MenuShader,
        PerformancePlus,

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
        BetterScreenshot,
        Radio,
        Panic,
        ChatMod,
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
        SwingLimiter,

        //movement
        ElytraFlight,
        FastFall,
        Flight,
        GuiWalk,
        HighJump,
        Jesus,
        LongJump,
        NoFall,
        NoJumpDelay,
        NoSlow,
        PearlClip,
        Phase,
        ReverseStep,
        SafeWalk,
        Scaffold,
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
        SpeedMine,
        TickShift,
        Timer,
        ViewLock,

        //visual
        Ambience,
        BlockHighlight,
        Chams,
        ConicalHat,
        Cosmetic,
        CrossHair,
        CrystalRenderer,
        CustomSky,
        ESP,
        FancyHandshake,
        FollowTargetHud,
        FovModifier,
        FreeLook,
        FullBright,
        GlintModifier,
        GlowESP,
        HandShader,
        HealthParticles,
        HitColour,
        HitParticles,
        HoleESP,
        HungerOverlay,
        Indicators,
        ItemPhysics,
        JumpCircles,
        KillEffect,
        MotionBlur,
        Nametags,
        NoRender,
        PenisESP,
        PlayerModel,
        PopChams,
        Predict,
        SmoothCrouch,
        StorageESP,
        SuperHeroFX,
        TargetESP,
        ThirdPersonCamera,
        TNTESP,
        Tracers,
        Trails,
        ViewModel,
        VisualRotations,
        WallHack,
    )

    fun moduleLoad() = getModules().forEach {
        it.onInit()
        if (it.enabledByDefault) it.setEnabled(true)
    }

    fun hudModuleLoad() = getHudModules().forEach {
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