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

import android.view.View;

import org.gearvrf.arpet.R;

class ExitView extends BaseMainView implements IExitView {

    private View mCancelButton;
    private View mConfirmButton;

    public ExitView(View view, MainView controller) {
        super(view, controller);
        this.mCancelButton = view.findViewById(R.id.cancel_button_screen);
        this.mConfirmButton = view.findViewById(R.id.button_confirm);
    }

    @Override
    public void setOnCancelClickListener(View.OnClickListener listener) {
        mCancelButton.setOnClickListener(listener);
    }

    @Override
    public void setOnConfirmClickListener(View.OnClickListener listener) {
        mConfirmButton.setOnClickListener(listener);
    }
}
