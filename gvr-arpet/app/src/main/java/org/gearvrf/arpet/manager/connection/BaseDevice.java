package org.gearvrf.arpet.manager.connection;

import org.gearvrf.arpet.connection.Device;

import java.util.Objects;

public abstract class BaseDevice implements Device {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseDevice that = (BaseDevice) o;
        return Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }
}
