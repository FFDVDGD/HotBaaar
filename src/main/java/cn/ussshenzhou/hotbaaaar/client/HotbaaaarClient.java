package cn.ussshenzhou.hotbaaaar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;

/**
 * Client-only state and logic for the "super long hotbar".
 * <p>
 * The real hotbar is always vanilla physical slots 0-8. We present the whole inventory as a wide
 * strip of up to four 9-slot rows and let the selection glide across them. To use a row other than
 * the one currently in the real hotbar, that row is physically swapped into slots 0-8 via container
 * SWAP clicks (which a vanilla server accepts), so no server-side mod is required.
 * <p>
 * Items are rendered at fixed <i>logical</i> positions; {@link #physicalOfLogical} tracks which
 * physical row currently holds each logical row so the swap stays invisible. We never auto-restore
 * the inventory order; we only reset this internal map (see {@link #resetMapping()}) when a container
 * screen opens or the player instance changes, so external rearrangement can't desync the view.
 *
 * @author USS_Shenzhou
 */
public class HotbaaaarClient {

    private static final int ROW = 9;
    private static final int MAX_ROWS = 4;

    /** {@code physicalOfLogical[logicalRow]} = physical row (0..3) currently holding that logical row's items. */
    private static final int[] physicalOfLogical = {0, 1, 2, 3};
    /** The logical row whose items currently sit in the real hotbar (physical row 0). */
    private static int activeLogicalRow = 0;

    private static Player lastPlayer = null;

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

    /** Number of addressable rows, mirroring the original width-based behaviour. */
    public static int getRows() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return 1;
        }
        return Mth.clamp(mc.getWindow().getGuiScaledWidth() / 182, 1, MAX_ROWS);
    }

    /** Reset the logical-to-physical map to identity without moving any items. */
    public static void resetMapping() {
        for (int i = 0; i < MAX_ROWS; i++) {
            physicalOfLogical[i] = i;
        }
        activeLogicalRow = 0;
    }

    /**
     * Keep internal state consistent: reset when the player changes (login / respawn / dimension)
     * or when the window shrank below the active row. Called from rendering and scrolling.
     */
    public static void tickSanity() {
        Player p = Minecraft.getInstance().player;
        if (p != lastPlayer) {
            lastPlayer = p;
            resetMapping();
        }
        if (activeLogicalRow >= getRows()) {
            resetMapping();
        }
    }

    /**
     * Replacement for vanilla {@code Inventory.swapPaint}: glide the selection within the active row,
     * and flip to the adjacent row (swapping it into the real hotbar) when scrolling past an edge.
     */
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
        int newSelected = inv.selected - dir;
        if (newSelected < 0) {
            // past the left edge -> previous row
            setSelected(inv, flipRow(-1) ? ROW - 1 : 0);
        } else if (newSelected >= ROW) {
            // past the right edge -> next row
            setSelected(inv, flipRow(1) ? 0 : ROW - 1);
        } else {
            setSelected(inv, newSelected);
        }
    }

    private static void setSelected(Inventory inv, int slot) {
        inv.selected = slot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    /** @return true if the row was actually flipped. */
    private static boolean flipRow(int delta) {
        int target = activeLogicalRow + delta;
        if (target < 0 || target >= getRows()) {
            return false;
        }
        return activateLogicalRow(target);
    }

    /** Bring logical row {@code target} into the real hotbar (physical row 0). */
    private static boolean activateLogicalRow(int target) {
        if (target == activeLogicalRow) {
            return true;
        }
        int targetPhysical = physicalOfLogical[target];
        if (!swapPhysicalRowWithHotbar(targetPhysical)) {
            return false;
        }
        // The old active row (was at physical 0) now lives where the target used to be, and vice-versa.
        int old = activeLogicalRow;
        physicalOfLogical[old] = targetPhysical;
        physicalOfLogical[target] = 0;
        activeLogicalRow = target;
        return true;
    }

    /**
     * Swap physical row {@code physical} (1..3) with the real hotbar (physical row 0) via nine SWAP
     * container clicks against the player inventory menu. Only valid while no other screen is open.
     *
     * @return true if the swap was performed (state should be updated), false otherwise.
     */
    private static boolean swapPhysicalRowWithHotbar(int physical) {
        if (physical == 0) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gameMode == null) {
            return false;
        }
        // The player inventory menu (id 0) must be the active container, i.e. no chest/etc. open.
        if (player.containerMenu != player.inventoryMenu) {
            return false;
        }
        for (int col = 0; col < ROW; col++) {
            // InventoryMenu slot index for inventory index (physical*9 + col), physical >= 1, equals the same number.
            int menuSlot = physical * ROW + col;
            mc.gameMode.handleInventoryMouseClick(0, menuSlot, col, ClickType.SWAP, player);
        }
        return true;
    }
}
