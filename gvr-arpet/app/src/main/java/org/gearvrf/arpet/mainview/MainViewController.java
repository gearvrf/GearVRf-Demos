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

package org.gearvrf.arpet.mainview;

import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;

public class MainViewController extends BaseViewController {

    public MainViewController(PetContext petContext) {
        super(petContext);
        registerView(IExitView.class, R.layout.screen_exit_application, ExitView.class);
        registerView(IConnectionFinishedView.class, R.layout.view_connection_finished, ConnectionFinishedView.class);
    }
}
