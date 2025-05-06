package com.nettakrim.plane_advancements;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "plane_advancements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static LineType lineType = LineType.SMART;
	private static final float springScale = 32f;

	public static AdvancementWidgetInterface draggedWidget;

	@Override
	public void onInitializeClient() {

	}

	public static void arrangeIntoGrid(AdvancementWidgetInterface root) {
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

	public static List<AdvancementCluster> getClusters(AdvancementWidgetInterface root) {
		List<AdvancementWidgetInterface> clusterRoots = root.planeAdvancements$getChildren();
		clusterRoots.add(root);
		List<AdvancementCluster> clusters = new ArrayList<>(clusterRoots.size());
		for (AdvancementWidgetInterface clusterRoot : clusterRoots) {
			clusters.add(new AdvancementCluster(clusterRoot));
		}
		return clusters;
	}

	public static void renderLines(DrawContext context, int x, int y, int startX, int startY, int endX, int endY, boolean border) {
		int offsetX = endX-startX;
		int offsetY = endY-startY;

		MatrixStack matrixStack = context.getMatrices();
		matrixStack.push();
		matrixStack.translate(x+startX + 15.5, y+startY + 12.5, 0);

		if (lineType == LineType.ROTATED) {
			matrixStack.multiply(new Quaternionf(new AxisAngle4f((float)Math.atan2(offsetY, offsetX), 0, 0, 1)));
			int distance = MathHelper.floor(MathHelper.sqrt(offsetX*offsetX + offsetY*offsetY));
			if (border) {
				context.drawHorizontalLine(0, distance, -1, -16777216);
				context.drawHorizontalLine(0, distance, 1, -16777216);
			} else {
				context.drawHorizontalLine(0, distance, 0, -1);
			}
		} else {
			int absX = MathHelper.abs(offsetX);
			int absY = MathHelper.abs(offsetY);
			boolean isX = absX < absY;
			int xPos = isX ? (absX < 15 ? offsetX / 2 : offsetX) : 0;
			int yPos = !isX ? (absY < 15 ? offsetY / 2 : offsetY) : 0;
			int xLength = absX < 15 ? 0 : offsetX;
			int yLength = absY < 15 ? 0 : offsetY;

			if (border) {
				offsetX /= absX == 0 ? 1 : absX;
				offsetY /= absY == 0 ? 1 : absY;
				context.drawHorizontalLine(-offsetX, xLength+offsetX, yPos-1, -16777216);
				context.drawHorizontalLine(-offsetX, xLength+offsetX, yPos+1, -16777216);
				context.drawVerticalLine(xPos-1, yLength+offsetY, -offsetY, -16777216);
				context.drawVerticalLine(xPos+1, yLength+offsetY, -offsetY, -16777216);
			} else {
				context.drawHorizontalLine(0, xLength, yPos, -1);
				context.drawVerticalLine(xPos, yLength, 0, -1);
			}
		}

		matrixStack.pop();
	}

	public static void applySpringForce(AdvancementWidgetInterface a, AdvancementWidgetInterface b, float attraction, float repulsion) {
		//TODO: TEMP DISABLE
		if (repulsion < 100) {
			return;
		}

		// if the attraction force is big enough, the two advancements being attracted would collide in a single step
		assert attraction < springScale/2f;

		Vector2f direction = new Vector2f(a.planeAdvancements$getPos());
		float distance = b.planeAdvancements$getPos().distance(direction)/springScale;
		if (distance == 0) {
			return;
		}

		direction.sub(b.planeAdvancements$getPos());

		if (a.planeAdvancements$isConnected(b) && distance > 1) {
			direction.normalize(distance*-attraction);
		} else {
			direction.normalize(repulsion/Math.max(distance*distance, 0.01f));
		}

		if (a != PlaneAdvancementsClient.draggedWidget) {
			a.planeAdvancements$getPos().add(direction);
		} if (b != PlaneAdvancementsClient.draggedWidget) {
			b.planeAdvancements$getPos().sub(direction);
		}
	}

	public enum LineType {
		DEFAULT,
		SMART,
		ROTATED
	}
}