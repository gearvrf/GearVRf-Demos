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

package org.gearvrf.arpet.movement.toscreen;

import org.gearvrf.arpet.movement.MovementPosition;
import org.joml.Vector3f;

public class ToScreenMovementPosition implements MovementPosition<Vector3f> {

    private Vector3f mValue = new Vector3f();

    @Override
    public Vector3f getValue() {
        return mValue;
    }

    @Override
    public void setValue(Vector3f value) {
        this.mValue.set(value);
    }
}
