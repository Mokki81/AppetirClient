package com.appetir.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Intentionally empty.
 *
 * Previous implementation overrode Window#getFramebufferWidth which broke
 * GUI, mouse coords, screenshots and post-processing.
 *
 * Safe aspect-ratio changes belong in projection/FOV math, not framebuffer size.
 * Module AspectRatio remains as a stub until a proper projection hook is added.
 */
@Mixin(Window.class)
public class AspectRatioMixin {
}
