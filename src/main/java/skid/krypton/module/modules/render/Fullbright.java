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
        
        // Set gamma to extremely high value
        mc.options.getGamma().setValue(100.0);
        
        // Force a reload of the options
        mc.options.write();
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        // Restore original gamma
        if (originalGamma != -1.0) {
            mc.options.getGamma().setValue(originalGamma);
            mc.options.write();
        }
    }
    
    @EventListener
    public void onTick(TickEvent event) {
        if (!isEnabled()) return;
        
        // Force gamma to stay at max every tick
        double currentGamma = mc.options.getGamma().getValue();
        if (currentGamma < 50.0) {
            mc.options.getGamma().setValue(100.0);
        }
    }
}
