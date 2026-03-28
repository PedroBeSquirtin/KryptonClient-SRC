package skid.krypton.module.modules.render;

import skid.krypton.event.EventListener;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.utils.EncryptedString;

public final class Fullbright extends Module {
    
    private double originalGamma = -1.0;
    
    public Fullbright() {
        super(EncryptedString.of("Fullbright"), EncryptedString.of("Makes everything bright"), -1, Category.RENDER);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        // Save original gamma
        if (originalGamma == -1.0) {
            originalGamma = mc.options.getGamma().getValue();
        }
        
        // Set gamma to max brightness (100.0 is very bright)
        mc.options.getGamma().setValue(100.0);
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        // Restore original gamma
        if (originalGamma != -1.0) {
            mc.options.getGamma().setValue(originalGamma);
        }
    }
    
    @EventListener
    public void onTick(TickEvent event) {
        if (!isEnabled()) return;
        
        // Keep gamma at max brightness
        if (mc.options.getGamma().getValue() != 100.0) {
            mc.options.getGamma().setValue(100.0);
        }
    }
}
