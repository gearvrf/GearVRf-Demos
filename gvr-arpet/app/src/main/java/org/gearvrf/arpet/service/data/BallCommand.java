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

import android.support.annotation.StringDef;

import org.joml.Vector3f;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BallCommand implements Command {

    public static final String THROW = "THROW";
    public static final String RESET = "RESET";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({THROW, RESET})
    public @interface Type {
    }

    @Type
    private String type;
    private Vector3f mForceVector;

    public BallCommand(@Type String type) {
        this.type = type;
    }

    @Type
    @Override
    public String getType() {
        return type;
    }

    public Vector3f getForceVector() {
        return mForceVector;
    }

    public void setForceVector(Vector3f forceVector) {
        this.mForceVector = forceVector;
    }

    @Override
    public String toString() {
        return "BallCommand{" +
                "type='" + type + '\'' +
                '}';
    }
}
