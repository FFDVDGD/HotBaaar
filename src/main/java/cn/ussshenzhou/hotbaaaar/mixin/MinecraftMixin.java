package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When any container screen opens (player inventory, chest, ...) the player may rearrange items, so we
 * reset the internal logical-to-physical map to match the real inventory order. This does not move any
 * items; it just prevents the swap bookkeeping from desyncing the rendered view.
 *
 * @author USS_Shenzhou
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void hotbaaaar$onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof AbstractContainerScreen) {
            HotbaaaarClient.resetMapping();
        }
    }
}
