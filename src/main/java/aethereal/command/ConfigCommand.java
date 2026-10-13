package aethereal.command;

import aethereal.config.ModuleProcessor;
import aethereal.core.Reaxc;
import aethereal.core.Module;
import aethereal.setting.Setting;
import aethereal.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Util;

import java.io.File;

@Command(name = "cfg")
public class ConfigCommand extends BaseCommand {
    @SuppressWarnings("unchecked")
    private static <T> void resetSettingValue(Setting<?> setting) {
        ((Setting<T>) setting).a((T) setting.g());
    }

    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        ModuleProcessor processor = Reaxc.getInstance().getModuleProcessor().t();
        builder.then(a("save").executes(context -> {
            ChatUtil.sendMessage("Usage: .cfg save <name>");
            return 1;
        }).then(b("name").executes(context -> {
            String configName = a(context, "name");
            processor.b(configName);
            ChatUtil.sendMessage("Config " + configName + " was saved successfully.");
            return 1;
        }))).then(a("load").executes(context -> {
            ChatUtil.sendMessage("Usage: .cfg load <name>");
            return 1;
        }).then(b("name").suggests(c()).executes(context -> {
            String configName = a(context, "name");
            if (processor.c(configName)) {
                ChatUtil.sendMessage("Config " + configName + " was loaded successfully.");
                return 1;
            }
            ChatUtil.sendMessage("Config " + configName + " not found.");
            return 1;
        }))).then(a("list").executes(context -> {
            File configDir = processor.d();
            File[] configFiles = configDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (configFiles == null || configFiles.length == 0) {
                ChatUtil.sendMessage("The config list is empty.");
                return 1;
            }
            ChatUtil.sendMessage("Configs (" + configFiles.length + "):");
            for (File configFile : configFiles) {
                ChatUtil.sendMessage("  - " + configFile.getName());
            }
            return 1;
        })).then(a("reset").executes(context -> {
            for (Module module : processor.e()) {
                module.a(false);
                module.a(-1);
                for (Setting<?> setting : module.e()) {
                    if (setting.g() != null) {
                        resetSettingValue(setting);
                    }
                }
            }
            ChatUtil.sendMessage("All modules were reset to their default state.");
            return 1;
        })).then(a("remove").executes(context -> {
            ChatUtil.sendMessage("Usage: .cfg remove <name>");
            return 1;
        }).then(b("name").suggests(c()).executes(context -> {
            String configName = a(context, "name");
            if (processor.d(configName)) {
                ChatUtil.sendMessage("Config " + configName + " was deleted successfully.");
                return 1;
            }
            ChatUtil.sendMessage("Config " + configName + " not found.");
            return 1;
        }))).then(a("dir").executes(context -> {
            File configDir = processor.d();
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            ChatUtil.sendMessage("Config folder: " + configDir.getAbsolutePath());
            Util.getOperatingSystem().open(configDir);
            return 1;
        })).executes(context -> {
            ChatUtil.sendMessage("Usage: .cfg <load|save|list|reset|remove|dir>");
            return 1;
        });
    }

    private SuggestionProvider<CommandSource> c() {
        return (context, builder) -> {
            ModuleProcessor processor = Reaxc.getInstance().getModuleProcessor().t();
            File configDir = processor.d();
            File[] configFiles = configDir.exists() ? configDir.listFiles((dir, name) -> name.endsWith(".json")) : null;
            if (configFiles != null) {
                for (File configFile : configFiles) {
                    String name = configFile.getName();
                    builder.suggest(name.substring(0, name.length() - 5));
                }
            }
            return builder.buildFuture();
        };
    }
}
