package com.nettakrim.plane_advancements;

import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "plane_advancements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean springLineIsAngled = true;
	public static TreeType treeType = TreeType.SPRING;

	public static AdvancementWidgetInterface draggedWidget;

	public static float repulsion = 0.1f;

	public static ButtonWidget treeButton;
	public static ButtonWidget lineButton;
	public static SliderWidget repulsionSlider;

	@Override
	public void onInitializeClient() {
		treeButton = ButtonWidget.builder(Text.literal("="), (w) -> cycleTreeType()).dimensions(0,0,16,16).build();
		lineButton = ButtonWidget.builder(Text.literal("/"), (w) -> cycleLineType()).dimensions(16,0,16,16).build();
		repulsionSlider = new SliderWidget(32, 0, 50, 16, Text.literal("Spread"), MathHelper.sqrt(repulsion)) {
			@Override
			protected void updateMessage() {

			}

			@Override
			protected void applyValue() {
				repulsion = Math.max((float)(value * value), 0.01f);
			}
		};
	}

	public static LineType getCurrentLineType() {
		return treeType == TreeType.SPRING ? (springLineIsAngled ? LineType.ROTATED : LineType.SMART) : LineType.DEFAULT;
	}

	public static void cycleTreeType() {
		treeType = TreeType.values()[(treeType.ordinal()+1)%TreeType.values().length];
	}

	public static void cycleLineType() {
		springLineIsAngled = !springLineIsAngled;
	}

	public static boolean hoveredUI() {
		return treeButton.isHovered() || lineButton.isHovered() || repulsionSlider.isHovered();
	}

	public static boolean selectedUI() {
		return treeButton.isFocused() || lineButton.isFocused() || repulsionSlider.isFocused();
	}

	public static void clearUIHover() {
		treeButton.setFocused(false);
		lineButton.setFocused(false);
		repulsionSlider.setFocused(false);
	}
}