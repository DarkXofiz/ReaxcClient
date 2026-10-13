package aethereal.command;

import aethereal.config.ModuleProcessor;
import aethereal.core.Reaxc;
import aethereal.core.Module;
import aethereal.setting.BindSetting;
import aethereal.util.ChatUtil;
import aethereal.util.KeyUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Command(name = "bind")
public class BindCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        ModuleProcessor processor = Reaxc.getInstance().getModuleProcessor().t();
        LiteralArgumentBuilder<CommandSource> literalArgumentBuilderExecutes = a("add").executes(context -> {
            ChatUtil.sendMessage("Usage: .bind add <module name> <key>");
            return 1;
        });
        RequiredArgumentBuilder<CommandSource, String> requiredArgumentBuilderC = c("args");
        Objects.requireNonNull(processor);
        builder.then(literalArgumentBuilderExecutes.then(requiredArgumentBuilderC.suggests(a(processor::e, (v0) -> {
            return v0.j();
        })).executes(context2 -> {
            String args = a(context2, "args");
            if (args == null || args.trim().isEmpty()) {
                ChatUtil.sendMessage("Usage: .bind add <module name> <key>");
                return 1;
            }
            String[] parts = args.trim().split("\\s+");
            if (parts.length < 2) {
                ChatUtil.sendMessage("Usage: .bind add <module name> <key>");
                return 1;
            }
            String keyName = parts[parts.length - 1];
            String moduleName = args.substring(0, args.length() - keyName.length()).trim();
            KeyUtil key = KeyUtil.a(keyName);
            if (key == KeyUtil.UNKNOWN) {
                ChatUtil.sendMessage("Key " + keyName + " not found.");
                return 1;
            }
            Module module = processor.e().stream().filter(m -> {
                return m.j().equalsIgnoreCase(moduleName);
            }).findFirst().orElse(null);
            if (module == null) {
                ChatUtil.sendMessage("Module " + moduleName + " not found.");
                return 1;
            }
            module.a(key.a());
            processor.unSetup();
            ChatUtil.sendMessage("Module " + moduleName + " was successfully bound to key " + key.b() + ".");
            return 1;
        }))).then(a("list").executes(context3 -> {
            List<Module> modules = processor.e().stream().filter(module -> {
                return module.p() != -1 || module.e().stream().anyMatch(setting -> {
                    if (setting instanceof BindSetting bind) {
                        return bind.c().intValue() != -1;
                    }
                    return false;
                });
            }).toList();
            if (modules.isEmpty()) {
                ChatUtil.sendMessage("The list of modules with bound keys is empty.");
                return 1;
            }
            ChatUtil.sendMessage("Modules with bound keys (" + modules.size() + "):");
            modules.forEach(module2 -> {
                String binds = module2.e().stream().filter(setting -> {
                    if (setting instanceof BindSetting bind) {
                        return bind.c().intValue() != -1;
                    }
                    return false;
                }).map(setting2 -> {
                    return setting2.i() + " &8→&7 " + KeyUtil.a(((BindSetting) setting2).c().intValue()).b();
                }).collect(Collectors.joining(", "));
                ChatUtil.sendMessage(" &c" + module2.j() + (module2.p() != -1 ? " &8→&7 " + KeyUtil.a(module2.p()).b() : "") + (binds.isEmpty() ? "" : " &8[&7" + binds + "&8]"));
            });
            return 1;
        })).then(a("clear").executes(context4 -> {
            for (Module module : processor.e()) {
                module.a(-1);
            }
            processor.unSetup();
            ChatUtil.sendMessage("Bound keys were successfully cleared from all modules.");
            return 1;
        })).then(a("remove").executes(context5 -> {
            ChatUtil.sendMessage("Usage: .bind remove <key>");
            return 1;
        }).then(d("key").suggests(b()).executes(context6 -> {
            String name = a(context6, "key");
            KeyUtil key = KeyUtil.a(name);
            if (key == KeyUtil.UNKNOWN) {
                ChatUtil.sendMessage("Key " + name + " not found.");
                return 1;
            }
            List<Module> modules = processor.e().stream().filter(module -> {
                return module.p() == key.a();
            }).toList();
            if (modules.isEmpty()) {
                ChatUtil.sendMessage("No modules are bound to key " + key.b() + ".");
                return 1;
            }
            modules.forEach(module2 -> {
                module2.a(-1);
            });
            processor.unSetup();
            ChatUtil.sendMessage("Key " + key.b() + " was successfully removed from " + modules.size() + " modules.");
            return 1;
        }))).executes(context7 -> {
            ChatUtil.sendMessage("Usage: .bind <add|list|clear|remove>");
            return 1;
        });
    }
}
