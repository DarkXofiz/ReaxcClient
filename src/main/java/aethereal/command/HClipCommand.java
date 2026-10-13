package aethereal.command;

import aethereal.util.ChatUtil;
import aethereal.util.Rotation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Command(name = "hclip")
public class HClipCommand extends BaseCommand {
    private static Vec3d c() {
        return Vec3d.fromPolar(0.0f, Rotation.a().c());
    }

    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(a("forward").executes(context -> {
            Vec3d dir = c().normalize();
            float distance = a(dir);
            if (distance != 0.0f) {
                a(dir, distance);
                ChatUtil.sendMessage("You were successfully moved forward along your horizontal look direction");
                return 1;
            }
            return 1;
        })).then(a("back").executes(context2 -> {
            Vec3d dir = c().normalize().negate();
            float distance = a(dir);
            if (distance != 0.0f) {
                a(dir, distance);
                ChatUtil.sendMessage("You were successfully moved backward along your horizontal look direction");
                return 1;
            }
            return 1;
        })).then(f("number").executes(context3 -> {
            float distance = c(context3, "number");
            Vec3d forward = c().normalize();
            a(forward, distance);
            ChatUtil.sendMessage("You successfully moved by " + distance + " blocks along your horizontal look direction");
            return 1;
        })).executes(context4 -> {
            ChatUtil.sendMessage("Usage: .hclip <number|forward|back>");
            return 1;
        });
    }

    private void a(Vec3d horizontalDirection, float distance) {
        Vec3d delta = horizontalDirection.multiply(distance);
        double x = mc.player.getX() + delta.x;
        double y = mc.player.getY();
        double z = mc.player.getZ() + delta.z;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, mc.player.isOnGround(), mc.player.horizontalCollision));
        mc.player.setPosition(x, y, z);
    }

    private float a(Vec3d direction) {
        Vec3d unit = direction.normalize();
        Vec3d origin = mc.player.getPos();
        for (int blocks = 1; blocks <= 255; blocks++) {
            BlockPos here = BlockPos.ofFloored(origin.add(unit.multiply(blocks)));
            BlockPos ahead = BlockPos.ofFloored(origin.add(unit.multiply(blocks + 1)));
            if (mc.world.getBlockState(here).isAir() && mc.world.getBlockState(ahead).isAir()) {
                return blocks + 1;
            }
            if (mc.world.getBlockState(here).isOf(Blocks.BEDROCK)) {
                ChatUtil.sendMessage(Formatting.GRAY + "Teleporting to this location is not possible");
                return 0.0f;
            }
        }
        return 0.0f;
    }
}
