package aethereal.module.movement;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.PushEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;

@ModuleRegister(name = "No Push", description = "Disables being pushed by the selected objects", category = Category.Movement)
public class NoPush extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Disable collision for", new BooleanSetting("Water and lava", false), new BooleanSetting("Blocks", false), new BooleanSetting("Entities", false), new BooleanSetting("Border", false), new BooleanSetting("Fishing rods", false));

    public NoPush() {
        a(this.b);
    }

    public MultiModeSetting q() {
        return this.b;
    }

    @EventTarget
    public void a(PushEvent event) {
        switch (event.b()) {
            case FLUIDS:
                event.a(this.b.a("Water and lava").c().booleanValue());
                break;
            case BLOCKS:
                event.a(this.b.a("Blocks").c().booleanValue());
                break;
            case ENTITIES:
                event.a(this.b.a("Entities").c().booleanValue());
                break;
            case WORLD_BORDER:
                event.a(this.b.a("Border").c().booleanValue());
                break;
            case FISHING_HOOK:
                event.a(this.b.a("Fishing rods").c().booleanValue());
                break;
        }
    }
}
