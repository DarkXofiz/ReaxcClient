package aethereal.module.player;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.PacketEvent;
import aethereal.event.TickEvent;
import aethereal.setting.StringSetting;
import aethereal.util.ServerUtil;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

@ModuleRegister(name = "Auto Auth", description = "Automatically enters your password on login and registration", category = Category.Player)
public class AutoAuth extends Module {
    private final StringSetting b = new StringSetting("Login password", "").a();
    private final StringSetting c = new StringSetting("Registration password", "").a();
    private String password;

    public AutoAuth() {
        a(this.b, this.c);
    }

    public StringSetting q() {
        return this.b;
    }

    public StringSetting r() {
        return this.c;
    }

    public String s() {
        return this.password;
    }

    @EventTarget
    public void a(PacketEvent eventPacket) {
        if (eventPacket.isReceive()) {
            if (eventPacket.getPacket() instanceof GameMessageS2CPacket packet) {
                String message = packet.content().getString();
                if ((message.contains("Зарегистрируйтесь") || message.contains("/reg") || message.contains("/register")) && !this.c.c().isEmpty()) {
                    this.password = "/reg " + this.c.c();
                }
                if ((message.contains("Авторизуйтесь") || message.contains("Войдите в игру") || message.contains("/login")) && !this.b.c().isEmpty()) {
                    this.password = "/login " + this.b.c();
                }
            }
        }
    }

    @EventTarget
    public void a(TickEvent tickEvent) {
        if (this.password != null) {
            if (ServerUtil.a.a$() && ServerUtil.a.c()) {
                return;
            }
            mc.player.networkHandler.sendChatMessage(this.password);
            this.password = null;
        }
    }
}
