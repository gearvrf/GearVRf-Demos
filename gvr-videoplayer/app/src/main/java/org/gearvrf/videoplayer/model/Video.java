package org.gearvrf.videoplayer.model;

import android.support.annotation.IntDef;

public final class Video extends GalleryItem {

    @IntDef({VideoType.LOCAL, VideoType.EXTERNAL})
    public @interface VideoType {
        int LOCAL = 0;
        int EXTERNAL = 1;
    }

    private long id;
    private String title;
    private String path;
    private long duration;
    private boolean isRatio21;
    private @VideoType int videoType;

    public Video(long id, String title, String path, long duration, boolean isRatio21, @VideoType int videoType) {
        super(Type.TYPE_VIDEO);
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.isRatio21 = isRatio21;
        this.videoType = videoType;
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

    @VideoType
    public int getVideoType() {
        return videoType;
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
                ", videoType=" + videoType +
                "} " + super.toString();
    }
}
