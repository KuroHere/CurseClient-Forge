package com.curseclient.mixin.gui;

import com.curseclient.client.module.modules.client.HUD;
import com.curseclient.client.module.modules.visual.CrossHair;
import com.curseclient.client.utility.Wrapper;
import com.curseclient.client.utility.render.RenderUtils2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (value = {GuiIngame.class})
public abstract
class MixinGuiIngame
    extends Gui {
    @Shadow
    @Final
    protected Minecraft mc;
    @Shadow protected GuiPlayerTabOverlay overlayPlayerList;

    @Shadow
    protected abstract void renderHotbarItem(int var2, int var3, float partialTicks, EntityPlayer var4, ItemStack var5);

    @Shadow
    @Final
    protected static ResourceLocation WIDGETS_TEX_PATH;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderHotbar(ScaledResolution sr, float partialTicks, CallbackInfo callbackInfo) {

        if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer && (HUD.INSTANCE.getBlackHotbarValue() || HUD.INSTANCE.getAnimHotbarValue())) {
            final Minecraft mc = Minecraft.getMinecraft();
            float f1;
            EntityPlayer entityPlayer = (EntityPlayer) mc.getRenderViewEntity();
            ItemStack itemstack = entityPlayer.getHeldItemOffhand();
            EnumHandSide enumhandside = entityPlayer.getPrimaryHand().opposite();
            int i = sr.getScaledWidth() / 2;
            boolean blackHB = HUD.INSTANCE.getBlackHotbarValue();
            int middleScreen = sr.getScaledWidth() / 2;
            float posInv = HUD.updateHotbar();

            GlStateManager.resetColor();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);

            float f = this.zLevel;
            this.zLevel = -90.0F;
            GlStateManager.resetColor();

            if (blackHB) {
                RenderUtils2D.INSTANCE.originalRoundedRect(middleScreen - 91, sr.getScaledHeight() - 2, middleScreen + 91, sr.getScaledHeight() - 22, 3F, Integer.MIN_VALUE);
                RenderUtils2D.INSTANCE.originalRoundedRect(middleScreen - 91 + posInv, sr.getScaledHeight() - 2, middleScreen - 91 + posInv + 22, sr.getScaledHeight() - 22, 3F, Integer.MAX_VALUE);
            } else {
                this.drawTexturedModalRect(middleScreen - 91F, sr.getScaledHeight() - 22, 0, 0, 182, 22);
                this.drawTexturedModalRect(middleScreen - 91F + posInv - 1, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
            }

            this.zLevel = f;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for (int j = 0; j < 9; ++j) {
                int k = i - 90 + j * 20 + 2;
                int l = sr.getScaledHeight() - 19 - (blackHB ? 1 : 0);
                this.renderHotbarItem(k, l, partialTicks, entityPlayer, (ItemStack) entityPlayer.inventory.mainInventory.get(j));
            }
            if (!itemstack.isEmpty()) {
                int l1 = sr.getScaledHeight() - 16 - 3;
                if (enumhandside == EnumHandSide.LEFT) {
                    this.renderHotbarItem(i - 91 - 26, l1, partialTicks, entityPlayer, itemstack);
                } else {
                    this.renderHotbarItem(i + 91 + 10, l1, partialTicks, entityPlayer, itemstack);
                }
            }
            if (this.mc.gameSettings.attackIndicator == 2 && (f1 = this.mc.player.getCooledAttackStrength(0.0f)) < 1.0f) {
                int i2 = sr.getScaledHeight() - 20;
                int j2 = i + 91 + 6;
                if (enumhandside == EnumHandSide.RIGHT) {
                    j2 = i - 91 - 22;
                }
                this.mc.getTextureManager().bindTexture(Gui.ICONS);
                int k1 = (int) (f1 * 19.0f);
                GlStateManager.color((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
                this.drawTexturedModalRect(j2, i2, 0, 94, 18, 18);
                this.drawTexturedModalRect(j2, i2 + 18 - k1, 18, 112 - k1, 18, k1);
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.resetColor();
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderAttackIndicator", at = @At(value = "HEAD"), cancellable = true)
    private void injectCrosshair(CallbackInfo ci) {

        if (CrossHair.INSTANCE.isEnabled())
            ci.cancel();

    }
}
