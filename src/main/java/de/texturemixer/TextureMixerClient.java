package de.texturemixer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class TextureMixerClient implements ClientModInitializer {

    // Standard-Taste: F6 (F5 ist in Minecraft schon der Perspektivwechsel).
    // Ändern: Optionen > Steuerung > Tastenbelegung > "Texture Pack Mixer"
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.texturemixer.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "category.texturemixer"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MixerScreen());
                }
            }
        });
    }
}
