package aethereal.module.render;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.RemovalsEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;

@ModuleRegister(name = "Removals", description = "Removes the selected visual effects and game elements", category = Category.Render)
public class Removals extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Disabled elements", new BooleanSetting("Damage shake", true), new BooleanSetting("Scoreboard", false), new BooleanSetting("Bossbar", false), new BooleanSetting("Portal effect", true), new BooleanSetting("Fire", true), new BooleanSetting("Camera clip", true), new BooleanSetting("Break particles", false), new BooleanSetting("Black hearts", true), new BooleanSetting("Weather particles", false), new BooleanSetting("Water/lava overlay", false), new BooleanSetting("Nausea", true), new BooleanSetting("Blindness", true), new BooleanSetting("Pumpkin", true), new BooleanSetting("Glowing", true), new BooleanSetting("Darkness", true));

    public Removals() {
        a(this.b);
    }

    public MultiModeSetting q() {
        return this.b;
    }

    @EventTarget
    public void a(RemovalsEvent event) {
        switch (AnonymousClass1.a[event.b().ordinal()]) {
            case 1:
                event.a(this.b.a("Damage shake").c().booleanValue());
                break;
            case 2:
                event.a(this.b.a("Scoreboard").c().booleanValue());
                break;
            case 3:
                event.a(this.b.a("Bossbar").c().booleanValue());
                break;
            case 4:
                event.a(this.b.a("Portal effect").c().booleanValue());
                break;
            case 5:
                event.a(this.b.a("Fire").c().booleanValue());
                break;
            case 6:
                event.a(this.b.a("Camera clip").c().booleanValue());
                break;
            case 7:
                event.a(this.b.a("Break particles").c().booleanValue());
                break;
            case 8:
                event.a(this.b.a("Water/lava overlay").c().booleanValue());
                break;
            case 9:
                event.a(this.b.a("Nausea").c().booleanValue());
                break;
            case 10:
                event.a(this.b.a("Blindness").c().booleanValue());
                break;
            case 11:
                event.a(this.b.a("Pumpkin").c().booleanValue());
                break;
            case 12:
                event.a(this.b.a("Weather particles").c().booleanValue());
                break;
            case 13:
                event.a(this.b.a("Glowing").c().booleanValue());
                break;
            case InterfaceC0020Opcode.L:
                event.a(this.b.a("Darkness").c().booleanValue());
                break;
            case 15:
                event.a(this.b.a("Black hearts").c().booleanValue());
                break;
        }
    }

    static class AnonymousClass1 {
        static final int[] a = new int[RemovalsEvent.type.values().length];

        static {
            try {
                a[RemovalsEvent.type.HURT_CAM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[RemovalsEvent.type.SCOREBOARD.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[RemovalsEvent.type.BOSS_BAR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                a[RemovalsEvent.type.PORTAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                a[RemovalsEvent.type.FIRE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                a[RemovalsEvent.type.CLIP.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                a[RemovalsEvent.type.BREAK_PARTICLES.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                a[RemovalsEvent.type.WATER.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                a[RemovalsEvent.type.NAUSEA.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                a[RemovalsEvent.type.BLINDNESS.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                a[RemovalsEvent.type.PUMPKIN.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                a[RemovalsEvent.type.WEATHER.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                a[RemovalsEvent.type.GLOW.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                a[RemovalsEvent.type.DARKNESS.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                a[RemovalsEvent.type.BLACK_HEARTS.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
        }
    }
}
