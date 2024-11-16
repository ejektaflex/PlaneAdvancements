package com.nettakrim.plane_advancements;

import net.fabricmc.api.ClientModInitializer;

import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "plane_advancements";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

	}

	public static void arrangeIntoGrid(AdvancementPositionerInterface root) {
		List<AdvancementCluster> clusters = getClusters(root);
		calculateGrid(clusters);

		for (AdvancementCluster cluster : clusters) {
			cluster.applyPosition();
		}
	}

	public static void calculateGrid(List<AdvancementCluster> clusters) {
		Vector2f pos = new Vector2f(0,0);
		for (AdvancementCluster cluster : clusters) {
			cluster.pos.set(pos);
			pos.add(cluster.size);
		}
	}

	public static List<AdvancementCluster> getClusters(AdvancementPositionerInterface root) {
		List<AdvancementPositionerInterface> positioners = root.planeAdvancements$getChildren(true);
		List<AdvancementCluster> clusters = new ArrayList<>(positioners.size());
		for (AdvancementPositionerInterface positioner : positioners) {
			clusters.add(new AdvancementCluster(positioner));
		}
		return clusters;
	}
}