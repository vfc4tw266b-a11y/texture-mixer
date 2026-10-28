package de.texturemixer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MixerScreen extends Screen {

    private static final int ROWS = 6;

    private final List<Path> packs = new ArrayList<>();
    // 0 = "nicht ändern", 1..n = Pack
    private final int[] selection = new int[Mixer.CATEGORIES.size()];
    private String status = "";

    public MixerScreen() {
        super(Text.literal("Texture Pack Mixer"));
        try {
            packs.addAll(Mixer.listPacks());
        } catch (IOException e) {
            status = "Packs konnten nicht gelesen werden: " + e.getMessage();
        }
    }

    private String label(int i) {
        String cat = Mixer.CATEGORIES.get(i).name();
        String value = selection[i] == 0 ? "-" : packs.get(selection[i] - 1).getFileName().toString();
        if (value.toLowerCase().endsWith(".zip")) value = value.substring(0, value.length() - 4);
        if (value.length() > 16) value = value.substring(0, 15) + "…";
        return cat + ": " + value;
    }

    @Override
    protected void init() {
        int colW = 200;
        int left = this.width / 2 - colW - 5;
        int right = this.width / 2 + 5;
        int top = 40;

        for (int i = 0; i < Mixer.CATEGORIES.size(); i++) {
            final int idx = i;
            int x = (i / ROWS == 0) ? left : right;
            int y = top + (i % ROWS) * 24;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(label(i)), b -> {
                selection[idx] = (selection[idx] + 1) % (packs.size() + 1);
                b.setMessage(Text.literal(label(idx)));
            }).dimensions(x, y, colW, 20).build());
        }

        int btnY = top + ROWS * 24 + 12;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Speichern & anwenden"), b -> save())
                .dimensions(left, btnY, colW, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Abbrechen"), b -> close())
                .dimensions(right, btnY, colW, 20).build());
    }

    private void save() {
        Map<Mixer.Category, Path> chosen = new HashMap<>();
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] > 0) chosen.put(Mixer.CATEGORIES.get(i), packs.get(selection[i] - 1));
        }
        if (chosen.isEmpty()) {
            status = "Bitte wähle mindestens eine Kategorie aus.";
            return;
        }
        status = "Mische ...";
        new Thread(() -> {
            try {
                Mixer.mix(chosen);
                client.execute(() -> {
                    client.reloadResources();
                    client.setScreen(null);
                });
            } catch (Exception e) {
                status = "Fehler: " + e.getMessage();
            }
        }, "TextureMixer").start();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Klick auf einen Button = nächstes Pack. \"-\" = nicht ändern."),
                this.width / 2, 24, 0xAAAAAA);
        if (packs.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Keine Texture Packs im resourcepacks-Ordner gefunden."),
                    this.width / 2, this.height - 40, 0xFF5555);
        }
        if (!status.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status),
                    this.width / 2, this.height - 24, 0xFFFF55);
        }
    }
}
