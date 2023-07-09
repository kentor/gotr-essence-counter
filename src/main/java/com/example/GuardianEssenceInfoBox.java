package com.example;

import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GuardianEssenceInfoBox extends InfoBox {
    public int count = 0;

    GuardianEssenceInfoBox(BufferedImage image, ExamplePlugin plugin) {
        super(image, plugin);
    }

    public String getText() {
        return String.valueOf(count);
    }

    public Color getTextColor() {
        return Color.WHITE;
    }
}
