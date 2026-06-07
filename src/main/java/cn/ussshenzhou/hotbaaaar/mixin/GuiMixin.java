package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import cn.ussshenzhou.hotbaaaar.util.Util;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders the extended hotbar: up to four 9-slot rows laid out as one wide strip, with items drawn at
 * fixed logical positions (see {@link HotbaaaarClient}). Faithfully reproduces the vanilla 1.16.5
 * hotbar (background, selection frame, offhand slot, attack indicator) generalised to N rows.
 * <p>
 * 1.16.5 predates the 1.17 core-shader rework, so texture setup uses {@code TextureManager.bind} +
 * {@code RenderSystem.color4f} rather than {@code RenderSystem.setShader*}. The official-channel
 * mappings on 1.16.5 keep MCP class names (IngameGui/AbstractGui/MatrixStack/...) but expose modern
 * Mojang member names.
 *
 * @author USS_Shenzhou
 */
@Mixin(IngameGui.class)
public abstract class GuiMixin extends AbstractGui {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @Shadow
    private PlayerEntity getCameraPlayer() {
        throw new AssertionError();
    }

    @Shadow
    private void renderSlot(int x, int y, float partialTick, PlayerEntity player, ItemStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$renderHotbar(float partialTick, MatrixStack matrixStack, CallbackInfo ci) {
        PlayerEntity player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        HotbaaaarClient.tickSanity();
        PlayerInventory inv = player.inventory;
        int rows = HotbaaaarClient.getRows();

        ItemStack offhand = player.getOffhandItem();
        HandSide offhandArm = player.getMainArm().getOpposite();

        final int oneHotbar = Util.HOTBAR_UNIT_LENGTH;
        final int half = 91;
        final int height = Util.HOTBAR_UNIT_HEIGHT;
        int center = this.screenWidth / 2;
        int x0 = center - rows * half;
        int x1 = x0 + rows * oneHotbar;

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(Util.WIDGETS_LOCATION);
        RenderSystem.enableBlend();

        // backgrounds
        for (int i = 0; i < rows; i++) {
            this.blit(matrixStack, x0 + i * oneHotbar, this.screenHeight - height, 0, 0, oneHotbar, height);
        }

        // selection frame at the logical selected slot
        int logicalSelected = MathHelper.clamp(HotbaaaarClient.getActiveLogicalRow(), 0, rows - 1) * 9 + inv.selected;
        this.blit(matrixStack, x0 - 1 + logicalSelected * 20 + (logicalSelected / 9 * 2), this.screenHeight - height - 1, 0, 22, 24, 22);

        // offhand frame
        if (!offhand.isEmpty()) {
            if (offhandArm == HandSide.LEFT) {
                this.blit(matrixStack, x0 - 29, this.screenHeight - 23, 24, 22, 29, 24);
            } else {
                this.blit(matrixStack, x1, this.screenHeight - 23, 53, 22, 29, 24);
            }
        }

        // items, read from the physical slot that currently holds each logical position
        for (int i = 0; i < rows * 9; i++) {
            int logicalRow = i / 9;
            int col = i % 9;
            int physicalSlot = HotbaaaarClient.physicalRowOfLogical(logicalRow) * 9 + col;
            int x = x0 + i * 20 + 3 + (i / 9 * 2);
            int y = this.screenHeight - 16 - 3;
            this.renderSlot(x, y, partialTick, player, inv.items.get(physicalSlot));
        }

        // offhand item
        if (!offhand.isEmpty()) {
            int y = this.screenHeight - 16 - 3;
            if (offhandArm == HandSide.LEFT) {
                this.renderSlot(x0 - 26, y, partialTick, player, offhand);
            } else {
                this.renderSlot(x1 + 10, y, partialTick, player, offhand);
            }
        }

        // attack indicator
        if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
            float scale = this.minecraft.player.getAttackStrengthScale(0.0F);
            if (scale < 1.0F) {
                int y = this.screenHeight - 20;
                int x = (offhandArm == HandSide.RIGHT) ? x0 - 22 : x1 + 6;
                this.minecraft.getTextureManager().bind(Util.GUI_ICONS_LOCATION);
                int progress = (int) (scale * 19.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                this.blit(matrixStack, x, y, 36, 94, 18, 18);
                this.blit(matrixStack, x, y + 18 - progress, 52, 94 + 18 - progress, 18, progress);
            }
        }

        ci.cancel();
    }
}
