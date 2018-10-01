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

import org.gearvrf.arpet.common.Dimension;

import java.util.Arrays;

public class SharedPlane extends SharedObject {

    private Dimension dimension;

    public SharedPlane(float[] modelMatrix, Dimension dimension) {
        super(modelMatrix);
        this.dimension = dimension;
    }

    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        return "SharedPlane{" +
                "dimension=" + dimension +
                ", modelMatrix=" + Arrays.toString(getModelMatrix()) +
                "} ";
    }
}
