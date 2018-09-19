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
 */

package org.gearvrf.arpet.cloud.anchor;

import android.support.annotation.IntDef;

import org.gearvrf.arpet.manager.connection.bluetooth.BTMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class CommandViewMessage extends BTMessage {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CommandViewType.SHOW_PAIRED_VIEW})
    public @interface CommandViewType {
        int SHOW_PAIRED_VIEW = 1;
    }

    public CommandViewMessage(@CommandViewType int type) {
        super(type);
    }
}
