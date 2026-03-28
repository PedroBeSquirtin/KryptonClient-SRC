package skid.krypton.module.modules.render;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.EncryptedString;

public final class Fullbright extends Module {
    
    // Enum for mode selection
    public enum BrightnessMode {
        GAMMA, NIGHT_VISION
    }
    
    // Settings
    private final ModeSetting<BrightnessMode> mode = new ModeSetting<>(EncryptedString.of("Mode"), BrightnessMode.GAMMA, BrightnessMode.class);
    private final NumberSetting gammaValue = new NumberSetting(EncryptedString.of("Gamma Value"), 0.1, 100.0, 10.0, 0.5);
    private final BooleanSetting smoothTransition = new BooleanSetting(EncryptedString.of("Smooth Transition"), true);
    private final NumberSetting transitionSpeed = new NumberSetting(EncryptedString.of("Transition Speed"), 0.1, 5.0, 1.0, 0.1);
    
    private double originalGamma = -1.0;
    private double currentGamma = -1.0;
    private boolean nightVisionActive = false;
    
    public Fullbright() {
        super(EncryptedString.of("Fullbright"), EncryptedString.of("Brightens up dark areas"), -1, Category.RENDER);
        this.addSettings(this.mode, this.gammaValue, this.smoothTransition, this.transitionSpeed);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        if (mode.getValue() == BrightnessMode.GAMMA) {
            // Save original gamma
            if (originalGamma == -1.0) {
                originalGamma = mc.options.getGamma().getValue();
            }
            currentGamma = originalGamma;
            
            double targetGamma = gammaValue.getValue();
            
            if (smoothTransition.getValue()) {
                // Start with current gamma
                currentGamma = originalGamma;
            } else {
                // Instant change
                mc.options.getGamma().setValue(targetGamma);
            }
        } else if (mode.getValue() == BrightnessMode.NIGHT_VISION) {
            // Add night vision effect
            if (mc.player != null) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
                nightVisionActive = true;
            }
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        if (mode.getValue() == BrightnessMode.GAMMA) {
            // Reset to original gamma
            if (originalGamma != -1.0) {
                mc.options.getGamma().setValue(originalGamma);
                currentGamma = originalGamma;
            }
        } else if (mode.getValue() == BrightnessMode.NIGHT_VISION) {
            // Remove night vision effect
            if (mc.player != null && nightVisionActive) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                nightVisionActive = false;
            }
        }
    }
    
    @Override
    public void onUpdate() {
        if (!isEnabled()) return;
        
        if (mode.getValue() == BrightnessMode.GAMMA) {
            if (smoothTransition.getValue()) {
                double targetGamma = gammaValue.getValue();
                
                // Smooth transition
                if (Math.abs(currentGamma - targetGamma) > 0.01) {
                    double speed = transitionSpeed.getValue();
                    currentGamma += (targetGamma - currentGamma) * Math.min(1.0, speed * 0.05);
                    mc.options.getGamma().setValue(currentGamma);
                } else if (currentGamma != targetGamma) {
                    currentGamma = targetGamma;
                    mc.options.getGamma().setValue(targetGamma);
                }
            } else if (mc.options.getGamma().getValue() != gammaValue.getValue()) {
                // Ensure gamma stays at the set value
                mc.options.getGamma().setValue(gammaValue.getValue());
            }
        } else if (mode.getValue() == BrightnessMode.NIGHT_VISION) {
            // Re-apply night vision if it gets removed
            if (mc.player != null && !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
                nightVisionActive = true;
            }
        }
    }
    
    // Helper method to get current brightness
    public double getCurrentBrightness() {
        if (mode.getValue() == BrightnessMode.GAMMA) {
            return mc.options.getGamma().getValue();
        } else if (mode.getValue() == BrightnessMode.NIGHT_VISION) {
            return mc.player != null && mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION) ? 1.0 : 0.0;
        }
        return 0.0;
    }
}
