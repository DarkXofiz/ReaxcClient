package platform;


import aethereal.core.Reaxc;
import net.fabricmc.api.ClientModInitializer;

public class Initializer implements ClientModInitializer {


    public void onInitializeClient() {
        new Reaxc();
    }
}
