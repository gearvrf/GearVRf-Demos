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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ViewCommand implements Command {

    public static final String SHOW_VIEW_LOOKING_AT_TARGET = "SHOW_VIEW_LOOKING_AT_TARGET";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            SHOW_VIEW_LOOKING_AT_TARGET
    })
    public @interface Type {
    }

    @Type
    private String type;

    public ViewCommand(@Type String type) {
        this.type = type;
    }

    @Type
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ViewCommand{" +
                "type='" + type + '\'' +
                '}';
    }
}
