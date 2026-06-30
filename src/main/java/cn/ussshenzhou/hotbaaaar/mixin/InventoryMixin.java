package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class InventoryMixin {

    /**
     * Intercept only when vanilla is about to change the selected hotbar slot.
     *
     * <p>Input frameworks such as MaLiLib handle and consume modifier + scroll actions earlier in
     * {@code MouseHandler.onScroll}. Injecting at HEAD and cancelling there prevents those handlers
     * (including Litematica's tool-mode and selection controls) from ever seeing the event.</p>
     */
    @Inject(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"
            ),
            cancellable = true
    )
    private void hotbaaaar$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !mc.player.isSpectator() && mc.screen == null && yOffset != 0) {
            HotbaaaarClient.onScroll(yOffset);
            ci.cancel();
        }
    }
}
