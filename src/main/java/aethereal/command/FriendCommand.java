package aethereal.command;

import aethereal.core.Reaxc;
import aethereal.friend.FriendConstructor;
import aethereal.friend.FriendProcessor;
import aethereal.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.Objects;

@Command(name = "friend")
public class FriendCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        FriendProcessor processor = Reaxc.getInstance().getModuleProcessor().e();
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderThen = builder.then(a("add").executes(context -> {
            ChatUtil.sendMessage("Usage: .friend add <nick>");
            return 1;
        }).then(b("nick").suggests(a()).executes(context2 -> {
            String name = a(context2, "nick");
            if (processor.d(name)) {
                ChatUtil.sendMessage("Friend " + name + " is already in the friends list.");
                return 1;
            }
            processor.b(name);
            processor.unSetup();
            ChatUtil.sendMessage("Friend " + name + " was successfully added to the friends list.");
            return 1;
        })));
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderExecutes = a("remove").executes(context3 -> {
            ChatUtil.sendMessage("Usage: .friend remove <nick>");
            return 1;
        });
        RequiredArgumentBuilder<CommandSource, String> requiredArgumentBuilderB = b("nick");
        Objects.requireNonNull(processor);
        literalArgumentBuilderThen.then(literalArgumentBuilderExecutes.then(requiredArgumentBuilderB.suggests(a(processor::a, (v0) -> {
            return v0.a();
        })).executes(context4 -> {
            String name = a(context4, "nick");
            if (!processor.d(name)) {
                ChatUtil.sendMessage("Friend " + name + " was not found in the friends list.");
                return 1;
            }
            processor.c(name);
            processor.unSetup();
            ChatUtil.sendMessage("Friend " + name + " was successfully removed from the friends list.");
            return 1;
        }))).then(a("list").executes(context5 -> {
            if (processor.a().isEmpty()) {
                ChatUtil.sendMessage("The friends list is empty.");
                return 1;
            }
            ChatUtil.sendMessage("Friends (" + processor.a().size() + "):");
            for (FriendConstructor friend : processor.a()) {
                ChatUtil.sendMessage("  - " + friend.a());
            }
            return 1;
        })).then(a("clear").executes(context6 -> {
            ChatUtil.sendMessage("Friends successfully removed from the list: " + processor.a().size());
            processor.f();
            processor.unSetup();
            return 1;
        })).executes(context7 -> {
            ChatUtil.sendMessage("Usage: .friend <add|remove|list|clear>");
            return 1;
        });
    }
}
