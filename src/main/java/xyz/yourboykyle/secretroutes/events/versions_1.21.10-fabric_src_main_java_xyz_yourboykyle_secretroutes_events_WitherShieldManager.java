package xyz.yourboykyle.secretroutes.events;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.nbt.CompoundTag;

/**
 * Minimal manager that tracks the wither shield timers and registers a UseItemCallback.
 * - ABSORPTION_COOLDOWN = 5000ms active & cooldown
 * - READY_DISPLAY = 2000ms READY display after expiry
 *
 * Public static fields are read by the HUD.
 */
public final class WitherShieldManager {

    private static final long ABSORPTION_COOLDOWN = 5_000L;
    private static final long READY_DISPLAY = 2_000L;

    // Public so HUD can access them directly in this minimal integration
    public static volatile long abilityEnd = -1L; // not active
    public static volatile long cooldownEnd = -1L; // no cooldown
    public static volatile long readyUntil = -1L; // not showing READY

    private WitherShieldManager() {}

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            try {
                ItemStack stack = player.getItemInHand((Hand) hand);
                if (stack != null && !stack.isEmpty() && stack.is(Items.IRON_SWORD) && hasWitherShieldScroll(stack)) {
                    long now = System.currentTimeMillis();
                    if (now >= cooldownEnd) {
                        abilityEnd = now + ABSORPTION_COOLDOWN;
                        cooldownEnd = now + ABSORPTION_COOLDOWN;
                        readyUntil = -1L;
                    }
                }
            } catch (Throwable t) {
                // swallow to avoid breaking other handlers; log on demand
                t.printStackTrace();
            }
            return ActionResult.PASS;
        });
    }

    private static boolean hasWitherShieldScroll(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        // Permissive check: look for the scroll marker anywhere in the serialized NBT
        try {
            String serialized = tag.toString();
            return serialized.contains("WITHER_SHIELD_SCROLL");
        } catch (Exception e) {
            return false;
        }
    }
}