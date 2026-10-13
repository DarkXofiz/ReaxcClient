package aethereal.module.render;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.HandAnimationEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

@ModuleRegister(name = "Swing Animation", description = "Customizes the hand swing animation", category = Category.Render)
public class SwingAnimation extends Module {
    private final BooleanSetting b = new BooleanSetting("Account for enabled Aura", true);
    private final ModeSetting c = new ModeSetting("Animation mode", "Mode 1", "Mode 1", "Mode 2", "Mode 3", "Mode 4", "Mode 5");
    private final SliderSetting d = new SliderSetting("Rotation angle", 75.0f, 0.0f, 360.0f, 1.0f).a(() -> {
        return Boolean.valueOf(this.c.l("Mode 1"));
    });
    private final SliderSetting e = new SliderSetting("Tip tilt", -20.0f, -90.0f, 90.0f, 1.0f).a(() -> {
        return Boolean.valueOf(!this.c.l("Mode 5"));
    });
    private final SliderSetting f = new SliderSetting("Swing intensity", 5.0f, 1.0f, 10.0f, 1.0f);

    public SwingAnimation() {
        a(this.b, this.c, this.d, this.e, this.f);
    }

    public BooleanSetting q() {
        return this.b;
    }

    public ModeSetting r() {
        return this.c;
    }

    public SliderSetting s() {
        return this.d;
    }

    public SliderSetting t() {
        return this.e;
    }

    public SliderSetting u() {
        return this.f;
    }

    @EventTarget
    public void a(HandAnimationEvent event) {
        if ((!this.b.c().booleanValue() || Reaxc.getInstance().getModuleProcessor().t().B().getTarget() != null) && event.getHand() == Hand.MAIN_HAND) {
            MatrixStack matrices = event.getMatrixStack();
            float anim = (float) Math.sin(((double) event.getSwingProgress()) * 3.1415936112270124d);
            float power = this.f.c().floatValue() * 10.0f;
            int arm = event.getArmX();
            matrices.translate(arm * (this.c.l("Mode 5") ? 0.5f : 0.72f), -0.5f, this.c.l("Mode 5") ? -0.72f : -1.0f);
            if (!this.c.l("Mode 5")) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-this.e.c().floatValue()));
            }
            switch (this.c.c()) {
                case "Mode 1":
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * 90));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm * (-70)));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((-this.d.c().floatValue()) - (power * anim)));
                    break;
                case "Mode 2":
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * 90));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm * (-65)));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((-65.0f) + (power * anim)));
                    break;
                case "Mode 3":
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * (-90)));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm * 60));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0f));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm * power * anim));
                    break;
                case "Mode 4":
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * 90));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm * (-75)));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((-45.0f) - (power * anim)));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * power * anim * 0.5f));
                    break;
                case "Mode 5":
                    float strength = power / 80.0f;
                    float swing = anim * anim;
                    float twist = (float) Math.sin(((double) (event.getSwingProgress() * event.getSwingProgress())) * 3.1415936112270124d);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * (45.0f + (twist * (-20.0f) * strength))));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm * swing * (-22.0f) * strength));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swing * (-85.0f) * strength));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm * (-45.0f)));
                    break;
            }
            event.a(true);
        }
    }
}
