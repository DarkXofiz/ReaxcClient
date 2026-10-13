package aethereal.module.combat;

import aethereal.core.Category;
import aethereal.core.Reaxc;
import aethereal.core.Module;
import aethereal.core.ModuleRegister;
import aethereal.setting.BindSetting;
import aethereal.setting.ModeSetting;
import aethereal.ui.screen.SwapScreen;
import aethereal.util.InventoryUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

@ModuleRegister(name = "Auto Swap", description = "Instantly moves the selected items to the off-hand when a key is pressed", category = Category.Combat)
public class AutoSwap extends Module {
    private final ModeSetting b = new ModeSetting("Swap mode", "Double", "Double", "Triple");
    private final ModeSetting c = new ModeSetting("First item", "Sphere", "Sphere", "Totem", "Golden apple", "Shield").a(() -> {
        return Boolean.valueOf(this.b.l("Double"));
    });
    private final ModeSetting d = new ModeSetting("Second item", "Totem", "Sphere", "Totem", "Golden apple", "Shield").a(() -> {
        return Boolean.valueOf(this.b.l("Double"));
    });
    private final SwapScreen e = new SwapScreen(Text.literal("SwapMenu"));

    public AutoSwap() {
        BindSetting f = new BindSetting("Swap key", 86, 0).a(() -> {
            if (Reaxc.getInstance().getModuleProcessor().t().V().b) {
                return;
            }
            if (this.b.l("Double")) {
                Reaxc.getInstance().getModuleProcessor().v().getInventoryHandler().moveItem(InventoryUtil.c(mc.player.getOffHandStack().getItem() == a(this.c) ? a(this.d) : a(this.c)), 45, 1);
            } else if (this.b.l("Triple")) {
                mc.setScreen(this.e);
            }
        }).b(() -> {
            if (mc.currentScreen instanceof SwapScreen) {
                mc.setScreen(null);
            }
        });
        a(f, this.b, this.c, this.d);
    }

    public SwapScreen q() {
        return this.e;
    }

    private Item a(ModeSetting modeSetting) {
        switch (modeSetting.c()) {
            case "Sphere":
                return Items.PLAYER_HEAD;
            case "Totem":
                return Items.TOTEM_OF_UNDYING;
            case "Golden apple":
                return Items.GOLDEN_APPLE;
            case "Shield":
                return Items.SHIELD;
            default:
                return null;
        }
    }
}
