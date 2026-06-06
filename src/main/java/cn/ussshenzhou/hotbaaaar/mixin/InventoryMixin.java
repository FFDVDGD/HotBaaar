package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the client-side mouse-wheel hotbar selection with the row-flipping logic. Vanilla
 * {@code swapPaint} is only ever called on the client (from {@code MouseHandler#onScroll}) against the
 * local player's inventory, and {@link HotbaaaarClient#onScroll} guards on {@code Minecraft.player},
 * so no extra side check is needed here.
 *
 * @author USS_Shenzhou
 */
@Mixin(Inventory.class)
public class InventoryMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$swapPaint(double direction, CallbackInfo ci) {
        HotbaaaarClient.onScroll(direction);
        ci.cancel();
    }
}
