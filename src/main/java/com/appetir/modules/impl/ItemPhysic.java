package com.appetir.modules.impl;

import com.appetir.modules.Module;

// ItemPhysic добавляет физику предметам на земле — они вращаются при падении.
// Реализуется через mixin на ItemEntityRenderer.
public class ItemPhysic extends Module {
    public ItemPhysic() {
        super("ItemPhysic", "Физика предметов", Category.RENDER);
    }
}
