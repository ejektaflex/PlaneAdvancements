package com.nettakrim.planeadvancements;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PlaneAdvancementsClient implements ClientModInitializer {
	public static final String MOD_ID = "planeadvancements";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Path configDir;

	public static TreeType treeType = TreeType.SPRING;
	public static float repulsion = 0.1f;
    public static boolean angledLines = true;

	public static AdvancementWidgetInterface draggedWidget;

	public static ButtonWidget treeButton;
	public static ButtonWidget lineButton;
	public static SliderWidget repulsionSlider;

	@Override
	public void onInitializeClient() {
		loadConfig();

		treeButton = ButtonWidget.builder(getTreeText(), (w) -> cycleTreeType()).dimensions(0,0,16,16).build();
		lineButton = ButtonWidget.builder(getLineText(), (w) -> cycleLineType()).dimensions(16,0,16,16).build();
		repulsionSlider = new SliderWidget(32, 0, 64, 16, getRepulsionText(), MathHelper.sqrt(repulsion)) {
			@Override
			protected void updateMessage() {
				setMessage(getRepulsionText());
			}

			@Override
			protected void applyValue() {
				repulsion = Math.max((float)(value * value), 0.01f);
			}
		};

		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> saveConfig());
	}

	private static Text getTreeText() {
		return Text.translatable(MOD_ID+".tree."+treeType.name().toLowerCase(Locale.ROOT));
	}

	private static Text getLineText() {
		return Text.translatable(MOD_ID+".line."+(angledLines ? "rotated" : "smart"));
	}

	private static Text getRepulsionText() {
		return Text.translatable(MOD_ID+".repulsion", repulsion <= 0.01f ? "0.0" : String.valueOf(MathHelper.sqrt(repulsion)+0.01f).substring(0,3));
	}

	public static LineType getCurrentLineType() {
		return treeType == TreeType.SPRING ? (angledLines ? LineType.ROTATED : LineType.SMART) : LineType.DEFAULT;
	}

	public static void cycleTreeType() {
		treeType = TreeType.values()[(treeType.ordinal()+1)%TreeType.values().length];
		treeButton.setMessage(getTreeText());
	}

	public static void cycleLineType() {
		angledLines = !angledLines;
		lineButton.setMessage(getLineText());
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

	private static final Codec<Data> dataCodec = RecordCodecBuilder.create((instance) -> instance.group(
			Codec.INT.optionalFieldOf("treeType", 1).forGetter(Data::treeType),
			Codec.FLOAT.optionalFieldOf("repulsion", 0.1f).forGetter(Data::repulsion),
			Codec.BOOL.optionalFieldOf("angledLines", true).forGetter(Data::angledLines)
	).apply(instance, Data::new));

	private record Data(int treeType, float repulsion, boolean angledLines) {}

	private static void loadConfig() {
		configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID+".json");
		if (!configDir.toFile().exists()) {
			saveConfig();
			return;
		}

		try {
			Data data = dataCodec.parse(JsonOps.INSTANCE, new GsonBuilder().create().fromJson(Files.newBufferedReader(configDir), JsonElement.class)).result().orElse(new Data(treeType.ordinal(), 0.1f, true));
			treeType = TreeType.values()[data.treeType];
			repulsion = data.repulsion;
			angledLines = data.angledLines;
		} catch (IOException e) {
			LOGGER.info("Failed to load file from {} {}", configDir, e);
		}
	}

	//see DataProvider.writeCodecToPath - it uses various @Beta and @Deprecated methods/classes
	@SuppressWarnings({"UnstableApiUsage", "deprecation"})
	private static void saveConfig() {
		CompletableFuture.runAsync(() -> {
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
				JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8));

				try {
					jsonWriter.setSerializeNulls(false);
					jsonWriter.setIndent("");
					JsonHelper.writeSorted(jsonWriter, dataCodec.encodeStart(JsonOps.INSTANCE, new Data(treeType.ordinal(), repulsion, angledLines)).getOrThrow(), DataProvider.JSON_KEY_SORTING_COMPARATOR);
				} catch (Throwable var9) {
					try {
						jsonWriter.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}

					throw var9;
				}

				jsonWriter.close();
				DataWriter.UNCACHED.write(configDir, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
			} catch (IOException e) {
				LOGGER.info("Failed to save file to {} {}", configDir, e);
			}
		}, Util.getMainWorkerExecutor().named("saveStable"));
	}
}