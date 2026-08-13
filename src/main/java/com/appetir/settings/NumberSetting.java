package com.appetir.settings;

public class NumberSetting extends Setting {

    private double value;
    private final double min, max, step;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = clamp(defaultValue);
    }

    public double get() { return value; }
    public float getFloat() { return (float) value; }
    public int getInt() { return (int) Math.round(value); }

    public void set(double value) {
        this.value = clamp(value);
    }

    public void increment() { set(value + step); }
    public void decrement() { set(value - step); }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    private double clamp(double v) {
        v = Math.max(min, Math.min(max, v));
        // snap to step
        if (step > 0) {
            v = Math.round(v / step) * step;
        }
        return v;
    }

    @Override
    public String getDisplayValue() {
        if (step >= 1.0) return String.valueOf(getInt());
        return String.format("%.2f", value);
    }
}
