package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import cn.ussshenzhou.hotbaaaar.util.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
 * Renders the extended hotbar on the 1.20.x {@link GuiGraphics} API: up to four 9-slot rows laid out
 * as one wide strip, items drawn at fixed logical positions (see {@link HotbaaaarClient}). Faithfully
 * reproduces the vanilla hotbar (background, selection frame, offhand slot, attack indicator)
 * generalised to N rows. Still uses the {@code widgets.png}/{@code icons.png} atlases (1.20.1).
 *
 * @author USS_Shenzhou
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract void renderSlot(GuiGraphics guiGraphics, int x, int y, float partialTick, Player player, ItemStack stack, int seed);

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$renderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        HotbaaaarClient.tickSanity();
        Inventory inv = player.getInventory();
        int rows = HotbaaaarClient.getRows();

        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        ItemStack offhand = player.getOffhandItem();
        HumanoidArm offhandArm = player.getMainArm().getOpposite();

        final int oneHotbar = Util.HOTBAR_UNIT_LENGTH;
        final int half = 91;
        final int height = Util.HOTBAR_UNIT_HEIGHT;
        int center = screenWidth / 2;
        int x0 = center - rows * half;
        int x1 = x0 + rows * oneHotbar;

        // backgrounds
        for (int i = 0; i < rows; i++) {
            guiGraphics.blit(Util.WIDGETS_LOCATION, x0 + i * oneHotbar, screenHeight - height, 0, 0, oneHotbar, height);
        }

        // selection frame at the logical selected slot
        int logicalSelected = Mth.clamp(HotbaaaarClient.getActiveLogicalRow(), 0, rows - 1) * 9 + inv.selected;
        guiGraphics.blit(Util.WIDGETS_LOCATION, x0 - 1 + logicalSelected * 20 + (logicalSelected / 9 * 2), screenHeight - height - 1, 0, 22, 24, 22);

        // offhand frame
        if (!offhand.isEmpty()) {
            if (offhandArm == HumanoidArm.LEFT) {
                guiGraphics.blit(Util.WIDGETS_LOCATION, x0 - 29, screenHeight - 23, 24, 22, 29, 24);
            } else {
                guiGraphics.blit(Util.WIDGETS_LOCATION, x1, screenHeight - 23, 53, 22, 29, 24);
            }
        }

        // items, read from the physical slot that currently holds each logical position
        int seed = 1;
        for (int i = 0; i < rows * 9; i++) {
            int logicalRow = i / 9;
            int col = i % 9;
            int physicalSlot = HotbaaaarClient.physicalRowOfLogical(logicalRow) * 9 + col;
            int x = x0 + i * 20 + 3 + (i / 9 * 2);
            int y = screenHeight - 16 - 3;
            this.renderSlot(guiGraphics, x, y, partialTick, player, inv.items.get(physicalSlot), seed++);
        }

        // offhand item
        if (!offhand.isEmpty()) {
            int y = screenHeight - 16 - 3;
            if (offhandArm == HumanoidArm.LEFT) {
                this.renderSlot(guiGraphics, x0 - 26, y, partialTick, player, offhand, seed++);
            } else {
                this.renderSlot(guiGraphics, x1 + 10, y, partialTick, player, offhand, seed++);
            }
        }

        // attack indicator
        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float scale = this.minecraft.player.getAttackStrengthScale(0.0F);
            if (scale < 1.0F) {
                int y = screenHeight - 20;
                int x = (offhandArm == HumanoidArm.RIGHT) ? x0 - 22 : x1 + 6;
                int progress = (int) (scale * 19.0F);
                guiGraphics.blit(Util.GUI_ICONS_LOCATION, x, y, 36, 94, 18, 18);
                guiGraphics.blit(Util.GUI_ICONS_LOCATION, x, y + 18 - progress, 52, 94 + 18 - progress, 18, progress);
            }
        }

        ci.cancel();
    }
}
