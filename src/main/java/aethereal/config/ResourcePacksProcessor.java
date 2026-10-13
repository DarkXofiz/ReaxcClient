package aethereal.config;


import aethereal.core.EventTarget;
import aethereal.event.BackendEvent;
import aethereal.network.PacketSecurity;
import aethereal.util.ChatUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ResourcePacksProcessor extends BaseProcessor {
    @Override

    public void setup() {
    }

    @Override
    public void unSetup() {
    }

    @EventTarget
    public void onBackend(BackendEvent event) {
        if (event.isReceive() && "resource-packs".equals(event.getPacket().getId())) {
            PacketSecurity security = event.getPacket().getSecurity();
            String payload = event.getPacket().getPayload();
            String pack = security.extractString(payload, "pack");
            String archive = security.extractString(payload, "archive");
            if (pack != null && archive != null) {
                File directory = new File(mc.runDirectory, "resourcepacks");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                try {
                    Files.write(new File(directory, pack + ".zip").toPath(), java.util.Base64.getDecoder().decode(archive));
                } catch (IOException e) {
                    ChatUtil.sendMessage("&c✖ &7Failed to save resource pack &a" + pack);
                    return;
                }
                ChatUtil.sendMessage("&a✔ &7Resource pack &a" + pack + " &7was successfully added to the available resource packs list.");
            }
        }
    }
}
