package aethereal.module.player;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.DropItemEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.util.ChatUtil;
import aethereal.util.ServerUtil;

@ModuleRegister(name = "Lock Slot", description = "Prevents dropping items from selected slots", category = Category.Player)
public class LockSlot extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Locked slots", new BooleanSetting("1", false),
            new BooleanSetting("2", false), new BooleanSetting("3", false), new BooleanSetting("4", false),
            new BooleanSetting("5", false), new BooleanSetting("6", false), new BooleanSetting("7", false),
            new BooleanSetting("8", false), new BooleanSetting("9", false));
    private final BooleanSetting c = new BooleanSetting("Block only in PVP", true);

    public LockSlot() {
        a(this.c, this.b);
    }

    @EventTarget
    public void a(DropItemEvent event) {
        if ((!this.c.c().booleanValue() || ServerUtil.e()) && this.b.a(event.b()).c().booleanValue()) {
            ChatUtil.sendMessage("Attempt to drop from slot \"&c" + (event.b() + 1) + "&7\" was blocked");
            event.a(true);
        }
    }
}
