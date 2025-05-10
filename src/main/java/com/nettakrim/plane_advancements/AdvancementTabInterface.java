package com.nettakrim.plane_advancements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface AdvancementTabInterface {
    Iterator<AdvancementWidgetInterface> planeAdvancements$getWidgets();
    AdvancementWidgetInterface planeAdvancements$getRoot();

    double planeAdvancements$getPanX();
    double planeAdvancements$getPanY();
    void planeAdvancements$centerPan(int width, int height);

    void planeAdvancements$updateRange();

    void planeAdvancements$heatGraph();
    void planeAdvancements$applyClusters(List<AdvancementCluster> clusters);

    default void planeAdvancements$arrangeIntoGrid() {
        AdvancementWidgetInterface root = planeAdvancements$getRoot();
        List<AdvancementWidgetInterface> clusterRoots = root.planeAdvancements$getChildren();
        clusterRoots.add(root);

        List<AdvancementCluster> clusters = new ArrayList<>(clusterRoots.size());
        for (AdvancementWidgetInterface clusterRoot : clusterRoots) {
            clusters.add(new AdvancementCluster(clusterRoot));
        }

        calculateGrid(clusters);
        planeAdvancements$applyClusters(clusters);
    }

    static void calculateGrid(List<AdvancementCluster> clusters) {
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
}
