package org.gearvrf.videoplayer.model;

public final class Album extends GalleryItem {

    private String title;
    private Video videoForThumbnail;

    public Album(String title, Video videoForThumbnail) {
        super(Type.TYPE_ALBUM);
        this.title = title;
        this.videoForThumbnail = videoForThumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Video getVideoForThumbnail() {
        return videoForThumbnail;
    }

    public void setVideoForThumbnail(Video videoForThumbnail) {
        this.videoForThumbnail = videoForThumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return title != null ? title.equals(album.title) : album.title == null;
    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Album{" +
                "title='" + title + '\'' +
                ", videoForThumbnail=" + videoForThumbnail +
                '}';
    }
}
