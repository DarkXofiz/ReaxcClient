package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BooleanSetting;

@ModuleRegister(name = "Item Physic", description = "Adds physics to items lying on the ground", category = Category.Render)
public class ItemPhysic extends Module {
    private final BooleanSetting b = new BooleanSetting("Shrink item size", false);

    public ItemPhysic() {
        a(this.b);
    }

    public BooleanSetting q() {
        return this.b;
    }
}
