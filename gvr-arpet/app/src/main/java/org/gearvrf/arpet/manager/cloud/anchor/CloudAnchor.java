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
 */

package org.gearvrf.arpet.manager.cloud.anchor;

import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.character.CharacterView;
import org.gearvrf.arpet.constant.ArPetObjectType;

import java.io.Serializable;

public class CloudAnchor implements Serializable {

    private String mCloudAnchorId;
    @ArPetObjectType
    private String mObjectType;

    public CloudAnchor(@ArPetObjectType String type) {
        mObjectType = type;
    }

    public String getCloudAnchorId() {
        return mCloudAnchorId;
    }

    public String getObjectType() {
        return mObjectType;
    }

    public void setCloudAnchorId(String id) {
        mCloudAnchorId = id;
    }

    public static CloudAnchor getFor(AnchoredObject object) {
        if (CharacterView.class.isInstance(object)) {
            new CloudAnchor(ArPetObjectType.PET);
        }
        return null;
    }

    @Override
    public String toString() {
        return "CloudAnchor{" +
                "mCloudAnchorId='" + mCloudAnchorId + '\'' +
                ", mObjectType='" + mObjectType + '\'' +
                '}';
    }
}
