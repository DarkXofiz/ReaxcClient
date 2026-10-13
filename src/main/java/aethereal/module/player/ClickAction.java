package aethereal.module.player;

import aethereal.core.Category;
import aethereal.core.Reaxc;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BindSetting;
import aethereal.util.ChatUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;

@ModuleRegister(name = "Click Action", description = "Performs the action bound to the selected key", category = Category.Player)
public class ClickAction extends Module {
    private final BindSetting b = new BindSetting("Ender pearl", -1).a(() -> {
        Reaxc.getInstance().getModuleProcessor().v().getUseableHandler().a(Items.ENDER_PEARL.getDefaultStack());
    });
    private final BindSetting c = new BindSetting("Add friend", -1).a(() -> {
        EntityHitResult hit = mc.crosshairTarget instanceof EntityHitResult ehr ? ehr : null;
        if (hit != null) {
            if (hit.getEntity() instanceof AbstractClientPlayerEntity targetPlayer) {
                if (targetPlayer != mc.player) {
                    String name = targetPlayer.getName().getString();
                    if (Reaxc.getInstance().getModuleProcessor().e().d(name)) {
                        Reaxc.getInstance().getModuleProcessor().e().c(name);
                        Reaxc.getInstance().getModuleProcessor().e().unSetup();
                        ChatUtil.sendMessage("Friend " + name + " was successfully removed from the friends list.");
                    } else {
                        Reaxc.getInstance().getModuleProcessor().e().b(name);
                        Reaxc.getInstance().getModuleProcessor().e().unSetup();
                        ChatUtil.sendMessage("Friend " + name + " was successfully added to the friends list.");
                    }
                }
            }
        }
    });

    public ClickAction() {
        a(this.b, this.c);
    }

    public BindSetting q() {
        return this.b;
    }

    public BindSetting r() {
        return this.c;
    }
}
