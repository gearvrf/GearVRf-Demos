package org.gearvrf.videoplayer.model;

public final class Video extends GalleryItem {

    private long id;
    private String title;
    private String path;
    private long duration;
    private boolean isRatio21;

    public Video(long id, String title, String path, long duration, boolean isRatio21) {
        super(Type.TYPE_VIDEO);
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.isRatio21 = isRatio21;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean getIsRatio21() {
        return this.isRatio21;
    }

    public void setIsRatio21(boolean isRatio21) {
        this.isRatio21 = isRatio21;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", duration=" + duration +
                ", type=" + type +
                ", isRatio21=" + isRatio21 +
                "} " + super.toString();
    }
}
