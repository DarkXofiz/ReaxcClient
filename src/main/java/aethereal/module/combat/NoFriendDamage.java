package aethereal.module.combat;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.AttackEvent;

@ModuleRegister(name = "No Friend Damage", description = "Prevents you from damaging your friends", category = Category.Combat)
public class NoFriendDamage extends Module {
    @EventTarget
    public void a(AttackEvent event) {
        event.a(Reaxc.getInstance().getModuleProcessor().e().d(event.b().getName().getString()));
    }
}
