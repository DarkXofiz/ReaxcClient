package aethereal.command;

import aethereal.core.Reaxc;
import aethereal.util.BooleanUtils;
import aethereal.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

@Command(name = "gps")
public class GPSCommand extends BaseCommand {
    private Vec3d target;

    public Vec3d c() {
        return this.target;
    }

    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(a(BooleanUtils.c).executes(context -> {
            if (this.target == null) {
                ChatUtil.sendMessage("There is no GPS marker right now");
                return 1;
            }
            this.target = null;
            ChatUtil.sendMessage("The GPS marker is no longer displayed");
            return 1;
        })).then(e("x").executes(context2 -> {
            ChatUtil.sendMessage("Usage: .gps <x> <z>, .gps <x> <y> <z> or .gps off");
            return 1;
        }).then(e("y or z").executes(context3 -> {
            a(new Vec3d(b(context3, "x"), mc.player.getY(), b(context3, "y or z")));
            return 1;
        }).then(e("z").executes(context4 -> {
            a(new Vec3d(b(context4, "x"), b(context4, "y or z"), b(context4, "z")));
            return 1;
        })))).then(a("event").executes(context5 -> {
            Reaxc.getInstance().getModuleProcessor().u().c().a(WayCommand.a.GPS);
            mc.player.networkHandler.sendCommand("event delay");
            return 1;
        })).executes(context6 -> {
            ChatUtil.sendMessage("Usage: .gps <x> <z>, .gps <x> <y> <z> or .gps off");
            return 1;
        });
    }

    public void a(Vec3d pos) {
        this.target = pos;
        ChatUtil.sendMessage("GPS marker set: " + ((int) pos.getX()) + ", " + ((int) pos.getY()) + ", " + ((int) pos.getZ()));
    }
}
