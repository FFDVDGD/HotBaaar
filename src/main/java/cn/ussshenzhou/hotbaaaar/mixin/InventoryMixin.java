package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the client-side mouse-wheel hotbar selection with the row-flipping logic. Vanilla
 * {@code swapPaint} only ever runs on the client (from {@code MouseHandler#onScroll}); the guard
 * keeps us off any logical server just in case.
 *
 * @author USS_Shenzhou
 */
@Mixin(PlayerInventory.class)
public class InventoryMixin {

    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$swapPaint(double direction, CallbackInfo ci) {
        PlayerInventory self = (PlayerInventory) (Object) this;
        if (self.player.level.isClientSide) {
            HotbaaaarClient.onScroll(direction);
            ci.cancel();
        }
    }
}
