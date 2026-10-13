package aethereal.module.player;

import aethereal.core.*;
import aethereal.core.Module;
import aethereal.event.PacketEvent;
import aethereal.setting.BooleanSetting;
import aethereal.setting.MultiModeSetting;
import aethereal.util.StringUtils;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ModuleRegister(name = "Auto Accept", description = "Automatically accepts the selected requests", category = Category.Player)
public class AutoAccept extends Module {
    public final MultiModeSetting b = new MultiModeSetting("Accept requests", new BooleanSetting("To clan team", true), new BooleanSetting("Teleports", true));
    private final BooleanSetting c = new BooleanSetting("Accept friends only", true);
    private final List<String> d = List.of("просит телепортироваться", "хочет телепортироваться", "Заявка буудет автоматически отменена через", "просит телепортироваться к Вам.", "120 секунд", "has requested to teleport", "teleport to you", "This request will timeout after", "120 seconds");
    private final List<String> e = List.of("приглашает вас в клан", "invites you to the clan");

    public AutoAccept() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.isReceive()) {
            if (event.getPacket() instanceof GameMessageS2CPacket packet) {
                String chat = packet.content().getString().toLowerCase();
                if (this.c.c().booleanValue() && Reaxc.getInstance().getModuleProcessor().e().a().stream().noneMatch(friend -> {
                    return chat.contains(friend.a().toLowerCase());
                })) {
                    return;
                }
                if (this.b.a("Teleports").c().booleanValue()) {
                    Stream<String> stream = this.d.stream();
                    Objects.requireNonNull(chat);
                    if (stream.anyMatch((v1) -> {
                        return chat.contains(v1);
                    })) {
                        mc.player.networkHandler.sendChatCommand("tpaccept");
                    }
                }
                if (this.b.a("To clan team").c().booleanValue()) {
                    Stream<String> stream2 = this.e.stream();
                    Objects.requireNonNull(chat);
                    if (stream2.anyMatch((v1) -> {
                        return chat.contains(v1);
                    })) {
                        mc.player.networkHandler.sendChatCommand("clan accept " + chat.split(StringUtils.a)[1]);
                    }
                }
            }
        }
    }
}
