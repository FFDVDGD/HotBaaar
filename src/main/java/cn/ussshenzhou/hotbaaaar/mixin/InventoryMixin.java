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

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void hotbaaaar$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !mc.player.isSpectator() && mc.screen == null && yOffset != 0) {
            HotbaaaarClient.onScroll(yOffset);
            ci.cancel();
        }
    }
}
