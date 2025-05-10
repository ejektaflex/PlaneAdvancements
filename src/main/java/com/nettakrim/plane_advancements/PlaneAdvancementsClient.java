package com.nettakrim.plane_advancements;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "plane_advancements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Map<TreeType, LineType> lineType = new HashMap<>();
	public static TreeType treeType = TreeType.GRID;

	public static AdvancementWidgetInterface draggedWidget;

	@Override
	public void onInitializeClient() {
		lineType.put(TreeType.DEFAULT, LineType.DEFAULT);
		lineType.put(TreeType.SPRING, LineType.ROTATED);
		lineType.put(TreeType.GRID, LineType.SMART);
	}
}