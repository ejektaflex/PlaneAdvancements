package com.nettakrim.plane_advancements;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "plane_advancements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean springLineIsAngled = true;
	public static TreeType treeType = TreeType.SPRING;

	public static AdvancementWidgetInterface draggedWidget;

	@Override
	public void onInitializeClient() {

	}

	public static LineType getCurrentLineType() {
		return treeType == TreeType.SPRING ? (springLineIsAngled ? LineType.ROTATED : LineType.SMART) : LineType.DEFAULT;
	}
}