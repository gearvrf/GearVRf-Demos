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

package org.gearvrf.arpet.event;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SharingModeViewEvent {

    @StringDef({Action.SHARE_PET_SCENE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {
        String SHARE_PET_SCENE = "SHARE_PET_SCENE";
    }

    @Action
    private String action;

    public SharingModeViewEvent(String action) {
        this.action = action;
    }

    @Action
    public String getAction() {
        return action;
    }
}
