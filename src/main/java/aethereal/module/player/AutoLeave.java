package aethereal.module.player;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.TickEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.setting.SliderSetting;
import aethereal.util.ChatUtil;
import aethereal.util.ServerUtil;
import net.minecraft.entity.player.PlayerEntity;

@ModuleRegister(name = "Auto Leave", description = "Automatically returns to the hub on triggers", category = Category.Player)
public class AutoLeave extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Trigger conditions", new BooleanSetting("Low HP", true), new BooleanSetting("Players nearby", true));
    private final SliderSetting c = new SliderSetting("Minimum HP", 8.0f, 1.0f, 20.0f, 0.5f).a(() -> {
        return this.b.a("Low HP").c();
    });
    private final SliderSetting d = new SliderSetting("Player distance", 8.0f, 8.0f, 128.0f, 1.0f).a(() -> {
        return this.b.a("Players nearby").c();
    });

    public AutoLeave() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (mc.world.getRegistryKey().getValue().toString().equals("minecraft:lobby")) {
            return;
        }
        if (ServerUtil.a.a$() && ServerUtil.a.d() == -1) {
            return;
        }
        PlayerEntity near = mc.world.getPlayers().stream().filter(player -> {
            return player != mc.player && !(mc.player.squaredDistanceTo(player) > ((double) (this.d.c().floatValue() * this.d.c().floatValue()))) && !Reaxc.getInstance().getModuleProcessor().e().d(player.getName().getString());
        }).findFirst().orElse(null);
        if (((this.b.a("Low HP").c().booleanValue() && mc.player.getHealth() <= this.c.c().floatValue()) || (this.b.a("Players nearby").c().booleanValue() && near != null)) && !ServerUtil.e()) {
            mc.player.networkHandler.sendChatCommand("hub");
            if (near != null) {
                ChatUtil.sendMessage("Left the anarchy: player nearby &c" + near.getName().getString() + "&7 at &c" + Math.round(Math.sqrt(mc.player.squaredDistanceTo(near))) + "&7 blocks.");
            } else {
                ChatUtil.sendMessage("Left the &canarchy: critically low health&7.");
            }
            a();
        }
    }
}
