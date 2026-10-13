package aethereal.module.player;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.SoundEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.setting.SliderSetting;

@ModuleRegister(name = "Sound Reducer", description = "Reduces the volume of the selected game sounds", category = Category.Player)
public class SoundReducer extends Module {
    private final ModeSetting b = new ModeSetting("Sound handling method", "Muffle", "Muffle", "Mute");
    private final SliderSetting c = new SliderSetting("Output sound volume", 0.5f, 0.0f, 1.0f, 0.01f).a(() -> {
        return Boolean.valueOf(this.b.l("Muffle"));
    });
    private final MultiModeSetting d = new MultiModeSetting("Select sounds", new BooleanSetting("Potion/XP throwing", true), new BooleanSetting("Creeper explosion", false), new BooleanSetting("TNT explosion", false), new BooleanSetting("Anvils", true), new BooleanSetting("Bow shot", false), new BooleanSetting("Ghast scream", false), new BooleanSetting("Skeleton steps", false), new BooleanSetting("Nether portal sound", false), new BooleanSetting("Event", true), new BooleanSetting("Wither", true), new BooleanSetting("Phantoms", true), new BooleanSetting("Trident", true), new BooleanSetting("Totem", false), new BooleanSetting("Warden", false), new BooleanSetting("Sculk Sensor", false));

    public SoundReducer() {
        a(this.b, this.d, this.c);
    }

    @EventTarget
    public void a(SoundEvent event) {
        if (a(event.getSound().getId().getPath())) {
            event.setVolume(this.b.l("Mute") ? 0.0f : event.getVolume() * this.c.c().floatValue());
        }
    }

    public boolean a(String path) {
        return (this.d.a("Potion/XP throwing").c().booleanValue() && (path.contains("splash_potion") || path.contains("experience_orb") || path.contains("lingering_potion"))) || (this.d.a("Creeper explosion").c().booleanValue() && (path.contains("creeper") || (path.contains("generic") && path.contains("explode")))) || ((this.d.a("TNT explosion").c().booleanValue() && (path.contains("tnt") || (path.contains("generic") && path.contains("explode")))) || ((this.d.a("Anvils").c().booleanValue() && path.contains("anvil")) || ((this.d.a("Bow shot").c().booleanValue() && (path.contains("arrow") || path.contains("bow"))) || ((this.d.a("Ghast scream").c().booleanValue() && path.contains("ghast")) || ((this.d.a("Skeleton steps").c().booleanValue() && path.contains("skeleton") && !path.contains("skeleton_horse")) || ((this.d.a("Nether portal sound").c().booleanValue() && path.contains("portal")) || ((this.d.a("Event").c().booleanValue() && path.contains("entity.ender_dragon.ambient")) || ((this.d.a("Wither").c().booleanValue() && path.contains("wither") && !path.contains("wither_skeleton")) || ((this.d.a("Phantoms").c().booleanValue() && path.contains("phantom")) || ((this.d.a("Trident").c().booleanValue() && path.contains("trident")) || ((this.d.a("Totem").c().booleanValue() && path.contains("totem")) || ((this.d.a("Warden").c().booleanValue() && path.contains("warden")) || (this.d.a("Sculk Sensor").c().booleanValue() && path.contains("sculk"))))))))))))));
    }
}
