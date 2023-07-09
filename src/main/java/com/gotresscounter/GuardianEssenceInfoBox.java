package com.gotresscounter;

import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GuardianEssenceInfoBox extends InfoBox {
    protected int count = 0;

    GuardianEssenceInfoBox(BufferedImage image, GotrEssenceCounterPlugin plugin) {
        super(image, plugin);
    }

    public String getText() {
        return String.valueOf(count);
    }

    public Color getTextColor() {
        return Color.WHITE;
    }
}
