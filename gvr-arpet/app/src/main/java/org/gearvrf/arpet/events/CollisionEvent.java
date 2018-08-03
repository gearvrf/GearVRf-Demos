/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.arpet.events;

import org.gearvrf.GVRSceneObject;

public class CollisionEvent {
    public enum Type {
        ENTER,
        EXIT
    }

    private GVRSceneObject mObject;
    private Type mType;

    public CollisionEvent(GVRSceneObject gvrSceneObject, Type type) {
        mObject = gvrSceneObject;
        mType = type;
    }

    public Type getType() {
        return mType;
    }

    public GVRSceneObject getObject() {
        return mObject;
    }

    public void setType(Type type) {
        mType = type;
    }
}
