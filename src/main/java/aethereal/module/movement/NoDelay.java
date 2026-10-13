package aethereal.module.movement;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.TickEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;
import net.minecraft.item.BlockItem;

@ModuleRegister(name = "No Delay", description = "Removes the delay on the selected actions", category = Category.Movement)
public class NoDelay extends Module {
    public final MultiModeSetting b = new MultiModeSetting("Disable delay on", new BooleanSetting("Block placing", true), new BooleanSetting("Jumping", true));

    public NoDelay() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.a("Block placing").c().booleanValue() && (mc.player.getMainHandStack().getItem() instanceof BlockItem) && !Reaxc.getInstance().getModuleProcessor().t().aS().m()) {
            ((platform.inject.accessors.MinecraftClientAccessor) mc).setItemUseCooldown(0);
        }
        if (this.b.a("Jumping").c().booleanValue()) {
            ((platform.inject.accessors.LivingEntityAccessor) mc.player).setJumpingCooldown(0);
        }
    }
}
