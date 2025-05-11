package com.nettakrim.planeadvancements;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.joml.Vector2f;

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

        List<AdvancementCluster> clusters = new ArrayList<>(clusterRoots.size());
        for (AdvancementWidgetInterface clusterRoot : clusterRoots) {
            clusters.add(new AdvancementCluster(clusterRoot));
        }

        calculateGrid(new AdvancementCluster(root), clusters);
        planeAdvancements$applyClusters(clusters);
    }

    static void calculateGrid(AdvancementCluster root, List<AdvancementCluster> clusters) {
        //sort bigger clusters to start of the list
        clusters.sort((a, b) -> {
            int i = Float.compare(b.size.y, a.size.y);
            return i == 0 ? Float.compare(b.size.x, a.size.x) : i;
        });

        int maxWidth = 9;
        int usedWidth = 0;

        IntArrayList mask = new IntArrayList();

        // place clusters in first available spot, stepping left, then up when max width is reached
        for (AdvancementCluster cluster : clusters) {
            int y = 0;
            int search = (maxWidth - cluster.size.x) + 1;

            // if a cluster is too big, just let it overflow
            if (search <= 0) {
                for (int i = 0; i < cluster.size.y; i++) {
                    mask.add(Integer.MAX_VALUE);
                }
                cluster.pos.x = 0;
                cluster.pos.y = y;
                usedWidth = maxWidth;
                continue;
            }

            // otherwise step through positions
            while (true) {
                int foundPosition = getFreePos(mask, cluster, search, y);

                if (foundPosition >= 0) {
                    int foundMask = getMask(cluster.size.x, foundPosition);
                    for (int u = 0; u < cluster.size.y; u++) {
                        int index = y+u;
                        if (index >= mask.size()) {
                            mask.add(foundMask);
                        } else {
                            mask.set(index, mask.getInt(index) | foundMask);
                        }
                    }

                    cluster.pos.x = foundPosition;
                    cluster.pos.y = y;
                    usedWidth = Math.max(usedWidth, foundPosition + cluster.size.x);
                    break;
                }

                y++;
            }
        }

        // settle clusters to what will be the left, this is better than just doing rightwards to begin with as it means bigger clusters will be further to what will be the right
        for (AdvancementCluster cluster : clusters.reversed()) {
            int position = (int)cluster.pos.x;
            int y = (int)cluster.pos.y;

            int startingFilter = ~getMask(cluster.size.x, position);

            while (true) {
                position++;

                int sizeMask = getMask(cluster.size.x, position);

                boolean fits;

                if (cluster.size.x + position > maxWidth) {
                    fits = false;
                } else {
                    fits = true;
                    for (int u = 0; u < cluster.size.y; u++) {
                        int index = y + u;
                        if ((mask.getInt(index) & startingFilter & sizeMask) != 0) {
                            fits = false;
                        }
                    }
                }

                if (!fits) {
                    position--;
                    if (position == cluster.pos.x) {
                        break;
                    }

                    sizeMask = getMask(cluster.size.x, position);
                    for (int u = 0; u < cluster.size.y; u++) {
                        int index = y+u;
                        mask.set(index, (mask.getInt(index) & startingFilter) | sizeMask);
                    }
                    cluster.pos.x = position;
                    break;
                }
            }
        }

        // xy mirror everything
        Vector2f v = new Vector2f(usedWidth+1, mask.size());
        for (AdvancementCluster cluster : clusters) {
            v.sub(cluster.pos, cluster.pos).sub(cluster.size.x, cluster.size.y);
        }

        // set root position, then add it to list so it's position gets applied
        root.pos.x = 0;
        root.pos.y = (mask.size()-1)/2f;
        clusters.add(root);
    }

    static int getFreePos(IntArrayList mask, AdvancementCluster cluster, int search, int y) {
        if (y >= mask.size()) {
            return 0;
        }
        int currentMask = mask.getInt(y);

        for (int i = 0; i < search; i++) {
            int sizeMask = getMask(cluster.size.x, i);
            if ((currentMask & sizeMask) == 0) {
                boolean found = true;

                for (int u = 0; u < cluster.size.y; u++) {
                    int index = y+u;
                    if (index >= mask.size()) {
                        return i;
                    }
                    if ((mask.getInt(index) & sizeMask) != 0) {
                        found = false;
                        break;
                    }
                }

                if (found) {
                    return i;
                }
            }
        }

        return -1;
    }

    static int getMask(int size, int position) {
        return ((1 << size)-1) * (1 << position);
    }
}
