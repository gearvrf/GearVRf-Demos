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
