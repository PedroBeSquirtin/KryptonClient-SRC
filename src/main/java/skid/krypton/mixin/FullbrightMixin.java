package skid.krypton.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skid.krypton.Krypton;
import skid.krypton.module.Module;

@Mixin(LightmapTextureManager.class)
public class FullbrightMixin {

    @Shadow @Final private NativeImage image;

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(float delta, CallbackInfo ci) {

        Module fullbright = Krypton.INSTANCE.getModuleManager().getModuleByName("Fullbright");
        if (fullbright == null || !fullbright.isEnabled()) return;

        // Force FULL brightness
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                image.setColor(x, y, 0xFFFFFFFF);
            }
        }
    }
}
