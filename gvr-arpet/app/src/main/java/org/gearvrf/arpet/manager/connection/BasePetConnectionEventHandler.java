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

package org.gearvrf.arpet.manager.connection;

import android.support.annotation.NonNull;

public class BasePetConnectionEventHandler implements PetConnectionEventHandler {

    private String mName;

    public BasePetConnectionEventHandler(@NonNull String name) {
        this.mName = name;
    }

    @Override
    public void handleEvent(PetConnectionEvent event) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasePetConnectionEventHandler that = (BasePetConnectionEventHandler) o;
        return mName != null ? mName.equals(that.mName) : that.mName == null;
    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BasePetConnectionEventHandler{" +
                "mName='" + mName + '\'' +
                '}';
    }
}
