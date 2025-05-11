package betteradvancements.common.gui;

import net.minecraft.client.gui.DrawContext;

public abstract class BetterAdvancementWidget {
    public abstract void drawConnectivity(DrawContext context, int scrollX, int scrollY, boolean border);
    public abstract int getX();
    public abstract int getY();
}
