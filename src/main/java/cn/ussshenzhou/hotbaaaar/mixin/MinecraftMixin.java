package cn.ussshenzhou.hotbaaaar.mixin;

import cn.ussshenzhou.hotbaaaar.client.HotbaaaarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the row restore/re-apply around the player's own inventory screen:
 * <ul>
 *   <li>opening {@link InventoryScreen} -> restore canonical slot order so the GUI looks normal;</li>
 *   <li>closing it -> re-apply the active row so the held item is preserved;</li>
 *   <li>any other container (chest, creative, ...) -> just resync the internal map (we can't safely
 *       issue player-inventory clicks while a foreign container is open).</li>
 * </ul>
 *
 * @author USS_Shenzhou
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    public Screen screen;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void hotbaaaar$onSetScreen(Screen newScreen, CallbackInfo ci) {
        Screen old = this.screen;
        if (newScreen instanceof InventoryScreen) {
            HotbaaaarClient.onInventoryOpen();
        } else if (old instanceof InventoryScreen) {
            HotbaaaarClient.onInventoryClose();
        } else if (newScreen instanceof AbstractContainerScreen) {
            HotbaaaarClient.onForeignContainer();
        }
    }
}
