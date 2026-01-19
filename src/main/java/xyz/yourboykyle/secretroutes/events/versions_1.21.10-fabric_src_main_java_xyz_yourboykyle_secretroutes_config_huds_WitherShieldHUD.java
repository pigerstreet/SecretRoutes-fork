package xyz.yourboykyle.secretroutes.config.huds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.yourboykyle.secretroutes.events.WitherShieldManager;

import java.text.DecimalFormat;

/**
 * Minimal HUD renderer for the Wither Shield cooldown feature.
 * Renders at a fixed position (x=50, y=50) with a simple color.
 */
public class WitherShieldHUD {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");
    private static final int X = 50;
    private static final int Y = 50;
    private static final int COLOR = 0xFFFFFF; // white

    public void render(DrawContext context) {
        String text = getText();
        if (text == null || text.isEmpty()) return;

        context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                Text.literal(text),
                X,
                Y,
                COLOR
        );
    }

    private String getText() {
        long abilityEnd = WitherShieldManager.abilityEnd;
        long cooldownEnd = WitherShieldManager.cooldownEnd;
        long readyUntil = WitherShieldManager.readyUntil;

        if (abilityEnd == -1L && cooldownEnd == -1L && readyUntil == -1L) {
            return "";
        }

        long now = System.currentTimeMillis();

        // If ability is active
        if (abilityEnd > now) {
            double timeRemaining = (abilityEnd - now) / 1000.0d;
            String timer = DECIMAL_FORMAT.format(timeRemaining) + "s";
            return Formatting.DARK_PURPLE + "Wither Shield: " + Formatting.RESET + timer;
        }

        // If ability was active but just expired -> READY transient
        if (abilityEnd != -1L && readyUntil == -1L) {
            WitherShieldManager.readyUntil = now + 2_000L; // READY_DISPLAY
        }
        // clear ability end
        WitherShieldManager.abilityEnd = -1L;

        // Show READY if within the ready window
        if (WitherShieldManager.readyUntil > now) {
            return Formatting.DARK_PURPLE + "Wither Shield: " + Formatting.GREEN + "READY";
        }

        return "";
    }
}