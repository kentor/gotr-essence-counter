package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
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
	private static final int GOTR_WIDGET_ID = 48889876;

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
	private int latestTickWithMineEssenceMessage = -1;
	private int gotrVabitValue = 0;

	@Override
	protected void shutDown() throws Exception
	{
		if (guardianEssenceInfoBox != null) {
			infoBoxManager.removeInfoBox(guardianEssenceInfoBox);
			guardianEssenceInfoBox = null;
		}
	}

	@Subscribe public void onGameTick(GameTick tick) {
		boolean isInGotr = isInGotr();

		if (!isInGotr && guardianEssenceInfoBox != null) {
			infoBoxManager.removeInfoBox(guardianEssenceInfoBox);
			guardianEssenceInfoBox = null;
		}

		if (isInGotr && guardianEssenceInfoBox == null) {
			guardianEssenceInfoBox = new GuardianEssenceInfoBox(itemManager.getImage(ItemID.GUARDIAN_ESSENCE), this);
			infoBoxManager.addInfoBox(guardianEssenceInfoBox);
		}
	}

	@Subscribe public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() != InventoryID.INVENTORY.getId()) {
			return;
		}

		ItemContainer inventory = event.getItemContainer();

		int fragmentsCount = inventory.count(ItemID.GUARDIAN_FRAGMENTS);
		int essenceCount = inventory.count(ItemID.GUARDIAN_ESSENCE);

		if (guardianEssenceInfoBox != null) {
			// Edge case: False signal if 1-stack fragment is destroyed.
			if (lastFragmentsCount - fragmentsCount == 1) {
				guardianEssenceInfoBox.count += 1;
			}

			if (latestTickWithMineEssenceMessage == client.getTickCount()) {
				int essenceDiff = essenceCount - lastEssenceCount;
				if (essenceDiff > 0) {
					guardianEssenceInfoBox.count += essenceDiff;
				}
			}
		}

		lastFragmentsCount = fragmentsCount;
		lastEssenceCount = essenceCount;
	}

	@Subscribe public void onChatMessage(ChatMessage event) {
		if (event.getType() == ChatMessageType.SPAM && event.getMessage().equals("You manage to mine some guardian essence."))	 {
			latestTickWithMineEssenceMessage = client.getTickCount();
		}

		if (event.getType() == ChatMessageType.GAMEMESSAGE &&
				guardianEssenceInfoBox != null &&
				guardianEssenceInfoBox.count != 0 &&
				(event.getMessage().equals("The Portal Guardians close their rifts.") ||
						event.getMessage().equals("The rift becomes active!") ||
						event.getMessage().contains("The rift will become active in"))) {
			guardianEssenceInfoBox.count = 0;
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getVarbitId() == 13691) {
			gotrVabitValue = varbitChanged.getValue();
		}
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

	private boolean isInGotr() {
		return gotrVabitValue == 1 || isInMiniGameArea() || client.getWidget(GOTR_WIDGET_ID) != null;
	}
}
