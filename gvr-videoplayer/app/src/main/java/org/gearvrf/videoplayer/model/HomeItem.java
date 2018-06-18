package org.gearvrf.videoplayer.model;

import android.support.annotation.IdRes;

public abstract class HomeItem extends GalleryItem {

    private String label;
    @IdRes
    private int imageResourceId;

    HomeItem(String label, int imageResourceId) {
        super(Type.TYPE_HOME);
        this.label = label;
        this.imageResourceId = imageResourceId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    @Override
    public String toString() {
        return "HomeItem{" +
                "label='" + label + '\'' +
                ", imageResourceId=" + imageResourceId +
                "} " + super.toString();
    }
}
