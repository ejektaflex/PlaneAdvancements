package com.nettakrim.planeadvancements;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

public class CallableSlider extends SliderWidget {
    private final DoubleConsumer consumer;
    private final Supplier<Text> textSupplier;

    public CallableSlider(int x, int y, int width, int height, Supplier<Text> textSupplier, double value, DoubleConsumer consumer) {
        super(x, y, width, height, textSupplier.get(), value);
        this.consumer = consumer;
        this.textSupplier = textSupplier;
    }

    @Override
    protected void updateMessage() {
        setMessage(textSupplier.get());
    }

    @Override
    protected void applyValue() {
        consumer.accept(value);
    }
}
