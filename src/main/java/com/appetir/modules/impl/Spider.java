package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

// Spider — лазание по стенам.
// Реализуется через mixin на LivingEntity#isClimbing()
public class Spider extends Module {

    public Spider() {
        super("Spider", "Позволяет лазить по стенам, используя блоки или ведро с водой", Category.MOVEMENT);
    }
}
