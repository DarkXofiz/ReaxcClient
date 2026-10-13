package aethereal.command;

import aethereal.core.Reaxc;
import aethereal.staff.StaffConstructor;
import aethereal.staff.StaffProcessor;
import aethereal.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.Objects;

@Command(name = "staff")
public class StaffCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        StaffProcessor processor = Reaxc.getInstance().getModuleProcessor().f();
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderThen = builder.then(a("add").executes(context -> {
            ChatUtil.sendMessage("Usage: .staff add <nick>");
            return 1;
        }).then(b("nick").suggests(a()).executes(context2 -> {
            String name = a(context2, "nick");
            if (processor.d(name)) {
                ChatUtil.sendMessage("Staff " + name + " is already in the staff list.");
                return 1;
            }
            processor.b(name);
            processor.unSetup();
            ChatUtil.sendMessage("Staff " + name + " was successfully added to the staff list.");
            return 1;
        })));
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderExecutes = a("remove").executes(context3 -> {
            ChatUtil.sendMessage("Usage: .staff remove <nick>");
            return 1;
        });
        RequiredArgumentBuilder<CommandSource, String> requiredArgumentBuilderB = b("nick");
        Objects.requireNonNull(processor);
        literalArgumentBuilderThen.then(literalArgumentBuilderExecutes.then(requiredArgumentBuilderB.suggests(a(processor::a, (v0) -> {
            return v0.a();
        })).executes(context4 -> {
            String name = a(context4, "nick");
            if (!processor.d(name)) {
                ChatUtil.sendMessage("Staff " + name + " was not found in the staff list.");
                return 1;
            }
            processor.c(name);
            processor.unSetup();
            ChatUtil.sendMessage("Staff " + name + " was successfully removed from the staff list.");
            return 1;
        }))).then(a("list").executes(context5 -> {
            if (processor.a().isEmpty()) {
                ChatUtil.sendMessage("The staff list is empty.");
                return 1;
            }
            ChatUtil.sendMessage("Staff (" + processor.a().size() + "):");
            for (StaffConstructor staffConstructor : processor.a()) {
                ChatUtil.sendMessage("  - " + staffConstructor.a());
            }
            return 1;
        })).then(a("clear").executes(context6 -> {
            ChatUtil.sendMessage("Staff successfully removed from the list: " + processor.a().size());
            processor.f();
            processor.unSetup();
            return 1;
        })).executes(context7 -> {
            ChatUtil.sendMessage("Usage: .staff <add|remove|list|clear>");
            return 1;
        });
    }
}
