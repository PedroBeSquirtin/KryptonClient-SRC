package skid.krypton.module.modules.render;

import skid.krypton.event.EventListener;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;

public final class Fullbright extends Module {

    private double oldGamma = -1;

    public Fullbright() {
        super("Fullbright", "Brightens the world", -1, Category.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.player == null) return;

        // Save original gamma once
        if (oldGamma == -1) {
            oldGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.player == null) return;

        // Restore gamma
        if (oldGamma != -1) {
            mc.options.getGamma().setValue(oldGamma);
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if (!isEnabled() || mc.player == null) return;

        // Constantly force gamma
        mc.options.getGamma().setValue(16.0); // 16 is stable + not clamped
    }
}
