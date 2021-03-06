package com.ulyp.agent.settings;

import java.util.ArrayList;
import java.util.List;

public class SettingsProperty<T> {

    private final String name;
    private T value;
    private final List<SettingsPropertyListener<T>> listeners = new ArrayList<>();

    public SettingsProperty(String name) {
        this.name = name;
    }

    public SettingsProperty(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public T getValue() {
        return value;
    }

    synchronized void setValue(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        listeners.forEach(listener -> listener.onValueChanged(oldValue, newValue));
    }

    public synchronized void addListener(SettingsPropertyListener<T> listener) {
        listeners.add(listener);
        T currentValue = this.value;
        listener.onValueChanged(currentValue, currentValue);
    }

    @Override
    public String toString() {
        return "SettingsProperty{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
