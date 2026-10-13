package aethereal.module.misc;

import aethereal.core.Category;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BooleanSetting;

@ModuleRegister(name = "No Interact", description = "Blocks accidental interaction with containers and blocks", category = Category.Misc)
public class NoInteract extends Module {
    private final BooleanSetting b = new BooleanSetting("Account for enabled Aura", true);

    public NoInteract() {
        a(this.b);
    }

    public BooleanSetting q() {
        return this.b;
    }
}
