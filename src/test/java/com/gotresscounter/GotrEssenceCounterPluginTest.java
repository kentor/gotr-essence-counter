package com.gotresscounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GotrEssenceCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GotrEssenceCounterPlugin.class);
		RuneLite.main(args);
	}
}