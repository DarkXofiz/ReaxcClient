package aethereal.command;

import aethereal.core.InterfaceC0020Opcode;
import aethereal.render.ColorUtil;
import aethereal.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Command(name = "blockesp")
public class BlockESPCommand extends BaseCommand {
    private final List<a> c = new CopyOnWriteArrayList<>();

    public List<a> c() {
        return this.c;
    }

    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(a("add").executes(context -> {
            ChatUtil.sendMessage("Usage: .blockesp add <block> [color]");
            return 1;
        }).then(d("block").suggests((context2, suggestions) -> {
            Stream<String> streamLimit = Registries.BLOCK.stream().filter(block -> {
                return block != Blocks.AIR;
            }).map(block2 -> {
                return Registries.BLOCK.getId(block2).getPath();
            }).filter(path -> {
                return path.startsWith(suggestions.getRemainingLowerCase());
            }).limit(20L);
            Objects.requireNonNull(suggestions);
            streamLimit.forEach(s -> suggestions.suggest((String) s));
            return suggestions.buildFuture();
        }).executes(context3 -> {
            return a(a(context3, "block"), null);
        }).then(d("color").suggests((context4, suggestions2) -> {
            Stream<String> streamFilter = Arrays.stream(EspColor.values()).map(color -> {
                return color.name().toLowerCase();
            }).filter(name -> {
                return name.startsWith(suggestions2.getRemainingLowerCase());
            });
            Objects.requireNonNull(suggestions2);
            streamFilter.forEach(s -> suggestions2.suggest((String) s));
            return suggestions2.buildFuture();
        }).executes(context5 -> {
            return a(a(context5, "block"), a(context5, "color"));
        })))).then(a("remove").executes(context6 -> {
            ChatUtil.sendMessage("Usage: .blockesp remove <block>");
            return 1;
        }).then(d("block").suggests(a(() -> {
            return this.c;
        }, info -> {
            return Registries.BLOCK.getId(info.a).getPath();
        })).executes(context7 -> {
            String name = a(context7, "block");
            if (this.c.removeIf(info2 -> {
                return info2.a == Registries.BLOCK.get(Identifier.of(name));
            })) {
                ChatUtil.sendMessage("Block &c" + name + " &7removed successfully");
                return 1;
            }
            ChatUtil.sendMessage("Block named &c" + name + " &7is not in the list");
            return 1;
        }))).then(a("list").executes(context8 -> {
            if (this.c.isEmpty()) {
                ChatUtil.sendMessage("The block list is empty");
                return 1;
            }
            ChatUtil.sendMessage("All blocks (" + this.c.size() + "):");
            for (a info2 : this.c) {
                ChatUtil.sendMessage("— &c" + Registries.BLOCK.getId(info2.a).getPath() + (info2.b != -1 ? " &7(" + a(info2.b) + ")" : ""));
            }
            return 1;
        })).then(a("clear").executes(context9 -> {
            ChatUtil.sendMessage("Number of removed blocks: " + this.c.size());
            this.c.clear();
            return 1;
        })).executes(context10 -> {
            ChatUtil.sendMessage("Usage: .blockesp <add|remove|list|clear>");
            return 1;
        });
    }

    private int a(String name, String colorName) {
        Block block = Registries.BLOCK.get(Identifier.of(name));
        if (block != Blocks.AIR) {
            this.c.removeIf(info -> {
                return info.a == block;
            });
            this.c.add(new a(block, colorName != null ? EspColor.a(colorName) : -1));
            ChatUtil.sendMessage("Block &c" + name + " &7added successfully" + (colorName != null ? " (&c" + colorName + "&7)" : ""));
            return 1;
        }
        ChatUtil.sendMessage("Block named &c" + name + " &7not found");
        return 1;
    }

    private String a(int color) {
        for (EspColor espColor : EspColor.values()) {
            if (ColorUtil.convertToARGB(espColor.t, espColor.u, espColor.v, InterfaceC0020Opcode.al) == color) {
                return espColor.name().toLowerCase();
            }
        }
        return "custom";
    }

    public enum EspColor {
        RED(255, 0, 0),
        GREEN(0, 255, 0),
        BLUE(0, 0, 255),
        YELLOW(255, 255, 0),
        PURPLE(128, 0, 128),
        ORANGE(255, InterfaceC0020Opcode.bo, 0),
        PINK(255, InterfaceC0020Opcode.C, 203),
        CYAN(0, 255, 255),
        WHITE(255, 255, 255),
        BLACK(0, 0, 0),
        GRAY(128, 128, 128),
        BROWN(InterfaceC0020Opcode.bo, 42, 42),
        LIME(50, 205, 50),
        MAGENTA(255, 0, 255),
        GOLD(255, 215, 0),
        CORAL(255, InterfaceC0020Opcode.ce, 80),
        TURQUOISE(64, 224, 208),
        CRIMSON(220, 20, 60),
        EMERALD(80, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.bN);

        final int t;
        final int u;
        final int v;

        EspColor(final int r, final int g, final int b) {
            this.t = r;
            this.u = g;
            this.v = b;
        }

        static int a(String name) {
            try {
                EspColor espColor = valueOf(name.toUpperCase());
                return ColorUtil.convertToARGB(espColor.t, espColor.u, espColor.v, InterfaceC0020Opcode.al);
            } catch (IllegalArgumentException e) {
                return -1;
            }
        }
    }

    public record a(Block a, int b) {
        public a {
        }
    }
}
