package com.nettakrim.plane_advancements;

import java.util.Iterator;

public interface AdvancementTabInterface {
    Iterator<AdvancementWidgetInterface> planeAdvancements$getWidgets();

    double planeAdvancements$getPanX();
    double planeAdvancements$getPanY();
    void planeAdvancements$centerPan();

    void planeAdvancements$updateRange();
}
