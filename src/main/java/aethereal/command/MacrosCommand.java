package aethereal.command;

import aethereal.core.Reaxc;
import aethereal.macro.MacrosConstructor;
import aethereal.macro.MacrosProcessor;
import aethereal.util.ChatUtil;
import aethereal.util.KeyUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.List;
import java.util.Objects;

@Command(name = "macros")
public class MacrosCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        MacrosProcessor processor = Reaxc.getInstance().getModuleProcessor().d();
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderThen = builder.then(a("add").executes(context -> {
            ChatUtil.sendMessage("Usage: .macros add <key> <command>");
            return 1;
        }).then(d("key").suggests(b()).then(c("command").executes(context2 -> {
            String key = a(context2, "key");
            String command = a(context2, "command");
            KeyUtil keyUtil = KeyUtil.a(key);
            if (keyUtil == KeyUtil.UNKNOWN) {
                ChatUtil.sendMessage("Key " + key + " not found.");
                return 1;
            }
            processor.a(key, command);
            processor.unSetup();
            ChatUtil.sendMessage("Macro " + command + " was successfully added to key " + keyUtil.b() + ".");
            return 1;
        }))));
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderExecutes = a("remove").executes(context3 -> {
            List<MacrosConstructor> macros = processor.a();
            if (macros.isEmpty()) {
                ChatUtil.sendMessage("The macro list is empty.");
                return 1;
            }
            ChatUtil.sendMessage("Commands available for removal (" + macros.size() + "):");
            for (MacrosConstructor macro : macros) {
                ChatUtil.sendMessage("  " + macro.b() + " (key: " + KeyUtil.a(macro.a()).b() + ")");
            }
            ChatUtil.sendMessage("Usage: .macros remove <command>");
            return 1;
        });
        RequiredArgumentBuilder<CommandSource, String> requiredArgumentBuilderC = c("command");
        Objects.requireNonNull(processor);
        literalArgumentBuilderThen.then(literalArgumentBuilderExecutes.then(requiredArgumentBuilderC.suggests(a(processor::a, (v0) -> {
            return v0.b();
        })).executes(context4 -> {
            String command = a(context4, "command");
            MacrosConstructor macro = processor.a().stream().filter(m -> {
                return m.b().equals(command);
            }).findFirst().orElse(null);
            if (macro == null) {
                ChatUtil.sendMessage("Macro with command " + command + " was not found in the macro list.");
                return 1;
            }
            processor.b(macro.a());
            processor.unSetup();
            ChatUtil.sendMessage("Macro " + command + " was successfully removed from key " + KeyUtil.a(macro.a()).b() + ".");
            return 1;
        }))).then(a("list").executes(context5 -> {
            List<MacrosConstructor> macros = processor.a();
            if (macros.isEmpty()) {
                ChatUtil.sendMessage("The macro list is empty.");
                return 1;
            }
            ChatUtil.sendMessage("Macros (" + macros.size() + "):");
            for (MacrosConstructor macro : macros) {
                ChatUtil.sendMessage("  " + KeyUtil.a(macro.a()).b() + ": " + macro.b());
            }
            return 1;
        })).then(a("clear").executes(context6 -> {
            ChatUtil.sendMessage("Macros successfully removed from the list: " + processor.a().size());
            processor.f();
            processor.unSetup();
            return 1;
        })).executes(context7 -> {
            ChatUtil.sendMessage("Usage: .macros <add|remove|list|clear>");
            return 1;
        });
    }
}
