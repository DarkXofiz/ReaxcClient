package aethereal.module.misc;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.InputEvent;
import aethereal.event.PacketEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.MultiModeSetting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Hand;

@ModuleRegister(name = "Anti AFK", description = "Stops the server from kicking you for inactivity", category = Category.Player)
public class AntiAFK extends Module {
    private final ModeSetting b = new ModeSetting("Usage mode", "Normal", "Normal", "FunTime");
    private final MultiModeSetting c = new MultiModeSetting("Perform actions", new BooleanSetting("Jump", true), new BooleanSetting("Swing", true), new BooleanSetting("Movement", true)).a(() -> {
        return Boolean.valueOf(this.b.l("Normal"));
    });
    private final BooleanSetting d = new BooleanSetting("React to unavailability", false).a(() -> {
        return Boolean.valueOf(this.b.l("FunTime"));
    });

    public AntiAFK() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(InputEvent event) {
        if (mc.player.age % 600 == 0) {
            if (this.b.l("Normal")) {
                if (this.c.a("Jump").c().booleanValue() && mc.player.isOnGround()) {
                    event.setJump(true);
                }
                if (this.c.a("Swing").c().booleanValue()) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
                if (this.c.a("Movement").c().booleanValue()) {
                    Reaxc.getInstance().getModuleProcessor().v().getAFKHandler().a(7);
                    return;
                }
                return;
            }
            if (this.b.l("FunTime") && !this.d.c().booleanValue()) {
                Reaxc.getInstance().getModuleProcessor().v().getAFKHandler().a(7);
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.b.l("FunTime") && this.d.c().booleanValue() && event.isReceive()) {
            GameMessageS2CPacket message = (GameMessageS2CPacket) event.getPacket();
            if (message instanceof GameMessageS2CPacket) {
                if (message.content().getString().equals("Данная команда недоступна в режиме AFK")) {
                    Reaxc.getInstance().getModuleProcessor().v().getAFKHandler().a(7);
                }
            }
        }
    }
}
