/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.cursor;

import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;

import java.util.concurrent.Future;

public class AssetHolder {
    private static final String TAG = AssetHolder.class.getSimpleName();
    private static final int NUM_STATES = 4;
    private final AssetObjectTuple[] tuples;

    public AssetHolder() {
        tuples = new AssetObjectTuple[NUM_STATES];
    }

    public static class AssetObjectTuple {
        final Future<GVRMesh> mesh;
        final Future<GVRTexture> texture;

        AssetObjectTuple(Future<GVRMesh> mesh, Future<GVRTexture> texture) {
            this.mesh = mesh;
            this.texture = texture;
        }
    }

    public AssetObjectTuple getTuple(int state) {
        return tuples[state];
    }

    public void setTuple(int state, AssetObjectTuple tuple) {
        tuples[state] = tuple;
    }
}
