package com.nettakrim.planeadvancements;

import net.minecraft.advancement.AdvancementEntry;

import java.util.*;

public interface AdvancementTabInterface {
    Map<AdvancementEntry, AdvancementWidgetInterface> planeAdvancements$getWidgets();
    AdvancementWidgetInterface planeAdvancements$getRoot();

    double planeAdvancements$getPanX();
    double planeAdvancements$getPanY();
    void planeAdvancements$centerPan(int width, int height);
    void planeAdvancements$updateRange(int width, int height);

    void planeAdvancements$heatGraph();
    void planeAdvancements$applyClusters(List<AdvancementCluster> clusters);

    void planeAdvancements$setMerged(Collection<AdvancementTabInterface> tabs);
    void planeAdvancements$clearMerged(Collection<AdvancementTabInterface> tabs);
}
