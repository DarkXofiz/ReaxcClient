package aethereal.command;

import aethereal.core.Reaxc;
import aethereal.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

import java.util.List;

@Command(name = "warden")
public class WardenCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSource> builder) {
        List<Integer> anarchies = Reaxc.getInstance().getModuleProcessor().t().aU().getAnarchyList();
        builder.then(a("add").executes(context -> {
            ChatUtil.sendMessage("Usage: .warden add <anarchy>");
            return 1;
        }).then(e("anarchy").executes(context2 -> {
            int anarchy = b(context2, "anarchy");
            if (anarchy >= 1 && anarchy <= 999) {
                if (!anarchies.contains(Integer.valueOf(anarchy))) {
                    if (anarchies.size() < 10) {
                        anarchies.add(Integer.valueOf(anarchy));
                        ChatUtil.sendMessage("Anarchy " + anarchy + " added.");
                        return 1;
                    }
                    ChatUtil.sendMessage("You can add a maximum of 10 anarchies.");
                    return 1;
                }
                ChatUtil.sendMessage("Anarchy " + anarchy + " is already in the list.");
                return 1;
            }
            ChatUtil.sendMessage("Anarchy must be between 1 and 999.");
            return 1;
        }))).then(a("remove").executes(context3 -> {
            ChatUtil.sendMessage("Usage: .warden remove <anarchy>");
            return 1;
        }).then(e("anarchy").executes(context4 -> {
            int anarchy = b(context4, "anarchy");
            if (!anarchies.remove(Integer.valueOf(anarchy))) {
                ChatUtil.sendMessage("Anarchy " + anarchy + " not found.");
                return 1;
            }
            ChatUtil.sendMessage("Anarchy " + anarchy + " removed.");
            return 1;
        }))).then(a("list").executes(context5 -> {
            if (!anarchies.isEmpty()) {
                ChatUtil.sendMessage("Anarchies (" + anarchies.size() + "): " + anarchies);
                return 1;
            }
            ChatUtil.sendMessage("The anarchy list is empty.");
            return 1;
        })).then(a("clear").executes(context6 -> {
            anarchies.clear();
            ChatUtil.sendMessage("The anarchy list has been cleared.");
            return 1;
        })).executes(context7 -> {
            ChatUtil.sendMessage("Usage: .warden <add|remove|list|clear>");
            return 1;
        });
    }
}
