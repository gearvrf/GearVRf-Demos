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

package org.gearvrf.arpet.service.data;

import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.service.IMessageData;
import org.gearvrf.arpet.service.share.SharedObject;

import java.util.Arrays;

/**
 * Holds pet and other objects anchors to be
 * resolved through Google Cloud Anchor API
 */
public class SharedScene implements IMessageData {

    private CloudAnchor[] cloudAnchors; // Objects to resolve through ARCore
    private SharedObject[] sharedObjects; // Scene objects shared directly

    public SharedScene(CloudAnchor[] cloudAnchors) {
        this.cloudAnchors = cloudAnchors;
    }

    public CloudAnchor[] getCloudAnchors() {
        return cloudAnchors;
    }

    public SharedObject[] getSharedObjects() {
        return sharedObjects;
    }

    public void setSharedObjects(SharedObject[] sharedObjects) {
        this.sharedObjects = sharedObjects;
    }

    @Override
    public String toString() {
        return "SharedScene{" +
                "cloudAnchors=" + Arrays.toString(cloudAnchors) +
                ", sharedObjects=" + Arrays.toString(sharedObjects) +
                '}';
    }
}
