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

import android.graphics.Bitmap;
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
    private boolean is360tag;
    private boolean has360onTitle;
    private @VideoType int videoType;
    private Bitmap thumbnail;

    public Video(long id, String title, String path, long duration, boolean isRatio21,
                 boolean is360tag, boolean has360onTitle, @VideoType int videoType) {
        super(Type.TYPE_VIDEO);
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.isRatio21 = isRatio21;
        this.is360tag = is360tag;
        this.has360onTitle = has360onTitle;
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

    public boolean getIs360tag() {
        return is360tag;
    }

    public void setIs360tag(boolean is360tag) {
        this.is360tag = is360tag;
    }

    public boolean getHas360onTitle() {
        return has360onTitle;
    }

    public void setHas360onTitle(boolean has360onTitle) {
        this.has360onTitle = has360onTitle;
    }

    public void setThumbnail(Bitmap bitmap) {
        this.thumbnail = bitmap;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
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
                ", is360taq=" + is360tag +
                ", has360onTitle=" + has360onTitle +
                ", videoType=" + videoType +
                "} " + super.toString();
    }
}
