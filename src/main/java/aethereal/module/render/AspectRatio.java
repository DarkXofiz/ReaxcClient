package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.RatioEvent;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;

@ModuleRegister(name = "Aspect Ratio", description = "Changes the screen aspect ratio", category = Category.Render)
public class AspectRatio extends Module {
    public final ModeSetting b = new ModeSetting("Aspect ratio", "Custom", "4:3", "16:9", "1:1", "16:10", "Custom");
    public final SliderSetting c = new SliderSetting("Ratio", 1.9f, 0.1f, 5.0f, 0.1f).a(() -> {
        return Boolean.valueOf(this.b.l("Custom"));
    });

    public AspectRatio() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(RatioEvent event) {
        event.setRatio(q());
    }

    public float q() {
        switch (this.b.c()) {
            case "4:3":
                return 1.3333334f;
            case "16:9":
                return 1.7777778f;
            case "1:1":
                return 1.0f;
            case "16:10":
                return 1.6f;
            default:
                return this.c.c().floatValue();
        }
    }
}
