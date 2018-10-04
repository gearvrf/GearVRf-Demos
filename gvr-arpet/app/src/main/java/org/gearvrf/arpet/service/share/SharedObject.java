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

package org.gearvrf.arpet.service.share;

import org.gearvrf.arpet.service.IMessageData;

import java.util.Arrays;

public abstract class SharedObject implements IMessageData {

    private static int sId;
    private int id;
    private float[] modelMatrix;

    public SharedObject(float[] modelMatrix) {
        this.id = incrementId();
        this.modelMatrix = modelMatrix;
    }

    private static int incrementId() {
        synchronized (SharedObject.class) {
            return ++sId;
        }
    }

    public int getId() {
        return id;
    }

    public float[] getModelMatrix() {
        return modelMatrix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedObject that = (SharedObject) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SharedObject{" +
                "id=" + id +
                ", modelMatrix=" + Arrays.toString(modelMatrix) +
                '}';
    }
}
