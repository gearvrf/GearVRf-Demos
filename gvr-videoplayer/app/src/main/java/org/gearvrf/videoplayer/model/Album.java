/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
