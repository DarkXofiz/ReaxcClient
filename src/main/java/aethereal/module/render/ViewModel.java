package aethereal.module.render;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.HandViewEvent;
import aethereal.setting.ButtonSetting;
import aethereal.setting.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

@ModuleRegister(name = "View Model", description = "Changes the position and size of held items", category = Category.Render)
public class ViewModel extends Module {
    private final SliderSetting b = new SliderSetting("Main hand X", 0.0f, -2.0f, 2.0f, 0.1f);
    private final SliderSetting c = new SliderSetting("Main hand Y", 0.0f, -2.0f, 2.0f, 0.1f);
    private final SliderSetting d = new SliderSetting("Main hand Z", 0.0f, -2.0f, 2.0f, 0.1f);
    private final SliderSetting e = new SliderSetting("Off hand X", 0.0f, -2.0f, 2.0f, 0.1f);
    private final SliderSetting f = new SliderSetting("Off hand Y", 0.0f, -2.0f, 2.0f, 0.1f);
    private final SliderSetting g = new SliderSetting("Off hand Z", 0.0f, -2.0f, 2.0f, 0.1f);

    public ViewModel() {
        ButtonSetting h = new ButtonSetting("Reset positions", () -> {
            e().forEach(setting -> {
                if (setting instanceof SliderSetting slider) {
                    slider.a(slider.g());
                }
            });
        });
        a(this.b, this.c, this.d, this.e, this.f, this.g, h);
    }

    @EventTarget
    public void a(HandViewEvent e) {
        MatrixStack matrix = e.getMatrices();
        if (e.d().equals(Hand.MAIN_HAND)) {
            matrix.translate(this.b.h().floatValue(), this.c.h().floatValue(), this.d.h().floatValue());
        } else {
            matrix.translate(this.e.h().floatValue(), this.f.h().floatValue(), this.g.h().floatValue());
        }
    }
}
