package aethereal.ambience;

import aethereal.core.Category;
import aethereal.core.EventTarget;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.event.AmbienceEvent;
import aethereal.render.ColorUtil;
import aethereal.setting.BooleanSetting;
import aethereal.setting.ColorSetting;
import aethereal.setting.ModeSetting;
import aethereal.setting.SliderSetting;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.FogShape;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.Objects;

@ModuleRegister(name = "Ambience", description = "Customizes the atmosphere and environment of the game world", category = Category.Render)
public class Ambience extends Module {
    public final ModeSetting b = new ModeSetting("World weather", "No change", "No change", "Sunny", "Rain", "Snow", "Thunderstorm");
    private final ModeSetting c = new ModeSetting("World time", "No change", "No change", "Dawn", "Day", "Noon", "Sunset", "Night", "Midnight");
    private final BooleanSetting d = new BooleanSetting("Custom fog", false);
    private final ColorSetting e;
    private final SliderSetting f;
    private final SliderSetting g;

    public Ambience() {
        ColorSetting colorSetting = new ColorSetting("Fog color", Integer.valueOf(ColorUtil.a(255, 255, 255)));
        BooleanSetting booleanSetting = this.d;
        Objects.requireNonNull(booleanSetting);
        this.e = colorSetting.a(booleanSetting::h);
        SliderSetting sliderSetting = new SliderSetting("Fog distance", -8.0f, -8.0f, 25.0f, 1.0f);
        BooleanSetting booleanSetting2 = this.d;
        Objects.requireNonNull(booleanSetting2);
        this.f = sliderSetting.a(booleanSetting2::h);
        SliderSetting sliderSetting2 = new SliderSetting("Fog density", 100.0f, 0.0f, 100.0f, 1.0f);
        BooleanSetting booleanSetting3 = this.d;
        Objects.requireNonNull(booleanSetting3);
        this.g = sliderSetting2.a(booleanSetting3::h);
        a(this.c, this.b, this.d, this.e, this.f, this.g);
    }

    @EventTarget
    public void a(AmbienceEvent.c event) {
        long jB;
        switch (this.c.c()) {
            case "Dawn":
                jB = 23041;
                break;
            case "Day":
                jB = 1000;
                break;
            case "Noon":
                jB = 6000;
                break;
            case "Sunset":
                jB = 12610;
                break;
            case "Night":
                jB = 13000;
                break;
            case "Midnight":
                jB = 18000;
                break;
            default:
                jB = event.getTime();
                break;
        }
        event.setTime(jB);
    }

    @EventTarget
    public void a(AmbienceEvent.a event) {
        if (this.d.c().booleanValue()) {
            float[] rgba = ColorUtil.a(this.e.c().intValue());
            event.setRed(rgba[0]);
            event.setGreen(rgba[1]);
            event.setBlue(rgba[2]);
            event.setAlpha(rgba[3]);
            event.a(true);
        }
    }

    @EventTarget
    public void a(AmbienceEvent.b event) {
        if (this.d.c().booleanValue()) {
            FogShape shape = event.d().shape();
            if (event.getCamera().getSubmersionType() == CameraSubmersionType.NONE) {
                shape = FogShape.SPHERE;
            }
            float[] rgba = ColorUtil.a(this.e.c().intValue());
            event.a(MathHelper.clamp(this.f.c().floatValue(), -8.0f, event.c()), MathHelper.clamp(this.f.c().floatValue() + this.g.c().floatValue(), 0.0f, event.c()), shape, rgba[0], rgba[1], rgba[2], rgba[3]);
        }
    }

    @EventTarget
    public void a(AmbienceEvent.d event) {
        float fC;
        float fC2;
        switch (event.b()) {
            case RAIN_GRADIENT:
                switch (this.b.c()) {
                    case "Sunny":
                        fC2 = 0.0f;
                        break;
                    case "Rain":
                    case "Thunderstorm":
                        fC2 = 1.0f;
                        break;
                    case "Snow":
                        fC2 = 0.9f;
                        break;
                    default:
                        fC2 = event.c();
                        break;
                }
                event.a(fC2);
                break;
            case THUNDER_GRADIENT:
                switch (this.b.c()) {
                    case "Sunny":
                    case "Rain":
                    case "Snow":
                        fC = 0.0f;
                        break;
                    case "Thunderstorm":
                        fC = 1.0f;
                        break;
                    default:
                        fC = event.c();
                        break;
                }
                event.a(fC);
                break;
            case PRECIPITATION_PARTICLES:
                if (this.b.l("Snow")) {
                    event.a(0.0f);
                }
                break;
            case PRECIPITATION:
                if (this.b.l("Snow")) {
                    event.a(Biome.Precipitation.SNOW);
                }
                break;
        }
    }
}
