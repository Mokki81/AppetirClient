package com.appetir.settings;

public class NumberSetting extends Setting {

    private double value;
    private final double min, max, step;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        super(name, description);
        this.min = min;
        this.max = max;
        this.step = step > 0 ? step : 1.0;
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
        // Snap relative to min so grid is min, min+step, ...
        if (step > 0) {
            double steps = Math.round((v - min) / step);
            v = min + steps * step;
            // floating error guard
            v = Math.max(min, Math.min(max, v));
            // avoid ugly 0.30000000004
            v = Math.round(v / step) * step;
            // re-align to min grid after round
            steps = Math.round((v - min) / step);
            v = min + steps * step;
            v = Math.max(min, Math.min(max, v));
        }
        return v;
    }

    @Override
    public String getDisplayValue() {
        if (step >= 1.0) return String.valueOf(getInt());
        return String.format("%.2f", value);
    }
}
