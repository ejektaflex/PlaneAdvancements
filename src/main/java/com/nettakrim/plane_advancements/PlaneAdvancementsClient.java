package com.nettakrim.plane_advancements;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "plane_advancements";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean straightLines = false;

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
		//https://www.david-colson.com/2020/03/10/exploring-rect-packing.html
		clusters.sort((a, b) -> {
			int i = Float.compare(b.size.y, a.size.y);
			return i == 0 ? Float.compare(b.size.x, a.size.x) : i;
		});

		float xPos = 0;
		float yPos = 0;
		float largestHThisRow = 0;

		// Loop over all the rectangles
		for (AdvancementCluster cluster : clusters)
		{
			// If this rectangle will go past the width of the image
			// Then loop around to next row, using the largest height from the previous row
			if ((xPos + cluster.size.x) > 10)
			{
				yPos += largestHThisRow;
				xPos = 0;
				largestHThisRow = 0;
			}

			// If we go off the bottom edge of the image, then we've failed
			if ((yPos + cluster.size.y) > 10)
				break;

			// This is the position of the rectangle
			cluster.pos.x = xPos;
			cluster.pos.y = yPos;

			// Move along to the next spot in the row
			xPos += cluster.size.x;

			// Just saving the largest height in the new row
			if (cluster.size.y > largestHThisRow)
				largestHThisRow = cluster.size.y;
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