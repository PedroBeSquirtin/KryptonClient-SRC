package skid.krypton.module.modules.render;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.EncryptedString;

public final class Fullbright extends Module {
    
    // Settings
    private final ModeSetting mode = new ModeSetting(EncryptedString.of("Mode"), "Gamma", new String[]{"Gamma", "Night Vision"});
    private final NumberSetting gammaValue = new NumberSetting(EncryptedString.of("Gamma Value"), 0.1, 100.0, 10.0, 0.5);
    private final BooleanSetting smoothTransition = new BooleanSetting(EncryptedString.of("Smooth Transition"), true);
    private final NumberSetting transitionSpeed = new NumberSetting(EncryptedString.of("Transition Speed"), 0.1, 5.0, 1.0, 0.1);
    
    private float originalGamma = -1f;
    private float currentGamma = -1f;
    private boolean nightVisionActive = false;
    private boolean wasEnabled = false;
    
    public Fullbright() {
        super(EncryptedString.of("Fullbright"), EncryptedString.of("Brightens up dark areas"), -1, Category.RENDER);
        this.addSettings(this.mode, this.gammaValue, this.smoothTransition, this.transitionSpeed);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        if (mode.getValue().equals("Gamma")) {
            // Save original gamma
            if (originalGamma == -1f) {
                originalGamma = mc.options.getGamma().getValue();
            }
            currentGamma = originalGamma;
            
            // Set target gamma
            float targetGamma = gammaValue.getFloatValue();
            
            if (smoothTransition.getValue()) {
                // Start with current gamma
                currentGamma = originalGamma;
            } else {
                // Instant change
                mc.options.getGamma().setValue(targetGamma);
            }
        } else if (mode.getValue().equals("Night Vision")) {
            // Add night vision effect
            if (mc.player != null) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
                nightVisionActive = true;
            }
        }
        
        wasEnabled = true;
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        if (mode.getValue().equals("Gamma")) {
            // Reset to original gamma
            if (originalGamma != -1f) {
                mc.options.getGamma().setValue(originalGamma);
                currentGamma = originalGamma;
            }
        } else if (mode.getValue().equals("Night Vision")) {
            // Remove night vision effect
            if (mc.player != null && nightVisionActive) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                nightVisionActive = false;
            }
        }
        
        wasEnabled = false;
    }
    
    public void onUpdate() {
        if (!isEnabled()) return;
        
        if (mode.getValue().equals("Gamma")) {
            if (smoothTransition.getValue()) {
                float targetGamma = gammaValue.getFloatValue();
                
                // Smooth transition
                if (Math.abs(currentGamma - targetGamma) > 0.01f) {
                    float speed = transitionSpeed.getFloatValue();
                    currentGamma += (targetGamma - currentGamma) * Math.min(1.0f, speed * 0.05f);
                    mc.options.getGamma().setValue(currentGamma);
                } else if (currentGamma != targetGamma) {
                    currentGamma = targetGamma;
                    mc.options.getGamma().setValue(targetGamma);
                }
            } else if (mc.options.getGamma().getValue() != gammaValue.getFloatValue()) {
                // Ensure gamma stays at the set value
                mc.options.getGamma().setValue(gammaValue.getFloatValue());
            }
        } else if (mode.getValue().equals("Night Vision")) {
            // Re-apply night vision if it gets removed
            if (mc.player != null && !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
                nightVisionActive = true;
            }
        }
    }
    
    // Helper method to get current brightness
    public float getCurrentBrightness() {
        if (mode.getValue().equals("Gamma")) {
            return mc.options.getGamma().getValue();
        } else if (mode.getValue().equals("Night Vision")) {
            return mc.player != null && mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION) ? 1.0f : 0.0f;
        }
        return 0.0f;
    }
}
