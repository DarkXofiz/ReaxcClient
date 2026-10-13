package aethereal.module.misc;

import aethereal.autobuy.AutoBuyEntry;
import aethereal.core.Category;
import aethereal.core.Reaxc;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BindSetting;
import aethereal.setting.ModeSetting;
import aethereal.ui.screen.AssistantScreen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

@ModuleRegister(name = "Potion Thrower", description = "Quickly throw buffs via a wheel or a key", category = Category.Misc)
public class PotionThrower extends Module {
    private final ModeSetting b = new ModeSetting("Potion usage method", "Selection wheel", "Selection wheel", "Key");
    private final AssistantScreen c = new AssistantScreen(Text.literal("Potion Thrower"));

    public PotionThrower() {
        BindSetting d = new BindSetting("Open potion menu", 86, 0).a(() -> {
            mc.setScreen(this.c);
        }).b(() -> {
            if (mc.currentScreen == this.c) {
                this.c.b(this.c.b());
                if (mc.currentScreen == this.c) {
                    mc.setScreen(null);
                }
            }
        }).a(() -> {
            return Boolean.valueOf(this.b.l("Selection wheel"));
        });
        a(this.b, d);
        for (AutoBuyEntry potion : AutoBuyEntry.values()) {
            if (potion.getItem() == Items.SPLASH_POTION) {
                a(new BindSetting(potion.getDisplayName(), -1).a(() -> {
                    Reaxc.getInstance().getModuleProcessor().v().getUseableHandler().a(potion.a());
                }).a(() -> {
                    return Boolean.valueOf(this.b.l("Key"));
                }));
            }
        }
    }

    public AssistantScreen q() {
        return this.c;
    }
}
