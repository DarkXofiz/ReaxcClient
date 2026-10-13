package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.TickEvent;
import aethereal.setting.SliderSetting;

@ModuleRegister(name = "See Invisibles", description = "Makes invisible players visible", category = Category.Render)
public class SeeInvisibles extends Module {
    private final SliderSetting b = new SliderSetting("Transparency", 0.5f, 0.1f, 1.0f, 0.1f);

    public SeeInvisibles() {
        a(this.b);
    }

    public SliderSetting r() {
        return this.b;
    }

    public float q() {
        return this.b.c().floatValue();
    }

    @EventTarget
    public void a(TickEvent event) {
    }
}
