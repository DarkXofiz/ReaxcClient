package aethereal.module.player;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.TickEvent;
import aethereal.util.ChatUtil;

@ModuleRegister(name = "Death Coords", description = "Shows the coordinates of your last death", category = Category.Player)
public class DeathCoords extends Module {
    @EventTarget
    public void a(TickEvent event) {
        if (mc.player.deathTime == 1) {
            ChatUtil.sendMessage(String.format("You died at coordinates: &c[%d, %d, %d]", Integer.valueOf(mc.player.getBlockPos().getX()), Integer.valueOf(mc.player.getBlockPos().getY()), Integer.valueOf(mc.player.getBlockPos().getZ())));
        }
    }
}
