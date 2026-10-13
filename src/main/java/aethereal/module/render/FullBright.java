package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.GammaEvent;
import aethereal.event.TickEvent;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@ModuleRegister(name = "Full Bright", description = "Fully lights up the world via gamma or night vision", category = Category.Render)
public class FullBright extends Module {
    private final ModeSetting b = new ModeSetting("Vision mode", "Gamma", "Gamma", "Night vision");
    private final SliderSetting c = new SliderSetting("Gamma level", 4.0f, 1.0f, 8.0f, 0.5f).a(() -> {
        return Boolean.valueOf(this.b.l("Gamma"));
    });

    public FullBright() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.l("Night vision")) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 240, 1, false, false, false));
        } else {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventTarget
    public void a(GammaEvent event) {
        if (this.b.l("Gamma")) {
            event.setGamma(this.c.c().floatValue());
        }
    }
}
