package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.security.Guard;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	private GuardianEssenceInfoBox guardianEssenceInfoBox;

	private int lastFragmentsCount = 0;
	private int lastEssenceCount = 0;
	private static final int MINIGAME_MAIN_REGION = 14484;

	@Override
	protected void startUp() throws Exception
	{
	}

	@Override
	protected void shutDown() throws Exception
	{
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		if (!isInMiniGame()) {
			if (guardianEssenceInfoBox != null) {
				infoBoxManager.removeInfoBox(guardianEssenceInfoBox);
				guardianEssenceInfoBox = null;
			}
			lastFragmentsCount = 0;
			lastEssenceCount = 0;
			return;
		}

		if (guardianEssenceInfoBox == null) {
			guardianEssenceInfoBox = new GuardianEssenceInfoBox(itemManager.getImage(ItemID.GUARDIAN_ESSENCE), this);
			infoBoxManager.addInfoBox(guardianEssenceInfoBox);
		}

		if (guardianEssenceInfoBox != null && isInMiniGameArea()) {
			ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

			int fragmentsCount = inventory.count(ItemID.GUARDIAN_FRAGMENTS);
			int essenceCount = inventory.count(ItemID.GUARDIAN_ESSENCE);

			// You just chiseled a fragment. Edge case: false signal if you destroyed all fragments when you only have 1.
			if (lastFragmentsCount - fragmentsCount == 1) {
				guardianEssenceInfoBox.count += 1;
			// You just mined some essence. Edge case: mining and putting into pouch happens in same tick.
			} else {
				int diffCount = essenceCount - lastEssenceCount;
				if (diffCount > 0 && guardianEssenceInfoBox != null) {
					guardianEssenceInfoBox.count += diffCount;
				}
			}

			lastFragmentsCount = fragmentsCount;
			lastEssenceCount = essenceCount;
		}
	}

	@Subscribe public void onItemQuantityChanged​(ItemQuantityChanged itemQuantityChanged) {

	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}

	private boolean isInMiniGame() {
		VarbitComposition varbit = client.getVarbit(13691);
		int value = client.getVarps()[varbit.getIndex()];
		int lsb = varbit.getLeastSignificantBit();
		int msb = varbit.getMostSignificantBit();
		int mask = (1 << ((msb - lsb) + 1)) - 1;
		return ((value >> lsb) & mask) > 0;
	}

	private boolean isInMiniGameArea() {
		WorldPoint location = client.getLocalPlayer().getWorldLocation();
		if (location == null) {
			return false;
		}
		int x = location.getX();
		int y = location.getY();
		return y >= 9484 && y <= 9521 && x >= 3588 && x <= 3643;
	}
}
