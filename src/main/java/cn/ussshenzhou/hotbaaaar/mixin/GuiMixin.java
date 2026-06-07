package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import cn.ussshenzhou.hotbaaaar.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * Loom's official Mojang mappings give 1.16.5 modern class names (Gui/GuiComponent/PoseStack/...), so
 * this is almost identical to the later loaders' GuiMixin. The only real differences are 1.16.5 API:
 * it predates the 1.17 core-shader rework (so texture setup uses {@code TextureManager.bind} +
 * {@code RenderSystem.color4f}, not {@code RenderSystem.setShader*}); {@code renderSlot} has no seed
 * arg; and {@code getCameraPlayer}/{@code renderSlot} are private (shadowed with stub bodies).
 *
 * @author USS_Shenzhou
 */
@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @Shadow
    private Player getCameraPlayer() {
        throw new AssertionError();
    }

    @Shadow
    private void renderSlot(int x, int y, float partialTick, Player player, ItemStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$renderHotbar(float partialTick, PoseStack poseStack, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        HotbaaaarClient.tickSanity();
        Inventory inv = player.inventory;
        int rows = HotbaaaarClient.getRows();

        ItemStack offhand = player.getOffhandItem();
        HumanoidArm offhandArm = player.getMainArm().getOpposite();

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
            this.blit(poseStack, x0 + i * oneHotbar, this.screenHeight - height, 0, 0, oneHotbar, height);
        }

        // selection frame at the logical selected slot
        int logicalSelected = Mth.clamp(HotbaaaarClient.getActiveLogicalRow(), 0, rows - 1) * 9 + inv.selected;
        this.blit(poseStack, x0 - 1 + logicalSelected * 20 + (logicalSelected / 9 * 2), this.screenHeight - height - 1, 0, 22, 24, 22);

        // offhand frame
        if (!offhand.isEmpty()) {
            if (offhandArm == HumanoidArm.LEFT) {
                this.blit(poseStack, x0 - 29, this.screenHeight - 23, 24, 22, 29, 24);
            } else {
                this.blit(poseStack, x1, this.screenHeight - 23, 53, 22, 29, 24);
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
            if (offhandArm == HumanoidArm.LEFT) {
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
                int x = (offhandArm == HumanoidArm.RIGHT) ? x0 - 22 : x1 + 6;
                this.minecraft.getTextureManager().bind(Util.GUI_ICONS_LOCATION);
                int progress = (int) (scale * 19.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                this.blit(poseStack, x, y, 36, 94, 18, 18);
                this.blit(poseStack, x, y + 18 - progress, 52, 94 + 18 - progress, 18, progress);
            }
        }

        ci.cancel();
    }
}
