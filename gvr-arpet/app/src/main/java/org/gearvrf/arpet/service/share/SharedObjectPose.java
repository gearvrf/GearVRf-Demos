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

import android.support.annotation.NonNull;

import org.gearvrf.arpet.constant.ArPetObjectType;

import java.io.Serializable;
import java.util.Arrays;

public class SharedObjectPose implements Serializable {

    private int id;

    @ArPetObjectType
    private String objectType;
    private float[] modelMatrix;

    public SharedObjectPose(
            int id,
            @NonNull @ArPetObjectType String objectType,
            @NonNull float[] modelMatrix) {

        this.id = id;
        this.objectType = objectType;
        this.modelMatrix = modelMatrix;
    }

    public int getId() {
        return id;
    }

    public float[] getModelMatrix() {
        return modelMatrix;
    }

    @ArPetObjectType
    public String getObjectType() {
        return objectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharedObjectPose that = (SharedObjectPose) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SharedObjectPose{" +
                "id=" + id +
                ", objectType='" + objectType + '\'' +
                ", modelMatrix=" + Arrays.toString(modelMatrix) +
                '}';
    }
}
