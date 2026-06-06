package cn.ussshenzhou.hotbaaaar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;

/**
 * Client-only state and logic for the "super long hotbar" (26.1.x variant: the held slot is accessed
 * via {@code getSelectedSlot()/setSelectedSlot()} instead of the {@code selected} field).
 * <p>
 * See the standard variant for the full design notes (row-flip via container SWAP clicks; restore on
 * inventory open, re-apply on close).
 *
 * @author USS_Shenzhou
 */
public class HotbaaaarClient {

    private static final int ROW = 9;
    private static final int MAX_ROWS = 4;

    private static final int[] physicalOfLogical = {0, 1, 2, 3};
    private static int activeLogicalRow = 0;

    private static Player lastPlayer = null;

    private static boolean restoredForInventory = false;
    private static int savedActiveRow = 0;

    private HotbaaaarClient() {
    }

    public static int getActiveLogicalRow() {
        return activeLogicalRow;
    }

    public static int physicalRowOfLogical(int logicalRow) {
        if (logicalRow < 0 || logicalRow >= MAX_ROWS) {
            return logicalRow;
        }
        return physicalOfLogical[logicalRow];
    }

    public static int getRows() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return 1;
        }
        return Mth.clamp(mc.getWindow().getGuiScaledWidth() / 182, 1, MAX_ROWS);
    }

    public static void resetMapping() {
        for (int i = 0; i < MAX_ROWS; i++) {
            physicalOfLogical[i] = i;
        }
        activeLogicalRow = 0;
    }

    public static void tickSanity() {
        Player p = Minecraft.getInstance().player;
        if (p != lastPlayer) {
            lastPlayer = p;
            restoredForInventory = false;
            resetMapping();
        }
        if (activeLogicalRow >= getRows()) {
            resetMapping();
        }
    }

    // --- inventory-screen open/close: restore on open, re-apply on close -------------------------

    public static void onInventoryOpen() {
        if (restoredForInventory) {
            return;
        }
        savedActiveRow = activeLogicalRow;
        restoreCanonical();
        restoredForInventory = true;
    }

    public static void onInventoryClose() {
        if (!restoredForInventory) {
            return;
        }
        restoredForInventory = false;
        if (savedActiveRow > 0 && savedActiveRow < getRows()) {
            activateLogicalRow(savedActiveRow);
        }
    }

    public static void onForeignContainer() {
        restoredForInventory = false;
        resetMapping();
    }

    public static void restoreCanonical() {
        for (int i = 1; i < MAX_ROWS; i++) {
            if (logicalAtPhysical(i) == i) {
                continue;
            }
            if (logicalAtPhysical(0) != i) {
                if (!swapWithHotbarTracked(physicalOfLogical[i])) {
                    break;
                }
            }
            if (!swapWithHotbarTracked(i)) {
                break;
            }
        }
        activeLogicalRow = logicalAtPhysical(0);
    }

    private static int logicalAtPhysical(int physical) {
        for (int l = 0; l < MAX_ROWS; l++) {
            if (physicalOfLogical[l] == physical) {
                return l;
            }
        }
        return physical;
    }

    private static boolean swapWithHotbarTracked(int p) {
        if (p == 0) {
            return true;
        }
        if (!swapPhysicalRowWithHotbar(p)) {
            return false;
        }
        int a = logicalAtPhysical(0);
        int b = logicalAtPhysical(p);
        physicalOfLogical[a] = p;
        physicalOfLogical[b] = 0;
        return true;
    }

    // --- scrolling ------------------------------------------------------------------------------

    public static void onScroll(double direction) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        int dir = (int) Math.signum(direction);
        if (dir == 0) {
            return;
        }
        tickSanity();
        Inventory inv = player.getInventory();
        int newSelected = inv.getSelectedSlot() - dir;
        if (newSelected < 0) {
            setSelected(inv, flipRow(-1) ? ROW - 1 : 0);
        } else if (newSelected >= ROW) {
            setSelected(inv, flipRow(1) ? 0 : ROW - 1);
        } else {
            setSelected(inv, newSelected);
        }
    }

    private static void setSelected(Inventory inv, int slot) {
        inv.setSelectedSlot(slot);
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static boolean flipRow(int delta) {
        int target = activeLogicalRow + delta;
        if (target < 0 || target >= getRows()) {
            return false;
        }
        return activateLogicalRow(target);
    }

    private static boolean activateLogicalRow(int target) {
        if (target == activeLogicalRow) {
            return true;
        }
        int targetPhysical = physicalOfLogical[target];
        if (!swapPhysicalRowWithHotbar(targetPhysical)) {
            return false;
        }
        int old = activeLogicalRow;
        physicalOfLogical[old] = targetPhysical;
        physicalOfLogical[target] = 0;
        activeLogicalRow = target;
        return true;
    }

    private static boolean swapPhysicalRowWithHotbar(int physical) {
        if (physical == 0) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gameMode == null) {
            return false;
        }
        if (player.containerMenu != player.inventoryMenu) {
            return false;
        }
        for (int col = 0; col < ROW; col++) {
            int menuSlot = physical * ROW + col;
            mc.gameMode.handleContainerInput(0, menuSlot, col, ContainerInput.SWAP, player);
        }
        return true;
    }
}
