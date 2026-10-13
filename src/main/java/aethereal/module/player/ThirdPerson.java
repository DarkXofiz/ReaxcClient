package aethereal.module.player;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.KeyEvent;
import aethereal.event.TickEvent;
import aethereal.lib.javassist.TokenId;
import aethereal.setting.BindSetting;
import aethereal.setting.ModeSetting;
import aethereal.util.Look;
import aethereal.util.MathUtil;
import aethereal.util.Rotation;
import net.minecraft.client.option.Perspective;

@ModuleRegister(name = "Third Person", description = "Free third-person view without changing movement direction", category = Category.Player)
public class ThirdPerson extends Module {
    private final ModeSetting b = new ModeSetting("Look activation mode", "On press", "On press", "On hold");
    private boolean isActive;
    private Rotation rotation;

    public ThirdPerson() {
        BindSetting d = new BindSetting("Look key", Integer.valueOf(TokenId.Q_), 0).a(() -> {
            if (this.b.l("On hold")) {
                d(true);
            } else {
                d(!this.isActive);
            }
        }).b(() -> {
            if (this.isActive && this.b.l("On hold")) {
                d(false);
            }
        });
        a(this.b, d);
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (this.isActive && event.getKey() == mc.options.togglePerspectiveKey.getDefaultKey().getCode()) {
            event.a(true);
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.isActive) {
            if (mc.currentScreen != null) {
                d(false);
            } else {
                Reaxc.getInstance().getModuleProcessor().k().startAiming(
                        new Rotation(mc.player.getYaw(), MathUtil.b(mc.player.getPitch(), -89.0f, 89.0f)), 360.0f, 0,
                        1);
            }
        }
    }

    private void d(boolean active) {
        if (active) {
            this.rotation = new Rotation(Look.b(), Look.c());
        } else {
            Look.a(this.rotation.c());
            Look.b(this.rotation.d());
        }
        mc.options.setPerspective(active ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON);
        this.isActive = active;
    }
}
