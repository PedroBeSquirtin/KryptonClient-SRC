package skid.krypton.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skid.krypton.Krypton;

@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {

    @Inject(method = "update", at = @At("TAIL"))
    private void fullbright(CallbackInfo ci) {
        // Check if your module is enabled
        if (!Krypton.INSTANCE.getModuleManager().getModuleByName("Fullbright").isEnabled()) return;

        LightmapTextureManager self = (LightmapTextureManager)(Object)this;

        // Force max brightness
        for (int i = 0; i < 256; i++) {
            self.image.setColor(i, 0xFFFFFFFF); // full white lightmap
        }
    }
}
